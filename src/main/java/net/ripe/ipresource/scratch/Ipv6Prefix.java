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
package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

public final class Ipv6Prefix extends Ipv6Block implements IpPrefix {
    private final @NotNull Ipv6Address prefix;
    private final byte length;

    Ipv6Prefix(@NotNull Ipv6Address prefix, int length) {
        this.prefix = prefix;
        this.length = (byte) length;
        if (Byte.toUnsignedInt(this.length) != length || length > Ipv6Address.NUMBER_OF_BITS) {
            throw new IllegalArgumentException("IPv6 prefix length out of bounds");
        }
        if (!isPrefixLowerBound(prefix, length)) {
            throw new IllegalArgumentException("not a proper IPv6 prefix");
        }
    }

    public static Ipv6Prefix prefix(Ipv6Address prefix, int length) {
        return new Ipv6Prefix(prefix, length);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv6Prefix that &&
            this.prefix.equals(that.prefix) && this.length == that.length;
    }

    @Override
    public int hashCode() {
        return '6' + 31 * 31 * prefix.hashCode() + 31 * Byte.hashCode(length);
    }

    @Override
    public String toString() {
        if (isSingleton()) {
            return String.valueOf(prefix());
        } else {
            return prefix() + "/" + length();
        }
    }

    @Override
    public int compareTo(@NotNull NumberResourceRange o) {
        return switch (o) {
            case AsnRange ignored ->
                1;
            case Ipv4Prefix ignored -> 1;
            case Ipv4Range ignored -> 1;
            case Ipv6Prefix that -> {
                int rc = this.prefix.compareTo(that.prefix);
                if (rc != 0) {
                    yield rc;
                }
                yield Byte.compareUnsigned(this.length, that.length);
            }
            case Ipv6Range that -> {
                int rc = this.start().compareTo(that.start());
                if (rc != 0) {
                    yield rc;
                }
                yield -this.end().compareTo(that.end());
            }
        };
    }

    @Override
    public @NotNull Ipv6Address start() {
        return prefix();
    }

    @Override
    public @NotNull Ipv6Address end() {
        return upperBoundForPrefix(prefix, length());
    }

    @Override
    public boolean isSingleton() {
        return length() == Ipv6Address.NUMBER_OF_BITS;
    }

    public @NotNull Ipv6Address prefix() {
        return prefix;
    }

    public int length() {
        return Byte.toUnsignedInt(this.length);
    }

    static Ipv6Address lowerBoundForPrefix(Ipv6Address prefix, int prefixLength) {
        if (prefixLength == 0) {
            return Ipv6Address.LOWEST;
        } else if (prefixLength < 64) {
            long mask = -(1L << (Ipv6Address.NUMBER_OF_BITS - prefixLength - 64));
            return new Ipv6Address(prefix.hi & mask, 0L);
        } else if (prefixLength == 64) {
            return new Ipv6Address(prefix.hi, 0L);
        } else {
            long mask = -(1L << (Ipv6Address.NUMBER_OF_BITS - prefixLength));
            return new Ipv6Address(prefix.hi, prefix.lo & mask);
        }
    }

    static Ipv6Address upperBoundForPrefix(Ipv6Address prefix, int prefixLength) {
        if (prefixLength == 0) {
            return new Ipv6Address(-1, -1);
        } else if (prefixLength < 64) {
            long mask = (1L << (Ipv6Address.NUMBER_OF_BITS - prefixLength - 64)) - 1;
            return new Ipv6Address(prefix.hi | mask, -1L);
        } else if (prefixLength == 64) {
            return new Ipv6Address(prefix.hi, -1);
        } else {
            long mask = (1L << (Ipv6Address.NUMBER_OF_BITS - prefixLength)) - 1;
            return new Ipv6Address(prefix.hi, prefix.lo | mask);
        }
    }

}
