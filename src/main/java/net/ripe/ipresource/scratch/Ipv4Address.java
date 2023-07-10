/**
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
package net.ripe.ipresource.scratch;

import net.ripe.ipresource.IpResourceType;
import org.jetbrains.annotations.NotNull;

public final class Ipv4Address implements IpAddress {
    public static final int NUMBER_OF_BITS = 32;

    final int value;

    Ipv4Address(int value) {
        this.value = value;
    }

    Ipv4Address(long value) {
        this.value = (int) value;
        if (Integer.toUnsignedLong(this.value) != value) {
            throw new ArithmeticException("IPv4 address value out of bounds");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return switch (obj) {
            case Ipv4Address that ->
                this.value == that.value;
            default ->
                false;
        };
    }

    @Override
    public int hashCode() {
        return '4' + 31 * Integer.hashCode(value);
    }

    @Override
    public String toString() {
        long v = longValue();
        return ((v >> 24) & 0xff) + "." + ((v >> 16) & 0xff) + "." + ((v >> 8) & 0xff) + "." + (v & 0xff);
    }

    @Override
    public int compareTo(@NotNull NumberResource o) {
        return switch (o) {
            case Asn ignored -> 1;
            case Ipv4Address that -> Integer.compareUnsigned(this.value, that.value);
            case Ipv6Address ignored -> -1;
        };
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv4;
    }

    @Override
    public @NotNull Ipv4Address predecessorOrFirst() {
        return value == 0 ? this : new Ipv4Address(value - 1);
    }

    @Override
    public @NotNull Ipv4Address successorOrLast() {
        return value == -1 ? this : new Ipv4Address(value + 1);
    }

    public static Ipv4Address parse(String s) {
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
                octetCount++;
                if (octetCount > 4) {
                    throw new IllegalArgumentException("invalid IPv4 address: " + s);
                }

                value = addOctet(value, octet);

                octet = 0;
            } else {
                throw new IllegalArgumentException("invalid IPv4 address: " + s);
            }
        }

        value = addOctet(value, octet);

        if (octetCount != 4) {
            throw new IllegalArgumentException("invalid IPv4 address: " + s);
        }

        return new Ipv4Address(value);
    }

    private static long addOctet(long value, int octet) {
        if (octet < 0 || octet > 255) {
            throw new IllegalArgumentException("value of octet not in range 0..255: " + octet);
        }
        return 256 * value + octet;
    }

    public static Ipv4Address of(long value) {
        return new Ipv4Address(value);
    }

    public long longValue() {
        return Integer.toUnsignedLong(this.value);
    }

}
