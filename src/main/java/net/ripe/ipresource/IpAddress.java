package net.ripe.ipresource;

import java.math.BigInteger;

import org.apache.commons.lang.Validate;

public abstract class IpAddress extends UniqueIpResource {

    private static final long serialVersionUID = 1L;

    protected IpAddress(IpResourceType type, BigInteger value) {
        super(type, value);
    }

    public static IpAddress parse(String s) {
        return parse(s, false);
    }
    
    public static IpAddress parse(String s, boolean defaultMissingOctets) {
        try {
            try {
    			return Ipv4Address.parse(s, defaultMissingOctets);
    		} catch (IllegalArgumentException e) {
    			return Ipv6Address.parse(s);
    		}
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("illegal IP address: %s (%s)", s, e.getMessage()));
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
	
	protected IpAddress createOfSameType(BigInteger value) {
	    return (IpAddress) getType().fromBigInteger(value);
	}
	
	/**
	 * Returns the position of the least significant '1' for an IP address;
	 * returns {@link IpResourceType#getBitSize()} if there is no '1'; i.e. for 0.0.0.0
	 * @return
	 */
	public int getLeastSignificantOne() {
		return getLeastSignificant(true);
	}
	
	/**
	 * Returns the position of the least significant '0' for an IP address;
	 * returns {@link IpResourceType#getBitSize()} if there is no '0'; i.e. for 255.255.255.255
	 * @return
	 */
	public int getLeastSignificantZero() {
		return getLeastSignificant(false);
	}
	
	private int getLeastSignificant(boolean bit) {
		for (int i = 0; i < getType().getBitSize(); i++) {
			if (value.testBit(i) == bit) {
			    return i;
			}
		}
		return getType().getBitSize();
	}
	
	public IpAddress stripLeastSignificantOnes() {
	        int leastSignificantZero = getLeastSignificantZero();
	        return createOfSameType(value.shiftRight(leastSignificantZero).shiftLeft(leastSignificantZero));
	}
	
	@Override
	public String toString() {
	    return toString(false);
	}
	
	public abstract String toString(boolean defaultMissingOctets);
}
