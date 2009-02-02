package net.ripe.ipresource;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.math.BigInteger;

public abstract class UniqueIpResource extends IpResource {

    private static final long serialVersionUID = 1L;

    private IpResourceType type;
    protected BigInteger value;

    // Construction.

    protected UniqueIpResource(IpResourceType type, BigInteger value) {
        Validate.notNull(type, "resource type not null");
        Validate.notNull(value, "resource value not null");
        this.type = type;
        this.value = value;
    }

    // Parsing.

    public static UniqueIpResource parse(String s) {
        try {
            return Ipv4Address.parse(s);
        } catch (IllegalArgumentException ex4) {
        	try {
        		return Ipv6Address.parse(s);
        	} catch (IllegalArgumentException ex6) {
        		return Asn.parse(s);
        	}
        }
    }

    @Override
    public final IpResourceType getType() {
        return type;
    }

    public final BigInteger getValue() {
        return value;
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
    protected final int doCompareTo(IpResource obj) {
        if (obj instanceof UniqueIpResource) {
            UniqueIpResource that = (UniqueIpResource) obj;
            return this.getValue().compareTo(that.getValue());
        } else if (obj instanceof IpResourceRange) {
            return upTo(this).compareTo(obj);
        } else {
            throw new IllegalArgumentException("not a valid resource type: " + obj);
        }
    }

    @Override
    protected int doHashCode() {
        return new HashCodeBuilder().append(type).append(value).toHashCode();
    }

    @Override
    public final UniqueIpResource unique() {
        return this;
    }

    public final IpResourceRange upTo(UniqueIpResource end) {
        return IpResourceRange.range(this, end);
    }

    public UniqueIpResource predecessor() {
        return type.fromBigInteger(value.subtract(BigInteger.ONE));
    }

    public UniqueIpResource successor() {
        return type.fromBigInteger(value.add(BigInteger.ONE));
    }

    public abstract int getCommonPrefixLength(UniqueIpResource other);

    public abstract IpAddress lowerBoundForPrefix(int prefixLength);

    public abstract IpAddress upperBoundForPrefix(int prefixLength);

}
