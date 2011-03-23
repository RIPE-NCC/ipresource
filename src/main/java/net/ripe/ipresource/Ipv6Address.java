package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;

/**
 * Ipv6 address. This implementation has no support for interfaces.
 */
public class Ipv6Address extends IpAddress {

    private static final long serialVersionUID = 2L;

    /**
     * Mask for 16 bits, which is the length of one part of an IPv6 address.
     */
    private BigInteger PART_MASK = BigInteger.valueOf(0xffff);

    public Ipv6Address(BigInteger value) {
        super(value);
    }

    public int getCommonPrefixLength(Ipv6Address other) {
        BigInteger temp = value.xor(other.value);
        return getBitSize() - temp.bitLength();
    }

    public Ipv6Address lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = bitMask(0).xor(bitMask(prefixLength));
        return new Ipv6Address(value.and(mask));
    }

    public Ipv6Address upperBoundForPrefix(int prefixLength) {
        return new Ipv6Address(value.or(bitMask(prefixLength)));
    }

    public static Ipv6Address parse(String ipAddressString) {
        if (ipAddressString != null) {
            ipAddressString = ipAddressString.trim();
        }

        Validate.isTrue(Pattern.matches("[0-9a-fA-F]{0,4}:([0-9a-fA-F]{0,4}:){1,6}[0-9a-fA-F]{0,4}", ipAddressString), "Invalid IPv6 address: " + ipAddressString);

        // Count number of colons: must be between 2 and 7
        int colonCount = countColons(ipAddressString);
        int doubleColonCount = numberOfDoubleColons(ipAddressString);

        // The number of double colons must be exactly one if there's a missing colon.
        // The double colon will be the place that gets filled out to complete the address for easy parsing.
        if (colonCount < 7) {
            Validate.isTrue(doubleColonCount == 1, "May only be one double colon in an IPv6 address");

            // Add extra colons
            ipAddressString = expandColons(ipAddressString);
        }

        // By now we have an IPv6 address that's guaranteed to have 7 colons.

        return new Ipv6Address(ipv6StringtoBigInteger(ipAddressString));
    }

    public Ipv6Address getCommonPrefix(Ipv6Address other) {
        return lowerBoundForPrefix(getCommonPrefixLength(other));
    }
    
    public Ipv6Address stripLeastSignificantOnes() {
        int leastSignificantZero = getLeastSignificantZero();
        return new Ipv6Address(value.shiftRight(leastSignificantZero).shiftLeft(leastSignificantZero));
    }
    
    /**
     * Converts a fully expanded IPv6 string to a BigInteger
     *
     * @param Fully expanded address (i.e. no '::' shortcut)
     * @return Address as BigInteger
     */
    private static BigInteger ipv6StringtoBigInteger(String ipAddressString) {
        Pattern p = Pattern.compile("([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4})");
        Matcher m = p.matcher(ipAddressString);
        m.find();

        String ipv6Number = "";
        for (int i = 1; i <= m.groupCount(); i++) {
            String part = m.group(i);
            String padding = "0000".substring(0, 4 - part.length());
            ipv6Number = ipv6Number + padding + part;
        }

        return new BigInteger(ipv6Number, 16);
    }

    @Override
    public String toString(boolean defaultMissingOctets) {
        long[] list = new long[8];
        int currentZeroLength = 0;
        int maxZeroLength = 0;
        int maxZeroIndex = 0;
        for (int i = 7; i >= 0; i--) {
            list[i] = value.shiftRight(i*16).and(PART_MASK).longValue();

            if (list[i] == 0) {
                currentZeroLength ++;
            } else {
                if (currentZeroLength > maxZeroLength) {
                    maxZeroIndex = i + currentZeroLength;
                    maxZeroLength = currentZeroLength;
                }
                currentZeroLength = 0;
            }
        }
        if (currentZeroLength > maxZeroLength) {
            maxZeroIndex = -1 + currentZeroLength;
            maxZeroLength = currentZeroLength;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            if (i == maxZeroIndex && maxZeroLength > 1) {
                if (i == 7) {
                    sb.append(':');
                }
                i -= (maxZeroLength - 1);
            } else {
                sb.append(String.format("%x", list[i]));
            }
            sb.append(':');
        }
        if ( (maxZeroIndex - maxZeroLength + 1) != 0) {
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }


    // -------------------------------------------------------------------------------- HELPERS
    
    private BigInteger bitMask(int prefixLength) {
        final BigInteger MINUS_ONE = new BigInteger("-1");
        return BigInteger.ONE.shiftLeft(getBitSize() - prefixLength).add(MINUS_ONE);
    }

    public boolean isValidNetmask() {
        int bitLength = value.bitLength();
        if (bitLength < getBitSize()) {
            return false;
        }

        int lowestSetBit = value.getLowestSetBit();
        for (int i = bitLength - 1; i >= lowestSetBit; --i) {
            if (!value.testBit(i)) {
                return false;
            }
        }

        return true;
    }

    private static String expandColons(String ipv6String) {
        String filledDoubleColons = ":::::::".substring(0, 7 - countColons(ipv6String) + 2);
        ipv6String = ipv6String.replace("::", filledDoubleColons);
        return ipv6String;
    }

    private static int countColons(String ipv6String) {
        Pattern colonPattern = Pattern.compile(":");
        Matcher colonMatcher = colonPattern.matcher(ipv6String);
        int colonCount = 0;
        while (colonMatcher.find()) { colonCount++ ; };
        return colonCount;
    }

    private static int numberOfDoubleColons(String ipv6String) {
        // Count number of double colons: should be either 0 (with 7 colons) or 1 (with less)
        Pattern doubleColonPattern = Pattern.compile("::");
        Matcher doubleColonMatcher = doubleColonPattern.matcher(ipv6String);
        int doubleColonCount = 0;
        while (doubleColonMatcher.find()) { doubleColonCount++ ; };
        return doubleColonCount;
    }

    @Override
    protected int getBitSize() {
        return 128;
    }

    @Override
    public int compareTo(Resource other) {
        if (other instanceof Ipv6Address) {
            return value.compareTo(other.value);
        }
        return 1;
    }
    
    public Ipv6Address successor() {
        return new Ipv6Address(value.add(BigInteger.ONE));
    }

    public Ipv6Address predecessor() {
        return new Ipv6Address(value.subtract(BigInteger.ONE));
    }
}
