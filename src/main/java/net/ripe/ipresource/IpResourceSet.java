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

import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A mutable set of IP resources. Resources can be ASNs, IPv4 addresses, IPv6
 * addresses, or ranges. Adjacent resources are merged. Single-sized ranges are
 * normalized into single resources.
 * <p>
 *     Note: the {@link ImmutableResourceSet} is now preferred to this class.
 * </p>
 */
public class IpResourceSet implements Iterable<IpResource>, Serializable {

    /**
     * @deprecated it is not safe to use this constant since the {@code IpResourceSet} can be mutated.
     */
    @Deprecated
    public static final IpResourceSet IP_PRIVATE_USE_RESOURCES = new IpResourceSet(ImmutableResourceSet.IP_PRIVATE_USE_RESOURCES);
    /**
     * @deprecated it is not safe to use this constant since the {@code IpResourceSet} can be mutated.
     */
    @Deprecated
    public static final IpResourceSet ASN_PRIVATE_USE_RESOURCES = new IpResourceSet(ImmutableResourceSet.ASN_PRIVATE_USE_RESOURCES);
    /**
     * @deprecated it is not safe to use this constant since the {@code IpResourceSet} can be mutated.
     */
    @Deprecated
    public static final IpResourceSet ALL_PRIVATE_USE_RESOURCES = new IpResourceSet(ImmutableResourceSet.ALL_PRIVATE_USE_RESOURCES);

    private static final long serialVersionUID = 1L;


    /*
     * Resources keyed by their end-point. This allows fast lookup to find potentially overlapping resources:
     *
     * resourcesByEndPoint.ceilingEntry(resourceToLookup.getStart())
     */
    TreeMap<UniqueIpResource, IpResource> resourcesByEndPoint;

    public IpResourceSet() {
        this.resourcesByEndPoint = new TreeMap<>();
    }

    public IpResourceSet(IpResourceSet resources) {
        this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
    }

    public IpResourceSet(ImmutableResourceSet resources) {
        this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
    }

    public IpResourceSet(IpResource... resources) {
        this();
        for (IpResource resource : resources) {
            add(resource);
        }
    }

    public IpResourceSet(Collection<? extends IpResource> resources) {
        this((Iterable<? extends IpResource>) resources);
    }

    public IpResourceSet(Iterable<? extends IpResource> resources) {
        if (resources instanceof IpResourceSet) {
            this.resourcesByEndPoint = new TreeMap<>(((IpResourceSet) resources).resourcesByEndPoint);
        } else if (resources instanceof ImmutableResourceSet) {
            this.resourcesByEndPoint = new TreeMap<>(((ImmutableResourceSet) resources).resourcesByEndPoint);
        } else {
            this.resourcesByEndPoint = new TreeMap<>();
            for (IpResource resource : resources) {
                add(resource);
            }
        }
    }

    public void addAll(Iterable<? extends IpResource> resources) {
        for (IpResource ipResource: resources) {
            add(ipResource);
        }
    }

    public void add(IpResource resource) {
        Validate.notNull(resource, "resource is null");

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

    public static IpResourceSet parse(String s) {
        String[] resources = s.split(",");
        IpResourceSet result = new IpResourceSet();
        for (String r : resources) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
                result.add(IpResource.parse(trimmed));
            }
        }
        return result;
    }

    @Override
    public Iterator<IpResource> iterator() {
        return resourcesByEndPoint.values().iterator();
    }

    @Override
    public Spliterator<IpResource> spliterator() {
        return Spliterators.spliterator(resourcesByEndPoint.values(),
            Spliterator.DISTINCT | Spliterator.SORTED);
    }

    public Stream<IpResource> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public boolean remove(IpResource resource) {
        boolean removed = false;

        Entry<UniqueIpResource, IpResource> potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        while (potentialMatch != null && potentialMatch.getValue().overlaps(resource)) {
            resourcesByEndPoint.remove(potentialMatch.getKey());
            removed = true;

            for (IpResource remains: potentialMatch.getValue().subtract(resource)) {
                add(remains);
            }

            potentialMatch = resourcesByEndPoint.ceilingEntry(resource.getStart());
        }

        return removed;
    }

    public void removeAll(Iterable<? extends IpResource> other) {
        for (IpResource resource: other) {
            remove(resource);
        }
    }

    public void retainAll(IpResourceSet other) {
        if (this.isEmpty()) {
            return;
        } else if (other.isEmpty()) {
            resourcesByEndPoint.clear();
            return;
        }

        TreeMap<UniqueIpResource, IpResource> temp = new TreeMap<>();
        Iterator<IpResource> thisIterator = this.iterator();
        Iterator<IpResource> thatIterator = other.iterator();
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
        this.resourcesByEndPoint = temp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! (obj instanceof IpResourceSet)) {
            return false;
        }
        IpResourceSet other = (IpResourceSet) obj;
        return resourcesByEndPoint.equals(other.resourcesByEndPoint);
    }

    @Override
    public int hashCode() {
        return resourcesByEndPoint.hashCode();
    }

    @Override
    public String toString() {
        String s = resourcesByEndPoint.values().toString();
        return s.substring(1, s.length() - 1);
    }

    private IpResource normalize(IpResource resource) {
        return resource.isUnique() ? resource.getStart() : resource;
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        if (!gf.defaulted("resourcesByEndPoint")) {
            resourcesByEndPoint = (TreeMap<UniqueIpResource, IpResource>) gf.get("resourcesByEndPoint", null);
        } else {
            SortedSet<IpResource> resources = (SortedSet<IpResource>) gf.get("resources", null);
            resourcesByEndPoint = new TreeMap<>();
            for (IpResource resource: resources) {
                resourcesByEndPoint.put(resource.getEnd(), resource);
            }
        }
    }
}
