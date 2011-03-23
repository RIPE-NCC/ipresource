package net.ripe.ipresource;

import java.io.Serializable;
import java.math.BigInteger;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Resource implements Serializable, Comparable<Resource> {
    
    private static final long serialVersionUID = 1L;
    
    protected final BigInteger value;
    
    protected Resource(BigInteger value) {
        this.value = value;
    }
    
    public static Resource parse(String string) {
        if (string.indexOf(':') > -1) {
            return Ipv6Address.parse(string);
        }
        if (string.indexOf('/') > -1 || string.indexOf('.') > -1) {
            return Ipv4Address.parse(string);
        }
        return Asn.parse(string);
    }
    
    @Override
    public abstract int compareTo(Resource other);

    @Override
    public final int hashCode() {
        return new HashCodeBuilder().append(value).toHashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        return compareTo(other) == 0;
    }

}