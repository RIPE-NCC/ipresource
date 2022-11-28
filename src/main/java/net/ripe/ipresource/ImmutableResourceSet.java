/**
 * The BSD License
 *
 * Copyright (c) 2010-2022 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.ipresource;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An immutable set of IP resources. Resources can be ASNs, IPv4 addresses, IPv6
 * addresses, or ranges. Adjacent resources are merged. Single-sized ranges are
 * normalized into single resources.
 */
public final class ImmutableResourceSet implements Iterable<IpResource>, Serializable {

    public static final ImmutableResourceSet IP_PRIVATE_USE_RESOURCES = ImmutableResourceSet.parse("10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");
    public static final ImmutableResourceSet ASN_PRIVATE_USE_RESOURCES = ImmutableResourceSet.parse("AS64512-AS65534,AS4200000000-AS4294967294");
    public static final ImmutableResourceSet ALL_PRIVATE_USE_RESOURCES = ASN_PRIVATE_USE_RESOURCES.union(IP_PRIVATE_USE_RESOURCES);

    private static final long serialVersionUID = 1L;

    private static final ImmutableResourceSet EMPTY = new ImmutableResourceSet();
    private static final ImmutableResourceSet UNIVERSAL = ImmutableResourceSet.of(IpResource.ALL_AS_RESOURCES, IpResource.ALL_IPV4_RESOURCES, IpResource.ALL_IPV6_RESOURCES);

    /*
     * Resources keyed by their end-point. This allows fast lookup to find potentially overlapping resources:
     *
     * resourcesByEndPoint.ceilingEntry(resourceToLookup.getStart())
     */
    final TreeMap<UniqueIpResource, IpResource> resourcesByEndPoint;

    private ImmutableResourceSet() {
        this.resourcesByEndPoint = new TreeMap<>();
    }

    private ImmutableResourceSet(TreeMap<UniqueIpResource, IpResource> resourcesByEndPoint) {
        if (resourcesByEndPoint.isEmpty()) {
            throw new IllegalArgumentException("empty resource set must use ImmutableResourceSet.empty()");
        }
        this.resourcesByEndPoint = resourcesByEndPoint;
    }


    public static ImmutableResourceSet of() {
        return empty();
    }

    public static ImmutableResourceSet of(IpResource resource) {
        TreeMap<UniqueIpResource, IpResource> resourcesByEndpoint = new TreeMap<>();
        resourcesByEndpoint.put(resource.getEnd(), normalize(resource));
        return new ImmutableResourceSet(resourcesByEndpoint);
    }

    public static ImmutableResourceSet of(IpResource... resources) {
        return resources.length == 0 ? empty() : ImmutableResourceSet.of(Arrays.asList(resources));
    }

    public static ImmutableResourceSet of(Iterable<? extends IpResource> resources) {
        if (resources instanceof ImmutableResourceSet) {
            return (ImmutableResourceSet) resources;
        } else if (resources instanceof IpResourceSet) {
            return of((IpResourceSet) resources);
        } else {
            return new Builder(resources).build();
        }
    }

    public static ImmutableResourceSet of(IpResourceSet resources) {
        return resources.isEmpty() ? empty() : new ImmutableResourceSet(resources.resourcesByEndPoint);
    }

    public static ImmutableResourceSet empty() {
        return EMPTY;
    }

    public static ImmutableResourceSet universal() {
        return UNIVERSAL;
    }

    public static Collector<IpResource, ImmutableResourceSet.Builder, ImmutableResourceSet> collector() {
        return Collector.of(
            Builder::new,
            Builder::add,
            (a, b) -> a.addAll(b.resourcesByEndPoint.values()),
            Builder::build,
            Collector.Characteristics.UNORDERED
        );
    }

    public ImmutableResourceSet add(IpResource value) {
        if (this.contains(value)) {
            return this;
        } else {
            return new Builder(this).add(value).build();
        }
    }

    public ImmutableResourceSet remove(IpResource value) {
        if (!this.intersects(value)) {
            return this;
        }
        return new Builder(this).remove(value).build();
    }

    /**
     * @return $this \cup that$
     */
    public ImmutableResourceSet union(ImmutableResourceSet that) {
        if (this.isEmpty()) {
            return that;
        } else if (that.isEmpty()) {
            return this;
        } else if (this.resourcesByEndPoint.size() < that.resourcesByEndPoint.size()) {
            return new Builder(that).addAll(this.resourcesByEndPoint.values()).build();
        } else {
            return new Builder(this).addAll(that.resourcesByEndPoint.values()).build();
        }
    }

    /**
     * @return $this \cap that$
     */
    public ImmutableResourceSet intersection(ImmutableResourceSet that) {
        if (this.isEmpty()) {
            return this;
        } else if (that.isEmpty()) {
            return that;
        } else {
            TreeMap<UniqueIpResource, IpResource> temp = new TreeMap<>();
            Iterator<IpResource> thisIterator = this.iterator();
            Iterator<IpResource> thatIterator = that.iterator();
            IpResource thisResource = thisIterator.next();
            IpResource thatResource = thatIterator.next();
            while (thisResource != null && thatResource != null) {
                IpResource intersect = thisResource.intersect(thatResource);
                if (intersect != null) {
                    temp.put(intersect.getEnd(), normalize(intersect));
                }
                int compareTo = thisResource.getEnd().compareTo(thatResource.getEnd());
                if (compareTo <= 0) {
                    thisResource = thisIterator.hasNext() ? thisIterator.next() : null;
                }
                if (compareTo >= 0) {
                    thatResource = thatIterator.hasNext() ? thatIterator.next() : null;
                }
            }
            return temp.isEmpty() ? ImmutableResourceSet.empty() : new ImmutableResourceSet(temp);
        }
    }

    @Deprecated
    public ImmutableResourceSet difference(ImmutableResourceSet that) {
        return this.minus(that);
    }

    /**
     * @return $this \setminus that$
     */
    public ImmutableResourceSet minus(ImmutableResourceSet that) {
        if (!this.intersects(that)) {
            return this;
        } else {
            return new Builder(this).removeAll(that).build();
        }
    }

    /**
     * @return $this \Delta that$
     */
    public ImmutableResourceSet symmetricDifference(ImmutableResourceSet that) {
        // $this \Delta that = (this \setminus that) \cup (that \setminus this)$
        return (this.minus(that)).union((that.minus(this)));
    }

    public ImmutableResourceSet complement() {
        return universal().difference(this);
    }

    @Override
    public Iterator<IpResource> iterator() {
        return Collections.unmodifiableMap(resourcesByEndPoint).values().iterator();
    }

    @Override
    public Spliterator<IpResource> spliterator() {
        return Spliterators.spliterator(resourcesByEndPoint.values(),
            Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.IMMUTABLE);
    }

    public Stream<IpResource> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public boolean isEmpty() {
        return resourcesByEndPoint.isEmpty();
    }

    public boolean contains(IpResource resource) {
        Entry<UniqueIpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        return potentialMatch != null && potentialMatch.getValue().contains(resource);
    }

    public boolean contains(Iterable<? extends IpResource> other) {
        for (IpResource resource: other) {
            if (!contains(resource)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsType(IpResourceType type) {
        for (IpResource resource: resourcesByEndPoint.values()) {
            if (type == resource.getType()) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(IpResource resource) {
        Entry<UniqueIpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        return potentialMatch != null && potentialMatch.getValue().overlaps(resource);
    }

    public boolean intersects(ImmutableResourceSet that) {
        if (this.isEmpty() || that.isEmpty()) {
            return false;
        }
        Iterator<IpResource> thisIterator = this.iterator();
        Iterator<IpResource> thatIterator = that.iterator();
        IpResource thisResource = thisIterator.next();
        IpResource thatResource = thatIterator.next();
        while (thisResource != null && thatResource != null) {
            if (thisResource.overlaps(thatResource)) {
                return true;
            }
            int compareTo = thisResource.getEnd().compareTo(thatResource.getEnd());
            if (compareTo <= 0) {
                thisResource = thisIterator.hasNext() ? thisIterator.next() : null;
            }
            if (compareTo >= 0) {
                thatResource = thatIterator.hasNext() ? thatIterator.next() : null;
            }
        }
        return false;
    }

    public static ImmutableResourceSet parse(String s) {
        String[] resources = s.split(",");
        Builder builder = new Builder();
        for (String r : resources) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
                builder.add(IpResource.parse(trimmed));
            }
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return resourcesByEndPoint.values().stream().map(Objects::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! (obj instanceof ImmutableResourceSet)) {
            return false;
        }
        ImmutableResourceSet other = (ImmutableResourceSet) obj;
        return resourcesByEndPoint.equals(other.resourcesByEndPoint);
    }

    @Override
    public int hashCode() {
        return resourcesByEndPoint.hashCode();
    }

    public static class Builder {
        private TreeMap<UniqueIpResource, IpResource> resourcesByEndPoint;

        public Builder() {
            this.resourcesByEndPoint = new TreeMap<>();
        }

        public Builder(ImmutableResourceSet resources) {
            this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
        }

        public Builder(IpResourceSet resources) {
            this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
        }

        public Builder(Iterable<? extends IpResource> resources) {
            if (resources instanceof ImmutableResourceSet) {
                this.resourcesByEndPoint = new TreeMap<>(((ImmutableResourceSet) resources).resourcesByEndPoint);
            } else if (resources instanceof IpResourceSet) {
                this.resourcesByEndPoint = new TreeMap<>(((IpResourceSet) resources).resourcesByEndPoint);
            } else {
                this.resourcesByEndPoint = new TreeMap<>();
                for (IpResource resource : resources) {
                    add(resource);
                }
            }
        }

        public ImmutableResourceSet build() {
            assertNotAlreadyUsed();
            try {
                return resourcesByEndPoint.isEmpty() ? empty() : new ImmutableResourceSet(resourcesByEndPoint);
            } finally {
                resourcesByEndPoint = null;
            }
        }

        public Builder addAll(Iterable<? extends IpResource> resources) {
            assertNotAlreadyUsed();
            for (IpResource ipResource: resources) {
                add(ipResource);
            }
            return this;
        }

        public Builder removeAll(Iterable<? extends IpResource> resources) {
            assertNotAlreadyUsed();
            for (IpResource resource: resources) {
                remove(resource);
            }
            return this;
        }

        public Builder add(IpResource resource) {
            assertNotAlreadyUsed();

            UniqueIpResource start = resource.getStart();
            if (!start.equals(start.getType().getMinimum())) {
                start = start.predecessor();
            }

            Iterator<IpResource> iterator = resourcesByEndPoint.tailMap(start, true).values().iterator();
            while (iterator.hasNext()) {
                IpResource potentialMatch = iterator.next();
                if (resource.isMergeable(potentialMatch)) {
                    iterator.remove();
                    resource = resource.merge(potentialMatch);
                } else {
                    break;
                }
            }

            resourcesByEndPoint.put(resource.getEnd(), normalize(resource));

            return this;
        }

        public Builder remove(IpResource resource) {
            assertNotAlreadyUsed();

            Entry<UniqueIpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
            while (potentialMatch != null && potentialMatch.getValue().overlaps(resource)) {
                resourcesByEndPoint.remove(potentialMatch.getKey());

                for (IpResource remains: potentialMatch.getValue().subtract(resource)) {
                    resourcesByEndPoint.put(remains.getEnd(), normalize(remains));
                }

                potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
            }

            return this;
        }

        private void assertNotAlreadyUsed() {
            if (resourcesByEndPoint == null) {
                throw new IllegalStateException("builder can only be used once");
            }
        }
    }

    private static IpResource normalize(IpResource resource) {
        return resource.isUnique() ? resource.getStart() : resource;
    }
}
