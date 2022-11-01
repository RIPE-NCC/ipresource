/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
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

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
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
    public static final IpResourceRange ALL_AS_RESOURCES = IpResourceRange.parse(String.format("AS%d-AS%d", Asn.ASN_MIN_VALUE, Asn.ASN32_MAX_VALUE));
    public static final IpRange ALL_IPV4_RESOURCES = IpRange.parse("0.0.0.0/0");
    public static final IpRange ALL_IPV6_RESOURCES = IpRange.parse("::/0");

    public static final ImmutableResourceSet IP_PRIVATE_USE_RESOURCES = ImmutableResourceSet.parse("10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");
    public static final ImmutableResourceSet ASN_PRIVATE_USE_RESOURCES = ImmutableResourceSet.parse("AS64512-AS65534");
    public static final ImmutableResourceSet ALL_PRIVATE_USE_RESOURCES = ImmutableResourceSet.parse("AS64512-AS65534,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");

    private static final long serialVersionUID = 1L;

    private static final ImmutableResourceSet EMPTY = new ImmutableResourceSet();
    private static final ImmutableResourceSet UNIVERSAL = new ImmutableResourceSet(ALL_AS_RESOURCES, ALL_IPV4_RESOURCES, ALL_IPV6_RESOURCES);

    /*
     * Resources keyed by their end-point. This allows fast lookup to find potentially overlapping resources:
     *
     * resourcesByEndPoint.ceilingEntry(resourceToLookup.getStart())
     */
    private final TreeMap<IpResource, IpResource> resourcesByEndPoint;

    private ImmutableResourceSet() {
        this.resourcesByEndPoint = new TreeMap<>();
    }

    public ImmutableResourceSet(ImmutableResourceSet resources) {
        this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
    }

    public ImmutableResourceSet(IpResource... resources) {
        this();
        for (IpResource resource : resources) {
            doAdd(resource);
        }
    }

    public ImmutableResourceSet(Collection<? extends IpResource> resources) {
        this();
        for (IpResource resource : resources) {
            doAdd(resource);
        }
    }

    public ImmutableResourceSet(IpResourceSet resources) {
        this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
    }

    private ImmutableResourceSet(TreeMap<IpResource, IpResource> resourcesByEndPoint) {
        this.resourcesByEndPoint = resourcesByEndPoint;
    }

    public static ImmutableResourceSet empty() {
        return EMPTY;
    }

    public static ImmutableResourceSet universal() {
        return UNIVERSAL;
    }

    public static ImmutableResourceSet of(IpResource... resources) {
        return new ImmutableResourceSet(resources);
    }

    public static Collector<IpResource, ImmutableResourceSet, ImmutableResourceSet> collector() {
        return Collector.of(
            ImmutableResourceSet::new,
            ImmutableResourceSet::doAdd,
            ImmutableResourceSet::union,
            Collector.Characteristics.UNORDERED
        );
    }

    public ImmutableResourceSet add(IpResource value) {
        if (this.contains(value)) {
            return this;
        } else {
            ImmutableResourceSet result = new ImmutableResourceSet(this);
            result.doAdd(value);
            return result;
        }
    }

    public ImmutableResourceSet remove(IpResource value) {
        if (!this.intersects(value)) {
            return this;
        }
        ImmutableResourceSet result = new ImmutableResourceSet(this);
        result.doRemove(value);
        return result;
    }

    public ImmutableResourceSet union(ImmutableResourceSet that) {
        if (this.isEmpty()) {
            return that;
        } else if (that.isEmpty()) {
            return this;
        } else if (this.resourcesByEndPoint.size() < that.resourcesByEndPoint.size()) {
            ImmutableResourceSet result = new ImmutableResourceSet(that);
            result.doAddAll(this);
            return result;
        } else {
            ImmutableResourceSet result = new ImmutableResourceSet(this);
            result.doAddAll(that);
            return result;
        }
    }

    public ImmutableResourceSet intersection(ImmutableResourceSet that) {
        if (this.isEmpty()) {
            return this;
        } else if (that.isEmpty()) {
            return that;
        } else {
            TreeMap<IpResource, IpResource> temp = new TreeMap<>();
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
            return new ImmutableResourceSet(temp);
        }
    }

    public ImmutableResourceSet difference(ImmutableResourceSet that) {
        if (this.isEmpty() || that.isEmpty()) {
            return this;
        } else {
            ImmutableResourceSet result = new ImmutableResourceSet(this);
            for (IpResource resource: that) {
                result.doRemove(resource);
            }
            return result;
        }
    }

    public ImmutableResourceSet complement() {
        return universal().difference(this);
    }

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
        Entry<IpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        return potentialMatch != null && potentialMatch.getValue().contains(resource);
    }

    public boolean contains(ImmutableResourceSet other) {
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
        Entry<IpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
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
        ImmutableResourceSet result = new ImmutableResourceSet();
        for (String r : resources) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
                result.doAdd(IpResource.parse(trimmed));
            }
        }
        return result;
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

    private void doAddAll(ImmutableResourceSet ipResourceSet) {
        for (IpResource ipResource: ipResourceSet.resourcesByEndPoint.values()) {
            doAdd(ipResource);
        }
    }

    private void doAdd(IpResource resource) {
        Validate.notNull(resource, "resource is null");

        UniqueIpResource start = resource.getStart();
        if (!start.equals(start.getType().getMinimum())) {
            start = start.predecessor();
        }

        IpResource resourceToAdd = normalize(resource);

        Iterator<IpResource> iterator = resourcesByEndPoint.tailMap(start, true).values().iterator();
        while (iterator.hasNext()) {
            IpResource potentialMatch = iterator.next();
            if (resourceToAdd.isMergeable(potentialMatch)) {
                iterator.remove();
                resourceToAdd = resourceToAdd.merge(potentialMatch);
            } else {
                break;
            }
        }

        IpResource normalized = normalize(resourceToAdd);
        resourcesByEndPoint.put(normalized.getEnd(), normalized);
    }

    private void doRemove(IpResource resource) {
        Entry<IpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        while (potentialMatch != null && potentialMatch.getValue().overlaps(resource)) {
            resourcesByEndPoint.remove(potentialMatch.getKey());

            for (IpResource remains: potentialMatch.getValue().subtract(resource)) {
                doAdd(remains);
            }

            potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        }
    }

    private static IpResource normalize(IpResource resource) {
        return resource.isUnique() ? resource.unique() : resource;
    }

}
