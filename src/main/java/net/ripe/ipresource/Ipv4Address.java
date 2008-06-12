package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

@SuppressWarnings("serial")
public class Ipv4Address extends IpAddress {

	/**
	 * The regex format of an IPv4 address.
	 */
	private static final String IPV4_FORMAT = "([0-9]{1,3})(\\.([0-9]{1,3}))?(\\.([0-9]{1,3}))?(\\.([0-9]{1,3}))?";

	private static final BigInteger BYTE_MASK = BigInteger.valueOf(255);

	public static final int NUMBER_OF_BITS = 32;

	public Ipv4Address(BigInteger address) {
		super(IpResourceType.IPv4, address);
	}

	public static Ipv4Address parse(String s) {
	    return parse(s, false);
	}
	
	public static Ipv4Address parse(String s, boolean defaultMissingOctets) {
        Pattern ipv4 = Pattern.compile(IPV4_FORMAT);
        Matcher m = ipv4.matcher(s);
        Validate.isTrue(m.matches(), "invalid IPv4 address: " + s);
        Validate.isTrue(defaultMissingOctets || m.groupCount() == 7, "invalid IPv4 address: " + s);
        return new Ipv4Address(new BigInteger(1, new byte[] {
                parseByte(m.group(1)), parseByte(m.group(3)),
                parseByte(m.group(5)), parseByte(m.group(7)) }));
	}

    public String toString(boolean defaultMissingOctets) {
        BigInteger[] octets = {
            getValue().shiftRight(24),
            getValue().shiftRight(16).and(BYTE_MASK), 
            getValue().shiftRight(8).and(BYTE_MASK), 
            getValue().and(BYTE_MASK)
        };
        
        if (defaultMissingOctets && BigInteger.ZERO.equals(octets[1]) && BigInteger.ZERO.equals(octets[2]) && BigInteger.ZERO.equals(octets[3])) {
            return String.format("%d", octets[0]);
        } else if (defaultMissingOctets && BigInteger.ZERO.equals(octets[2]) && BigInteger.ZERO.equals(octets[3])) {
            return String.format("%d.%d", octets[0], octets[1]);
        } else if (defaultMissingOctets && BigInteger.ZERO.equals(octets[3])) {
            return String.format("%d.%d.%d", octets[0], octets[1], octets[2]);
        } else {
            return String.format("%d.%d.%d.%d", octets[0], octets[1], octets[2], octets[3]);
        }
    }

    private static byte parseByte(String s) {
        if (s == null) {
            return 0;
        } else {
            int value = Integer.parseInt(s);
            Validate.isTrue(value >= 0 && value <= 255,
                    "value of byte not in range 0..255: " + value);
            return (byte) value;
        }
    }

}
