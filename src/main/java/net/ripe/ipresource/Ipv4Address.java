package net.ripe.ipresource;

import java.math.BigInteger;

public class Ipv4Address extends IpAddress {

	private static final long serialVersionUID = 2L;

	private static final int BYTE_MASK = 0xff;

	public static final int NUMBER_OF_BITS = 32;

//	private static final long MINIMUM_VALUE = 0;
//	private static final long MAXIMUM_VALUE = (1L << NUMBER_OF_BITS) - 1;
	
    public Ipv4Address(BigInteger value) {
        super(value);
    }
    
	public static Ipv4Address parse(String s) {
	    return parse(s, false);
	}
	
	public Ipv4Address getCommonPrefix(Ipv4Address other) {
        return lowerBoundForPrefix(getCommonPrefixLength(other));
    }
	
    public Ipv4Address stripLeastSignificantOnes() {
        int leastSignificantZero = getLeastSignificantZero();
        return new Ipv4Address(value.shiftRight(leastSignificantZero).shiftLeft(leastSignificantZero));
    }

	public static Ipv4Address parse(String s, boolean defaultMissingOctets) {
	    if (s != null) {
	        s = s.trim();
	    }

	    int length = s.length();
	    if (length == 0 || !Character.isDigit(s.charAt(0)) || !Character.isDigit(s.charAt(s.length() - 1))) {
            throw new IllegalArgumentException("invalid IPv4 address: " + s);
	    }

	    long value = 0;
        int octet = 0;
        int octetCount = 1;

        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                octet = octet * 10 + (ch - '0');
            } else if (ch == '.') {
                if (octetCount > 4) {
                    throw new IllegalArgumentException("invalid IPv4 address: " + s);
                }
                octetCount++;

                value = addOctet(value, octet);

                octet = 0;
            } else {
                throw new IllegalArgumentException("invalid IPv4 address: " + s);
            }
        }

        value = addOctet(value, octet);

        if (defaultMissingOctets) {
            value <<= 8 * (4 - octetCount);
        } else if (octetCount != 4) {
            throw new IllegalArgumentException("invalid IPv4 address: " + s);
        }

        return new Ipv4Address(BigInteger.valueOf(value));
	}

    private static long addOctet(long value, int octet) {
        if (octet < 0 || octet > 255) {
            throw new IllegalArgumentException("value of octet not in range 0..255: " + octet);
        }
        value = ((value) << 8) | octet;
        return value;
    }

    public String toString(boolean defaultMissingOctets) {
        long value = this.value.longValue();
        int a = (int) (value >> 24);
        int b = (int) (value >> 16) & BYTE_MASK;
        int c = (int) (value >> 8) & BYTE_MASK;
        int d = (int) value & BYTE_MASK;

        if (!defaultMissingOctets) {
            return a + "." + b + "." + c + "." + d;
        } else if (b == 0 && c == 0 && d == 0) {
            return "" + a;
        } else if (c == 0 && d == 0) {
            return a + "." + b;
        } else if (d == 0) {
            return a + "." + b + "." + c;
        } else {
            return a + "." + b + "." + c + "." + d;
        }
    }
    
    public Ipv4Address successor() {
        return new Ipv4Address(value.add(BigInteger.ONE));
    }

    public Ipv4Address predecessor() {
        return new Ipv4Address(value.subtract(BigInteger.ONE));
    }

    public int getCommonPrefixLength(Ipv4Address other) {
        long temp = value.longValue() ^ other.value.longValue();
        return Integer.numberOfLeadingZeros((int) temp);
    }

    public Ipv4Address lowerBoundForPrefix(int prefixLength) {
        long mask = ~((1L << (NUMBER_OF_BITS - prefixLength)) -  1);
        return new Ipv4Address(BigInteger.valueOf(value.longValue() & mask));
    }

    public Ipv4Address upperBoundForPrefix(int prefixLength) {
        long mask = (1L << (NUMBER_OF_BITS - prefixLength)) -  1;
        return new Ipv4Address(BigInteger.valueOf(value.longValue() | mask));
    }
    
    public boolean isValidNetmask() {
        int leadingOnesCount = Integer.numberOfLeadingZeros(~(int) value.longValue());
        int trailingZeroesCount = Integer.numberOfTrailingZeros((int) value.longValue());
        return leadingOnesCount > 0 && (leadingOnesCount + trailingZeroesCount) == NUMBER_OF_BITS;
    }

    @Override
    protected int getBitSize() {
        return 32;
    }

    @Override
    public int compareTo(Resource other) {
        if (other instanceof Ipv4Address) {
            return value.compareTo(other.value);
        }
        if (other instanceof Asn) {
            return 1;
        }
        return -1;
    }
}
