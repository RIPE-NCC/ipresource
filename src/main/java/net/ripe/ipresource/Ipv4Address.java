package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

public class Ipv4Address extends IpAddress {

	private static final long serialVersionUID = 1L;

	/**
	 * The regex format of an IPv4 address.
	 */
	private static final Pattern IPV4_FORMAT = Pattern.compile("([0-9]{1,3})(\\.([0-9]{1,3}))?(\\.([0-9]{1,3}))?(\\.([0-9]{1,3}))?");

	private static final int BYTE_MASK = 0xff;

	public static final int NUMBER_OF_BITS = 32;

	public Ipv4Address(BigInteger address) {
		super(IpResourceType.IPv4, address);
	}

	public static Ipv4Address parse(String s) {
	    return parse(s, false);
	}
	
	public static Ipv4Address parse(String s, boolean defaultMissingOctets) {
        Matcher m = IPV4_FORMAT.matcher(s);
        Validate.isTrue(m.matches(), "invalid IPv4 address: " + s);
        Validate.isTrue(defaultMissingOctets || m.groupCount() == 7, "invalid IPv4 address: " + s);
        return new Ipv4Address(new BigInteger(1, new byte[] {
                parseByte(m.group(1)), parseByte(m.group(3)),
                parseByte(m.group(5)), parseByte(m.group(7)) }));
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

    private static byte parseByte(String s) {
        if (s == null) {
            return 0;
        } else {
            int value = Integer.parseInt(s);
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("value of byte not in range 0..255: " + value);
            }
            return (byte) value;
        }
    }

}
