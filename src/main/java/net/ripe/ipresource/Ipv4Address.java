package net.ripe.ipresource;

import org.apache.commons.lang.Validate;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Ipv4Address extends IpAddress {

    public static final int ADDRESS_SIZE = IpResourceType.IPv4.getBitSize();

    private static final BigInteger ADDRESS_MASK = bitMask(0);
    private static final BigInteger BYTE_MASK = BigInteger.valueOf(255);

    public Ipv4Address(BigInteger address) {
        super(IpResourceType.IPv4, address);
    }

    public static Ipv4Address parse(String s) {
        Pattern ipv4 = Pattern
                .compile("([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})");
        Matcher m = ipv4.matcher(s);
        Validate.isTrue(m.matches(), "invalid IPv4 address: " + s);
        return new Ipv4Address(
                new BigInteger(1, new byte[] {parseByte(m.group(1)), parseByte(m.group(2)), parseByte(m.group(3)), parseByte(m.group(4))}));
    }

    private static byte parseByte(String s) {
        int value = Integer.parseInt(s);
        Validate.isTrue(value >= 0 && value <= 255, "value of byte not in range 0..255: " + value);
        return (byte) value;
    }

    public Ipv4Address getCommonPrefix(Ipv4Address other) {
        return lowerBoundForPrefix(getCommonPrefixLength(other));
    }

    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        BigInteger temp = this.getValue().xor(other.getValue());
        return ADDRESS_SIZE - temp.bitLength();
    }

    public Ipv4Address lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = ADDRESS_MASK.xor(bitMask(prefixLength));
        return new Ipv4Address(this.getValue().and(mask));
    }

    public Ipv4Address upperBoundForPrefix(int prefixLength) {
        return new Ipv4Address(this.getValue().or(bitMask(prefixLength)));
    }

    private static BigInteger bitMask(int prefixLength) {
        return BigInteger.valueOf((1L << (ADDRESS_SIZE - prefixLength)) - 1);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d", getValue().shiftRight(24), getValue().shiftRight(16).and(BYTE_MASK),
                getValue().shiftRight(8).and(BYTE_MASK), getValue().and(BYTE_MASK));
    }

}
