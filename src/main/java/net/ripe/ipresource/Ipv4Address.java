package net.ripe.ipresource;

import java.math.BigInteger;

public class Ipv4Address extends IpAddress {

	private static final long serialVersionUID = 1L;

	private static final int BYTE_MASK = 0xff;

	public static final int NUMBER_OF_BITS = 32;

	public Ipv4Address(BigInteger address) {
		super(IpResourceType.IPv4, address);
	}

	public static Ipv4Address parse(String s) {
	    return parse(s, false);
	}
	
	public static Ipv4Address parse(String s, boolean defaultMissingOctets) {
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
        long value = getValue().longValue();
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

}
