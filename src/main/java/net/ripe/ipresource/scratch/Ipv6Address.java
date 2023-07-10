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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class Ipv6Address implements IpAddress {
    public static final int NUMBER_OF_BITS = 128;
    public static final Ipv6Address LOWEST = new Ipv6Address(0, 0);
    public static final Ipv6Address HIGHEST = new Ipv6Address(-1, -1);

    private static final BigInteger IPV6_MAX = BigInteger.ONE.shiftLeft(NUMBER_OF_BITS).subtract(BigInteger.ONE);
    private static final BigInteger MASK_64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    public static final String COLON = ":";
    public static final int MASK_16 = 0xffff;

    final long hi, lo;

    Ipv6Address(long hi, long lo) {
        this.hi = hi;
        this.lo = lo;
    }

    Ipv6Address(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(IPV6_MAX) > 0) {
            throw new ArithmeticException("IPv6 address value out of bounds");
        }
        this.hi = value.shiftRight(64).and(MASK_64).longValue();
        this.lo = value.and(MASK_64).longValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv6Address that && this.hi == that.hi && this.lo == that.lo;
    }

    @Override
    public int hashCode() {
        return '6' + 31 * 31 * Long.hashCode(hi) + 31 * Long.hashCode(lo);
    }

    @Override
    public String toString() {
        long[] parts = new long[8];
        parts[0] = (hi >> 48) & MASK_16;
        parts[1] = (hi >> 32) & MASK_16;
        parts[2] = (hi >> 16) & MASK_16;
        parts[3] = (hi >> 0) & MASK_16;
        parts[4] = (lo >> 48) & MASK_16;
        parts[5] = (lo >> 32) & MASK_16;
        parts[6] = (lo >> 16) & MASK_16;
        parts[7] = (lo >> 0) & MASK_16;
        String[] formatted = new String[parts.length];

        for (int i = 0; i < parts.length; ++i) {
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
            return String.join(COLON, formatted);
        } else {
            String init = StringUtils.join(formatted, COLON, 0, maxZeroPartsStart);
            String tail = StringUtils.join(formatted, COLON, maxZeroPartsStart + maxZeroPartsLength, formatted.length);
            return init + "::" + tail;
        }
    }


    @Override
    public int compareTo(@NotNull NumberResource o) {
        return switch (o) {
            case Asn ignored -> 1;
            case Ipv4Address ignored -> 1;
            case Ipv6Address that -> {
                var rc = Long.compareUnsigned(this.hi, that.hi);
                if (rc != 0) {
                    yield rc;
                }
                yield Long.compareUnsigned(this.lo, that.lo);
            }
        };
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv6;
    }

    @Override
    public @NotNull Ipv6Address predecessorOrFirst() {
        if (hi == 0 && lo == 0) {
            return this;
        } else if (lo > 0) {
            return new Ipv6Address(hi, lo - 1);
        } else {
            return new Ipv6Address(hi - 1, -1);
        }
    }

    @Override
    public @NotNull Ipv6Address successorOrLast() {
        if (hi == -1 && lo == -1) {
            return this;
        } else if (lo == -1) {
            return new Ipv6Address(hi + 1, 0);
        } else {
            return new Ipv6Address(hi, lo + 1);
        }
    }

    public Ipv6Address min(Ipv6Address that) {
        return this.compareTo(that) <= 0 ? this : that;
    }

    public Ipv6Address max(Ipv6Address that) {
        return this.compareTo(that) >= 0 ? this : that;
    }

    public static Ipv6Address parse(String s) {
        var x = net.ripe.ipresource.Ipv6Address.parse(s);
        return new Ipv6Address(x.getValue());
    }

    public static Ipv6Address of(BigInteger value) {
        return new Ipv6Address(value);
    }

    public BigInteger getValue() {
        return BigInteger.valueOf(hi).and(MASK_64).shiftLeft(64).add(BigInteger.valueOf(lo).and(MASK_64));
    }

    public Ipv6Address getCommonPrefix(Ipv6Address that) {
        int length = Ipv6Block.getCommonPrefixLength(this, that);
        return Ipv6Prefix.lowerBoundForPrefix(this, length);
    }
}
