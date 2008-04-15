package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
	
	/**
	 * Get the IPv4 address as an array of 0 or 1 int values representing all the bits
	 * starting with the most significant bit.
	 * @return
	 */
	public List<Integer> toBitArray(int bitLength) {
		ArrayList<Integer> bits = new ArrayList<Integer>();
		for (int i=bitLength-1; i>=0; i--) {
			if (value.testBit(i)) {
				bits.add(new Integer("1"));
			} else {
				bits.add(new Integer("0"));
			}
		}
		return bits;	
	}
	
	/**
	 * Returns the position of the least significant '1' for IPv4 address;
	 * returns -1 if there is no '1'; i.e. for 0.0.0.0
	 * @return
	 */
	public int getLeastSignificantOne() {
		return getLeastSignificant(true);
	}
	
	/**
	 * Returns the position of the least significant '0' for IPv4 address;
	 * returns -1 if there is no '0'; i.e. for 255.255.255.255
	 * @return
	 */
	public int getLeastSignificantZero() {
		return getLeastSignificant(false);
	}
	
	private int getLeastSignificant(boolean bit) {
		int leastSignificantOne = 0;
		boolean notFound = true;
		for (int i = 0; i < value.bitLength() && notFound; i++) {
			if (value.testBit(i) == bit) {
				notFound = false;
				leastSignificantOne = i;
			}
		}
		if (notFound) { leastSignificantOne = -1; }
		return leastSignificantOne;		
	}
	
	public abstract IpAddress stripLeastSignificantOnes();
	
	
}
