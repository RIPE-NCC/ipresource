package net.ripe.ipresource;

import java.util.Iterator;


public class InheritedIpResourceSet extends IpResourceSet {

    private static final long serialVersionUID = 1L;
    private static final InheritedIpResourceSet INSTANCE = new InheritedIpResourceSet();

    private InheritedIpResourceSet() {}
    
    public static InheritedIpResourceSet getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void add(IpResource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(IpResource resource) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(IpResourceSet other) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsType(IpResourceType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public Iterator<IpResource> iterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(IpResource prefix) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void retainAll(IpResourceSet other) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "INHERITED";
    }

    public static IpResourceSet parse(String s) {
        if ("INHERITED".equals(s)) {
            return INSTANCE;
        } else {
            return IpResourceSet.parse(s);
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof InheritedIpResourceSet;
   }
}
