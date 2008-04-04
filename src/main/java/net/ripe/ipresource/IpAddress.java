package net.ripe.ipresource;

import java.math.BigInteger;

import org.apache.commons.lang.Validate;

public abstract class IpAddress extends UniqueIpResource {

    protected IpAddress(IpResourceType type, BigInteger value) {
        super(type, value);
    }

    public static IpAddress parse(String s) {
        try {
			return Ipv4Address.parse(s);
		} catch (IllegalArgumentException e) {
			return Ipv6Address.parse(s);
		}
    }

    protected static BigInteger bitMask(int prefixLength, IpResourceType type) {
    	final BigInteger MINUS_ONE = new BigInteger("-1");
        return BigInteger.ONE.shiftLeft(type.getBitSize() - prefixLength).add(MINUS_ONE);
    }
    
    public IpAddress getCommonPrefix(IpAddress other) {
        return lowerBoundForPrefix(getCommonPrefixLength(other));
    }
    
	@Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        BigInteger temp = this.getValue().xor(other.getValue());
        return getType().getBitSize() - temp.bitLength();
    }

	@Override
    public IpAddress lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = bitMask(0, getType()).xor(bitMask(prefixLength, getType()));
        return createOfSameType(this.getValue().and(mask));
    }

	@Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        return createOfSameType(this.getValue().or(bitMask(prefixLength, getType())));
    }
	
	protected abstract IpAddress createOfSameType(BigInteger value);
}
