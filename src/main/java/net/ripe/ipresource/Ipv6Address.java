/*
 * The BSD License
 *
 * Copyright (c) 2010-2023 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.ipresource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * Ipv6 address. This implementation has no support for interfaces.
 */
public class Ipv6Address extends IpAddress {

    private static final long serialVersionUID = 2L;

    /* Pattern to match IPv6 addresses in forms defined in http://www.ietf.org/rfc/rfc4291.txt */
    private static final Pattern IPV6_PATTERN = Pattern.compile("(([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))");
    private static final int COLON_COUNT_FOR_EMBEDDED_IPV4 = 6;
    private static final int COLON_COUNT_IPV6 = 7;
    private static final String COLON = ":";

    /**
     * Mask for 16 bits, which is the length of one part of an IPv6 address.
     */
    private final BigInteger PART_MASK = BigInteger.valueOf(0xffff);

    private final BigInteger value;

    public Ipv6Address(BigInteger value) {
        this.value = value;
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv6;
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof Ipv6Address) {
            Ipv6Address that = (Ipv6Address) obj;
            return this.getValue().compareTo(that.getValue());
        } else {
            return super.doCompareTo(obj);
        }
    }

    @Override
    protected boolean adjacent(UniqueIpResource other) {
        return other instanceof Ipv6Address && getValue().subtract(other.getValue()).abs().equals(BigInteger.ONE);
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        BigInteger temp = this.getValue().xor(other.getValue());
        return getType().getBitSize() - temp.bitLength();
    }

    @Override
    public Ipv6Address lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = bitMask(0, getType()).xor(bitMask(prefixLength, getType()));
        return new Ipv6Address(this.getValue().and(mask));
    }

    @Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        return new Ipv6Address(this.getValue().or(bitMask(prefixLength, getType())));
    }

    public static Ipv6Address parse(String ipAddressString) {
        Validate.notNull(ipAddressString);
        ipAddressString = ipAddressString.trim();
        Validate.isTrue(IPV6_PATTERN.matcher(ipAddressString).matches(), "Invalid IPv6 address: " + ipAddressString);

        ipAddressString = expandMissingColons(ipAddressString);
        if (isInIpv4EmbeddedIpv6Format(ipAddressString)) {
            ipAddressString = getIpv6AddressWithIpv4SectionInIpv6Notation(ipAddressString);
        }
        return new Ipv6Address(ipv6StringtoBigInteger(ipAddressString));
    }

    private static String expandMissingColons(String ipAddressString) {
        int colonCount = isInIpv4EmbeddedIpv6Format(ipAddressString) ? COLON_COUNT_FOR_EMBEDDED_IPV4 : COLON_COUNT_IPV6;
        return ipAddressString.replace("::", StringUtils.repeat(":", colonCount - StringUtils.countMatches(ipAddressString, ":") + 2));
    }

    private static boolean isInIpv4EmbeddedIpv6Format(String ipAddressString) {
        return ipAddressString.contains(".");
    }

    private static String getIpv6AddressWithIpv4SectionInIpv6Notation(String ipAddressString) {
        String ipv6Section = StringUtils.substringBeforeLast(ipAddressString, ":");
        String ipv4Section = StringUtils.substringAfterLast(ipAddressString, ":");
        try {
            long ipv4value = Ipv4Address.parse(ipv4Section).longValue();
            return ipv6Section + ":" +
                Long.toString(ipv4value >>> 16, 16) + ":" +
                Long.toString(ipv4value & 0xffff, 16);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Embedded Ipv4 in IPv6 address is invalid: " + ipAddressString, e);
        }
    }

    /**
     * Converts a fully expanded IPv6 string to a BigInteger
     *
     * @param ipAddressString Fully expanded address (i.e. no '::' shortcut)
     * @return Address as BigInteger
     */
    private static BigInteger ipv6StringtoBigInteger(String ipAddressString) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        int groupValue = 0;
        for (int i = 0; i < ipAddressString.length(); i++) {
            final char c = ipAddressString.charAt(i);
            if (c == ':') {
                byteBuffer.putShort((short) groupValue);
                groupValue = 0;
            } else {
                groupValue = (groupValue << 4) + Character.digit(c, 16);
            }
        }
        byteBuffer.putShort((short) groupValue);
        return new BigInteger(1, byteBuffer.array());
    }

    @Override
    public String toString(boolean defaultMissingOctets) {
        long[] parts = new long[8];
        String[] formatted = new String[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            parts[i] = getValue().shiftRight((7 - i) * 16).and(PART_MASK).longValue();
            formatted[i] = Long.toHexString(parts[i]);
        }

        // Find longest sequence of zeroes. Use the first one if there are
        // multiple sequences of zeroes with the same length.
        int currentZeroPartsLength = 0;
        int currentZeroPartsStart = 0;
        int maxZeroPartsLength = 0;
        int maxZeroPartsStart = 0;
        for (int i = 0; i < parts.length; ++i) {
            if (parts[i] == 0) {
                if (currentZeroPartsLength == 0) {
                    currentZeroPartsStart = i;
                }
                ++currentZeroPartsLength;
                if (currentZeroPartsLength > maxZeroPartsLength) {
                    maxZeroPartsLength = currentZeroPartsLength;
                    maxZeroPartsStart = currentZeroPartsStart;
                }
            } else {
                currentZeroPartsLength = 0;
            }
        }

        if (maxZeroPartsLength <= 1) {
            return StringUtils.join(formatted, COLON);
        } else {
            String init = StringUtils.join(formatted, COLON, 0, maxZeroPartsStart);
            String tail = StringUtils.join(formatted, COLON, maxZeroPartsStart + maxZeroPartsLength, formatted.length);
            return init + "::" + tail;
        }
    }

    @Override
    public final BigInteger getValue() {
        return value;
    }

    @Override
    public boolean isValidNetmask() {
        int bitLength = value.bitLength();
        if (bitLength < IpResourceType.IPv6.getBitSize()) {
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
}
