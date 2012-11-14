/**
 * The BSD License
 *
 * Copyright (c) 2010, 2011 RIPE NCC
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class IpResourceSet implements Iterable<IpResource>, Serializable {

    public static final IpResourceSet IP_PRIVATE_USE_RESOURCES = IpResourceSet.parse("10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");
    public static final IpResourceSet ASN_PRIVATE_USE_RESOURCES = IpResourceSet.parse("AS64512-AS65534");
    public static final IpResourceSet ALL_PRIVATE_USE_RESOURCES = IpResourceSet.parse("AS64512-AS65534,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");

    private static final long serialVersionUID = 1L;

    private SortedSet<IpResource> resources = new TreeSet<IpResource>();

    public IpResourceSet() {
    }

    public IpResourceSet(IpResource... resources) {
        for (IpResource resource : resources) {
            add(resource);
        }
    }

    public IpResourceSet(IpResourceSet resources) {
        this.resources = new TreeSet<IpResource>(resources.resources);
    }

    public IpResourceSet(Collection<? extends IpResource> resources) {
        for (IpResource resource : resources) {
            add(resource);
        }
    }

    public void addAll(IpResourceSet ipResourceSet) {
        for (IpResource ipResource: ipResourceSet.resources) {
            add(ipResource);
        }
        normalize();
    }

    public void add(IpResource resource) {
        Validate.notNull(resource, "resource is null");
        resources.add(resource);
    }

    public boolean isEmpty() {
        return resources.isEmpty();
    }

    public boolean contains(IpResource resource) {
        return contains(new IpResourceSet(resource));
    }

    public boolean contains(IpResourceSet other) {
        if (isEmpty()) {
            return other.isEmpty();
        }

        normalize();
        other.normalize();

        Iterator<IpResource> it1 = resources.iterator();
        Iterator<IpResource> it2 = other.resources.iterator();
        IpResource r1 = it1.next();
        while (it2.hasNext()) {
            IpResource r2 = it2.next();
            while (!r1.contains(r2) && it1.hasNext()) {
                r1 = it1.next();
            }
            if (!r1.contains(r2)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsType(IpResourceType type) {
        for (IpResource resource: resources) {
            if (type == resource.getType()) {
                return true;
            }
        }
        return false;
    }

    public static IpResourceSet parse(String s) {
        if ("INHERITED".equalsIgnoreCase(s)) {
            return InheritedIpResourceSet.getInstance();
        }

        String[] resources = s.split(",");
        IpResourceSet result = new IpResourceSet();
        for (String r : resources) {
            if (!StringUtils.isBlank(r)) {
                result.add(IpResource.parse(r.trim()));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! (obj instanceof IpResourceSet)) {
            return false;
        }
        normalize();
        IpResourceSet other = (IpResourceSet) obj;
        other.normalize();
        return resources.equals(other.resources);
    }

    @Override
    public int hashCode() {
        normalize();
        return resources.hashCode();
    }

    @Override
    public String toString() {
        normalize();
        String s = resources.toString();
        return s.substring(1, s.length() - 1);
    }

    /**
     * Normalizes this resource set: turns singleton ranges into single
     * resources, merges adjacent resources into ranges, and removes enclosed
     * ranges.
     *
     * Depends on the <code>resources</code> being sorted!
     */
    private void normalize() {
        if (resources.isEmpty()) {
            return;
        }

        TreeSet<IpResource> normalized = new TreeSet<IpResource>();
        Iterator<IpResource> it = resources.iterator();
        IpResource current = it.next();
        while (it.hasNext()) {
            IpResource next = it.next();
            if (current.contains(next)) {
                // Skip.
            } else if (current.isMergeable(next)) {
                current = current.merge(next);
            } else {
                normalized.add(normalize(current));
                current = next;
            }
        }
        normalized.add(normalize(current));
        resources = normalized;
    }

    private IpResource normalize(IpResource resource) {
        return resource.isUnique() ? resource.unique() : resource;
    }

    public Iterator<IpResource> iterator() {
        normalize();
        return resources.iterator();
    }

    public boolean remove(IpResource prefix) {
        SortedSet<IpResource> temp = new TreeSet<IpResource>();
        for (IpResource resource : resources) {
            temp.addAll(resource.subtract(prefix));
        }
        if (!temp.equals(resources)) {
            resources = temp;
            return true;
        } else {
            return false;
        }
    }

    public void removeAll(IpResourceSet other) {
        for (IpResource resource: other) {
            remove(resource);
        }
    }

    public void retainAll(IpResourceSet other) {
        if (this.isEmpty()) {
            return;
        } else if (other.isEmpty()) {
            resources.clear();
            return;
        }

        SortedSet<IpResource> temp = new TreeSet<IpResource>();
        Iterator<IpResource> thisIterator = this.iterator();
        Iterator<IpResource> thatIterator = other.iterator();
        IpResource thisResource = thisIterator.next();
        IpResource thatResource = thatIterator.next();
        while (thisResource != null && thatResource != null) {
            IpResource intersect = thisResource.intersect(thatResource);
            if (intersect != null) {
                temp.add(intersect);
            }
            int compareTo = thisResource.getEnd().compareTo(thatResource.getEnd());
            if (compareTo <= 0) {
                thisResource = thisIterator.hasNext() ? thisIterator.next() : null;
            }
            if (compareTo >= 0) {
                thatResource = thatIterator.hasNext() ? thatIterator.next() : null;
            }
        }
        this.resources = temp;
    }
}
