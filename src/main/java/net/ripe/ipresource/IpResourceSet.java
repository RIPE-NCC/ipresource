package net.ripe.ipresource;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class IpResourceSet implements Iterable<IpResource>, Serializable {

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
    
    public void addAll(IpResourceSet ipResourceSet) {
    	for (IpResource ipResource: ipResourceSet.resources) {
    		add(ipResource);
    	}
    	normalize();
    }

    public void add(IpResource resource) {
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

    public void removeAll(IpResourceSet resources) {
        for (IpResource resource: resources) {
            remove(resource);
        }
    }
}
