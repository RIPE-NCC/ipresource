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
package net.ripe.ipresource.jdk17;

import net.ripe.ipresource.IpResourceSet;
import net.ripe.ipresource.IpResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;

public sealed interface NumberResourceBlock extends Comparable<NumberResourceBlock> permits AsnBlock, IpBlock {
    NumberResourceBlock ALL_AS_RESOURCES = AsnBlock.range(Asn.of(0), Asn.of(4294967295L));
    NumberResourceBlock ALL_IPV4_RESOURCES = Ipv4Block.of(Ipv4Address.of(0), Ipv4Address.of(4294967295L));
    NumberResourceBlock ALL_IPV6_RESOURCES = Ipv6Block.of(Ipv6Address.of(BigInteger.ZERO), Ipv6Address.of(BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE)));

    Comparator<NumberResourceBlock> RANGE_END_COMPARATOR = (o1, o2) -> switch (o1) {
        case AsnBlock left -> switch (o2) {
            case AsnBlock right -> Long.compareUnsigned(left.upperBound(), right.upperBound());
            case IpBlock ignored -> -1;
        };
        case Ipv4Block left -> switch (o2) {
            case AsnBlock ignored -> 1;
            case Ipv4Prefix right -> Long.compareUnsigned(left.upperBound(), right.upperBound());
            case Ipv4Range right -> Long.compareUnsigned(left.upperBound(), right.upperBound());
            case Ipv6Prefix ipv6Prefix -> -1;
            case Ipv6Range ipv6RangeImpl -> -1;
        };
        case Ipv6Block left -> switch (o2) {
            case AsnBlock ignored -> 1;
            case Ipv4Prefix ignored -> 1;
            case Ipv4Range ignored -> 1;
            case Ipv6Prefix right -> left.end().compareTo(right.end());
            case Ipv6Range right -> left.end().compareTo(right.end());
        };
        default ->
            // Only here otherwise the compiler fails on missing cases above (even though all cases are covered)
            throw new IllegalStateException("Unexpected value: " + o1);
    };

    static @NotNull NumberResourceBlock parse(@NotNull String s) {
        var set = IpResourceSet.parse(s);
        if (set.size() != 1) {
            throw new IllegalArgumentException("only single range can be parsed");
        }
        var x = set.iterator().next();
        return switch (x.getType()) {
            case ASN -> new AsnBlock(x.getStart().getValue().longValue(), x.getEnd().getValue().longValue());
            case IPv4 -> Ipv4Block.of(Ipv4Address.of(x.getStart().getValue().longValue()), Ipv4Address.of(x.getEnd().getValue().longValue()));
            case IPv6 -> Ipv6Block.of(Ipv6Address.of(x.getStart().getValue()), Ipv6Address.of(x.getEnd().getValue()));
        };
    }

    IpResourceType getType();

    NumberResource start();

    NumberResource end();

    boolean contains(@Nullable NumberResourceBlock other);
    boolean isSingleton();

    @NotNull List<@NotNull NumberResourceBlock> subtract(@Nullable NumberResourceBlock other);

    static @NotNull NumberResourceBlock range(@NotNull NumberResource start, @NotNull NumberResource end) {
        return switch (start) {
            case Asn x -> AsnBlock.range(x, (Asn) end);
            case IpAddress x -> IpBlock.range(x, (IpAddress) end);
        };
    }

    static @Nullable NumberResourceBlock intersect(@Nullable NumberResourceBlock a, @Nullable NumberResourceBlock b) {
        return switch (a) {
            case null -> null;
            case AsnBlock x -> b instanceof AsnBlock y ? AsnBlock.intersection(x, y) : null;
            case Ipv4Prefix x -> b instanceof Ipv4Block y ? Ipv4Block.intersection(x, y) : null;
            case Ipv4Range x -> b instanceof Ipv4Block y ? Ipv4Block.intersection(x, y) : null;
            case Ipv6Prefix x -> b instanceof Ipv6Block y ? Ipv6Block.intersection(x, y) : null;
            case Ipv6Range x -> b instanceof Ipv6Block y ? Ipv6Block.intersection(x, y) : null;
        };
    }

    static boolean overlaps(@Nullable NumberResourceBlock a, @Nullable NumberResourceBlock b) {
        return switch (a) {
            case null -> false;
            case AsnBlock x -> b instanceof AsnBlock y && AsnBlock.overlaps(x, y);
            case Ipv4Prefix x -> b instanceof Ipv4Block y && Ipv4Block.overlaps(x, y);
            case Ipv4Range x -> b instanceof Ipv4Block y && Ipv4Block.overlaps(x, y);
            case Ipv6Prefix x -> b instanceof Ipv6Block y && Ipv6Block.overlaps(x, y);
            case Ipv6Range x -> b instanceof Ipv6Block y && Ipv6Block.overlaps(x, y);
        };
    }

    static boolean mergeable(@Nullable NumberResourceBlock a, @Nullable NumberResourceBlock b) {
        return switch (a) {
            case null -> false;
            case AsnBlock x -> b instanceof AsnBlock y && AsnBlock.mergeable(x, y);
            case Ipv4Prefix x -> b instanceof Ipv4Block y && Ipv4Block.mergeable(x, y);
            case Ipv4Range x -> b instanceof Ipv4Block y && Ipv4Block.mergeable(x, y);
            case Ipv6Prefix x -> b instanceof Ipv6Block y && Ipv6Block.mergeable(x, y);
            case Ipv6Range x -> b instanceof Ipv6Block y && Ipv6Block.mergeable(x, y);
        };
    }

    static @Nullable NumberResourceBlock merge(@Nullable NumberResourceBlock a, @Nullable NumberResourceBlock b) {
        return switch (a) {
            case null -> null;
            case AsnBlock x -> b instanceof AsnBlock y ? AsnBlock.merge(x, y) : null;
            case Ipv4Prefix x -> b instanceof Ipv4Block y ? Ipv4Block.merge(x, y) : null;
            case Ipv4Range x -> b instanceof Ipv4Block y ? Ipv4Block.merge(x, y) : null;
            case Ipv6Prefix x -> b instanceof Ipv6Block y ? Ipv6Block.merge(x, y) : null;
            case Ipv6Range x -> b instanceof Ipv6Block y ? Ipv6Block.merge(x, y) : null;
        };
    }
}
