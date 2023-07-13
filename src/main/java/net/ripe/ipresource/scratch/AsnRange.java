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

import net.ripe.ipresource.IpResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.toUnsignedLong;
import static java.lang.Long.max;
import static java.lang.Long.min;

public final class AsnRange implements NumberResourceRange {
    private final int start;
    private final int end;

    AsnRange(long start, long end) {
        this.start = (int) start;
        this.end = (int) end;
        if (toUnsignedLong(this.start) != start) {
            throw new IllegalArgumentException("start out of bounds");
        }
        if (toUnsignedLong(this.end) != end) {
            throw new IllegalArgumentException("end out of bounds");
        }
        if (Integer.compareUnsigned(this.start, this.end) > 0) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }
    }

    private AsnRange(Asn start, Asn end) {
        this(start.longValue(), end.longValue());
    }

    public static AsnRange range(Asn start, Asn end) {
        return new AsnRange(start, end);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AsnRange that && this.start == that.start && this.end == that.end;
    }

    @Override
    public int hashCode() {
        return 'A' + 31 * 31 * Integer.hashCode(start) + 31 * Integer.hashCode(end);
    }

    @Override
    public String toString() {
        return isSingleton() ? String.valueOf(start()) : (start() + "-" + end());
    }

    @Override
    public int compareTo(@NotNull NumberResourceRange o) {
        return switch (o) {
            case AsnRange that -> {
                int rc = Integer.compareUnsigned(this.start, that.start);
                if (rc != 0) {
                    yield rc;
                }
                yield -Integer.compareUnsigned(this.end, that.end);
            }
            case IpBlock ignored -> -1;
        };
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.ASN;
    }

    @Override
    public Asn start() {
        return new Asn(start);
    }

    @Override
    public Asn end() {
        return new Asn(end);
    }

    @Override
    public boolean contains(@Nullable NumberResourceRange other) {
        return switch (other) {
            case null, IpBlock ignored -> false;
            case AsnRange that -> this.lowerBound() <= that.lowerBound() && this.upperBound() >= that.upperBound();
        };
    }

    @Override
    public boolean isSingleton() {
        return start == end;
    }

    @Override
    public @NotNull List<@NotNull NumberResourceRange> subtract(@Nullable NumberResourceRange other) {
        return switch (other) {
            case null, IpBlock ignored -> Collections.singletonList(this);
            case AsnRange that -> {
                if (other.contains(this)) {
                    yield Collections.emptyList();
                } else if (overlaps(this, that)) {
                    var result = new ArrayList<@NotNull NumberResourceRange>(2);
                    if (this.lowerBound() < that.lowerBound()) {
                        result.add(new AsnRange(this.lowerBound(), that.lowerBound() - 1));
                    }
                    if (this.upperBound() > that.upperBound()) {
                        result.add(new AsnRange(that.upperBound() + 1, this.upperBound()));
                    }
                    yield result;
                } else {
                    yield Collections.singletonList(this);
                }
            }
        };
    }

    public static @Nullable AsnRange intersection(@Nullable AsnRange a, @Nullable AsnRange b) {
        long start = max(a.lowerBound(), b.lowerBound());
        long end = min(a.upperBound(), b.upperBound());
        return start <= end ? new AsnRange(start, end) : null;
    }

    public static boolean overlaps(@Nullable AsnRange a, @Nullable AsnRange b) {
        return a != null
            && b != null
            && a.lowerBound() <= b.upperBound()
            && a.upperBound() >= b.lowerBound();
    }

    public static @Nullable AsnRange merge(@Nullable AsnRange a, @Nullable AsnRange b) {
        if (!mergeable(a, b)) {
            return null;
        } else {
            return new AsnRange(
                min(a.lowerBound(), b.lowerBound()),
                max(a.upperBound(), b.upperBound())
            );
        }
    }

    public static boolean mergeable(@Nullable AsnRange a, @Nullable AsnRange b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.lowerBound() <= b.upperBound()) {
            return a.upperBound() >= b.lowerBound() || Math.abs(a.upperBound() - b.lowerBound()) == 1;
        } else {
            return Math.abs(a.lowerBound() - b.upperBound()) == 1;
        }
    }

    long lowerBound() {
        return toUnsignedLong(start);
    }

    long upperBound() {
        return toUnsignedLong(end);
    }
}
