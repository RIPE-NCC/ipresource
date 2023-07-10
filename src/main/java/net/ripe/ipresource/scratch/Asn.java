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
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Asn implements NumberResource {
    private static final Pattern ASN_TEXT_PATTERN = Pattern.compile("(?:AS)?(\\d+)(\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    public static final long ASN_MIN_VALUE = 0L;
    public static final long ASN16_MAX_VALUE = (1L << 16) - 1L;
    public static final long ASN32_MAX_VALUE = (1L << 32) - 1L;

    private final int value;

    Asn(int value) {
        this.value = value;
    }

    Asn(long value) {
        this.value = (int) value;
        if (Integer.toUnsignedLong(this.value) != value) {
            throw new IllegalArgumentException("ASN value out of bounds");
        }
    }

    public static @NotNull Asn of(long value) {
        return new Asn(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Asn that && this.value == that.value;
    }

    @Override
    public int hashCode() {
        return 'A' + 31 * value;
    }

    @Override
    public String toString() {
        return "AS" + longValue();
    }

    @Override
    public int compareTo(@NotNull NumberResource o) {
        return switch (o) {
            case Asn that -> Integer.compareUnsigned(this.value, that.value);
            case Ipv4Address ignored -> -1;
            case Ipv6Address ignored -> -1;
        };
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.ASN;
    }

    @Override
    public @NotNull Asn predecessorOrFirst() {
        return value == 0 ? this : new Asn(value - 1);
    }

    @Override
    public @NotNull Asn successorOrLast() {
        return value == -1 ? this : new Asn(value + 1);
    }

    public static @NotNull Asn parse(@NotNull String text) {
        text = text.trim();

        Matcher matcher = ASN_TEXT_PATTERN.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a legal ASN: " + text);
        }

        long high = 0L;
        long low;

        if (matcher.group(3) != null) {
            low = Long.parseLong(matcher.group(3));
            high = Long.parseLong(matcher.group(1));

            checkRange(high, ASN16_MAX_VALUE);
            checkRange(low, ASN16_MAX_VALUE);
        } else {
            low = Long.parseLong(matcher.group(1));

            checkRange(low, ASN32_MAX_VALUE);
        }

        return new Asn((high << 16) | low);
    }

    long longValue() {
        return Integer.toUnsignedLong(value);
    }

    private static void checkRange(long value, long max) {
        Validate.isTrue(value >= ASN_MIN_VALUE);
        Validate.isTrue(value <= max);
    }
}
