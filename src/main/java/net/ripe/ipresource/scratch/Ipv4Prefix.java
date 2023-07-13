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

public final class Ipv4Prefix extends Ipv4Block implements IpPrefix {
    private final int prefix;
    private final byte length;

    Ipv4Prefix(long prefix, int length) {
        this.prefix = (int) prefix;
        this.length = (byte) length;
        if (Integer.toUnsignedLong(this.prefix) != prefix) {
            throw new IllegalArgumentException("IPv4 prefix out of bounds");
        }
        if (Byte.toUnsignedInt(this.length) != length || length > Ipv4Address.NUMBER_OF_BITS) {
            throw new IllegalArgumentException("IPv4 prefix length out of bounds");
        }
        if (lowerBoundForPrefix(prefix, length) != prefix) {
            throw new IllegalArgumentException("not a proper IPv4 prefix");
        }
    }

    public static Ipv4Prefix prefix(long prefix, int length) {
        return new Ipv4Prefix(prefix, length);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv4Prefix that &&
            this.prefix == that.prefix && this.length == that.length;
    }

    @Override
    public int hashCode() {
        return '4' + 31 * 31 * Integer.hashCode(prefix) + 31 * Byte.hashCode(length);
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
            case Ipv4Prefix that -> {
                int rc = Integer.compareUnsigned(this.prefix, that.prefix);
                if (rc != 0) {
                    yield rc;
                }
                yield Byte.compareUnsigned(this.length, that.length);
            }
            case Ipv4Range that -> {
                int rc = Long.compare(this.lowerBound(), that.lowerBound());
                if (rc != 0) {
                    yield rc;
                }
                yield -Long.compare(this.upperBound(), that.upperBound());
            }
            case Ipv6Prefix ignored -> -1;
            case Ipv6Range ignored -> -1;
        };
    }

    @Override
    public @NotNull Ipv4Address start() {
        return prefix();
    }

    @Override
    public @NotNull Ipv4Address end() {
        return new Ipv4Address(upperBoundForPrefix(Integer.toUnsignedLong(prefix), length()));
    }

    @Override
    public boolean isSingleton() {
        return length() == Ipv4Address.NUMBER_OF_BITS;
    }

    public @NotNull Ipv4Address prefix() {
        return new Ipv4Address(Integer.toUnsignedLong(prefix));
    }

    public int length() {
        return Byte.toUnsignedInt(this.length);
    }

    long lowerBound() {
        return Integer.toUnsignedLong(prefix);
    }

    long upperBound() {
        return upperBoundForPrefix(Integer.toUnsignedLong(prefix), length());
    }

    static long lowerBoundForPrefix(long prefix, int prefixLength) {
        long mask = -(1L << (Ipv4Address.NUMBER_OF_BITS - prefixLength));
        return prefix & mask;
    }

    static long upperBoundForPrefix(long prefix, int prefixLength) {
        long mask = (1L << (Ipv4Address.NUMBER_OF_BITS - prefixLength)) - 1;
        return prefix | mask;
    }
}
