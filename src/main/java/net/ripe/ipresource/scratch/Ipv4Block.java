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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Long.max;
import static java.lang.Long.min;
import static net.ripe.ipresource.scratch.Ipv4Prefix.lowerBoundForPrefix;
import static net.ripe.ipresource.scratch.Ipv4Prefix.upperBoundForPrefix;

public sealed abstract class Ipv4Block implements IpBlock permits Ipv4Prefix, Ipv4Range {
    public abstract @NotNull Ipv4Address start();
    public abstract @NotNull Ipv4Address end();

    public static @NotNull Ipv4Block of(@NotNull Ipv4Address start, @NotNull Ipv4Address end) {
        return of(start.longValue(), end.longValue());
    }

    @NotNull
    private static Ipv4Block of(long start, long end) {
        if (isLegalPrefix(start, end)) {
            long temp = start ^ end;
            int length = Integer.numberOfLeadingZeros((int) temp);
            return Ipv4Prefix.prefix(start, length);
        } else {
            return new Ipv4Range(start, end);
        }
    }

    @Override
    public int compareTo(@NotNull NumberResourceRange o) {
        return switch (o) {
            case AsnRange ignored -> 1;
            case Ipv4Prefix that -> {
                int rc = Long.compare(this.lowerBound(), that.lowerBound());
                if (rc != 0) {
                    yield rc;
                }
                yield -Long.compare(this.upperBound(), that.upperBound());
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
    public IpResourceType getType() {
        return IpResourceType.IPv4;
    }

    @Override
    public boolean contains(@Nullable NumberResourceRange other) {
        return switch (other) {
            case null -> false;
            case AsnRange ignored -> false;
            case Ipv4Prefix that -> this.lowerBound() <= that.lowerBound() && this.upperBound() >= that.upperBound();
            case Ipv4Block that -> this.lowerBound() <= that.lowerBound() && this.upperBound() >= that.upperBound();
            case Ipv6Prefix ignored -> false;
            case Ipv6Range ignored -> false;
        };
    }

    @Override
    public @NotNull List<@NotNull NumberResourceRange> subtract(@Nullable NumberResourceRange other) {
        return switch (other) {
            case null -> Collections.singletonList(this);
            case AsnRange ignored -> Collections.singletonList(this);
            case Ipv6Block ignored -> Collections.singletonList(this);
            case Ipv4Block that -> {
                if (other.contains(this)) {
                    yield Collections.emptyList();
                } else if (overlaps(this, that)) {
                    var result = new ArrayList<@NotNull NumberResourceRange>(2);
                    if (this.lowerBound() < that.lowerBound()) {
                        result.add(Ipv4Block.of(this.lowerBound(), that.lowerBound() - 1));
                    }
                    if (this.upperBound() > that.upperBound()) {
                        result.add(Ipv4Block.of(that.upperBound() + 1, this.upperBound()));
                    }
                    yield result;
                } else {
                    yield Collections.singletonList(this);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + other);
        };
    }

    abstract long lowerBound();
    abstract long upperBound();

    public static @Nullable Ipv4Block intersection(@Nullable Ipv4Block a, @Nullable Ipv4Block b) {
        if (a == null || b == null) {
            return null;
        }

        long start = max(a.lowerBound(), b.lowerBound());
        long end = min(a.upperBound(), b.upperBound());
        return start <= end ? Ipv4Block.of(start, end) : null;
    }

    public static boolean overlaps(@Nullable Ipv4Block a, @Nullable Ipv4Block b) {
        return a != null
            && b != null
            && a.lowerBound() <= b.upperBound()
            && a.upperBound() >= b.lowerBound();
    }

    public static @Nullable Ipv4Block merge(@Nullable Ipv4Block a, @Nullable Ipv4Block b) {
        if (!mergeable(a, b)) {
            return null;
        } else {
            return Ipv4Block.of(
                Math.min(a.lowerBound(), b.lowerBound()),
                Math.max(a.upperBound(), b.upperBound())
            );
        }
    }

    public static boolean mergeable(@Nullable Ipv4Block a, @Nullable Ipv4Block b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.lowerBound() <= b.upperBound()) {
            return a.upperBound() + 1 >= b.lowerBound();
        } else {
            return Math.abs(a.lowerBound() - b.upperBound()) == 1;
        }
    }

    static boolean isLegalPrefix(long start, long end) {
        long temp = start ^ end;
        int length = Integer.numberOfLeadingZeros((int) temp);
        return start == lowerBoundForPrefix(start, length) && end == upperBoundForPrefix(end, length);
    }
}
