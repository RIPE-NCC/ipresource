package net.ripe.ipresource;

import java.math.BigInteger;

import org.apache.commons.lang.Validate;

public abstract class UniqueIpResource extends IpResource {

    private static final long serialVersionUID = 1L;

    private IpResourceType type;
    
    protected UniqueIpResource(IpResourceType type) {
        Validate.notNull(type, "resource type not null");
        this.type = type;
    }

    public abstract BigInteger getValue();
    
    public abstract int getCommonPrefixLength(UniqueIpResource other);

    public abstract IpAddress lowerBoundForPrefix(int prefixLength);

    public abstract IpAddress upperBoundForPrefix(int prefixLength);

    @Override
    public final IpResourceType getType() {
        return type;
    }

    @Override
    public final UniqueIpResource getStart() {
        return this;
    }

    @Override
    public final UniqueIpResource getEnd() {
        return this;
    }

    public final UniqueIpResource min(UniqueIpResource other) {
        return compareTo(other) < 0 ? this : other;
    }

    public final UniqueIpResource max(UniqueIpResource other) {
        return compareTo(other) >= 0 ? this : other;
    }

    protected boolean adjacent(UniqueIpResource other) {
        return getType() == other.getType() && getValue().subtract(other.getValue()).abs().equals(BigInteger.ONE);
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof UniqueIpResource) {
            throw new IllegalStateException("should be overriden by subclass");
        } else if (obj instanceof IpResourceRange) {
            return upTo(this).compareTo(obj);
        } else {
            throw new IllegalArgumentException("not a valid resource type: " + obj);
        }
    }

    @Override
    public final UniqueIpResource unique() {
        return this;
    }

    public final IpResourceRange upTo(UniqueIpResource end) {
        return IpResourceRange.range(this, end);
    }

    public UniqueIpResource predecessor() {
        return type.fromBigInteger(getValue().subtract(BigInteger.ONE));
    }

    public UniqueIpResource successor() {
        return type.fromBigInteger(getValue().add(BigInteger.ONE));
    }

    // Parsing.

    public static UniqueIpResource parse(String s) {
        try {
            try {
                return Ipv4Address.parse(s);
            } catch (IllegalArgumentException ex4) {
            	try {
            		return Ipv6Address.parse(s);
            	} catch (IllegalArgumentException ex6) {
            		return Asn.parse(s);
            	}
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("illegal number resource: %s (%s)", s, e.getMessage()));
        }
    }

}
