/**
 * The BSD License
 *
 * Copyright (c) 2010, 2011 RIPE NCC
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;

import org.apache.commons.lang.Validate;

public class Ipv4Address extends IpAddress {

	private static final long serialVersionUID = 2L;

	private static final int BYTE_MASK = 0xff;

	public static final int NUMBER_OF_BITS = 32;

	private static final long MINIMUM_VALUE = 0;
	private static final long MAXIMUM_VALUE = (1L << NUMBER_OF_BITS) - 1;

    // Int is more memory efficient, so use longValue() accessor to get correct unsigned long value.
    private int intValue;

    @Deprecated
    public Ipv4Address(BigInteger value) {
        this(value.longValue());
    }

	public Ipv4Address(long value) {
		Validate.isTrue(value >= MINIMUM_VALUE && value <= MAXIMUM_VALUE, "value out of range");
		this.intValue = (int) value;
	}

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv4;
    }

	public static Ipv4Address parse(String s) {
	    return parse(s, false);
	}

	public static Ipv4Address parse(String s, boolean defaultMissingOctets) {
        s = s.trim();

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

        return new Ipv4Address(value);
	}

    private static long addOctet(long value, int octet) {
        if (octet < 0 || octet > 255) {
            throw new IllegalArgumentException("value of octet not in range 0..255: " + octet);
        }
        value = ((value) << 8) | octet;
        return value;
    }

    @Override
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

    public long longValue() {
        return value();
    }

    @Override
    protected int doHashCode() {
        return intValue;
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof Ipv4Address) {
            long otherValue = ((Ipv4Address) obj).value();
            if (value() < otherValue) {
                return -1;
            } else if (value() > otherValue) {
                return +1;
            } else {
                return 0;
            }
        } else {
            return super.doCompareTo(obj);
        }
    }

    @Override
    public final BigInteger getValue() {
        return BigInteger.valueOf(value());
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        long temp = value() ^ ((Ipv4Address) other).value();
        return Integer.numberOfLeadingZeros((int) temp);
    }

    @Override
    public Ipv4Address lowerBoundForPrefix(int prefixLength) {
        long mask = ~((1L << (NUMBER_OF_BITS - prefixLength)) -  1);
        return new Ipv4Address(value() & mask);
    }

    @Override
    public Ipv4Address upperBoundForPrefix(int prefixLength) {
        long mask = (1L << (NUMBER_OF_BITS - prefixLength)) -  1;
        return new Ipv4Address(value() | mask);
    }

    @Override
    public boolean isValidNetmask() {
        int leadingOnesCount = Integer.numberOfLeadingZeros(~(int) value());
        int trailingZeroesCount = Integer.numberOfTrailingZeros((int) value());
        return leadingOnesCount > 0 && (leadingOnesCount + trailingZeroesCount) == NUMBER_OF_BITS;
    }

    private long value() {
        return intValue & MAXIMUM_VALUE;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        if (!gf.defaulted("intValue"))
            this.intValue = gf.get("intValue", 0);
        else
            this.intValue = (int) gf.get("value", 0L);
    }
}
