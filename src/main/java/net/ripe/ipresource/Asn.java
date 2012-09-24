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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;

/**
 * Immutable value object for Autonomous System Numbers.
 */
public class Asn extends UniqueIpResource {

    private static final long serialVersionUID = 2L;

    private static final Pattern ASN_TEXT_PATTERN = Pattern.compile("(?:AS)?(\\d+)(\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    public static long ASN_MIN_VALUE = 0L;
    public static long ASN16_MAX_VALUE = (1L << 16) - 1L;
    public static long ASN32_MAX_VALUE = (1L << 32) - 1L;

    // Int is more memory efficient, so use value() accessor to get correct
    // unsigned long value.
    private int intValue;

    public Asn(BigInteger value) {
        this(value.longValue());
    }

    public Asn(long value) {
        checkRange(value, ASN32_MAX_VALUE);
        this.intValue = (int) value;
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.ASN;
    }

    public static Asn parse(String text) {
        if (text == null) {
            return null;
        }

        text = text.trim();

        Matcher matcher = ASN_TEXT_PATTERN.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a legal ASN: " + text);
        }

        long high = 0L;
        long low;

        if (matcher.group(3) != null) {
            low = Long.valueOf(matcher.group(3));
            high = Long.valueOf(matcher.group(1));

            checkRange(high, ASN16_MAX_VALUE);
            checkRange(low, ASN16_MAX_VALUE);
        } else {
            low = Long.valueOf(matcher.group(1));

            checkRange(low, ASN32_MAX_VALUE);
        }

        return new Asn((high << 16) | low);
    }

    private static void checkRange(long value, long max) {
        Validate.isTrue(value >= ASN_MIN_VALUE);
        Validate.isTrue(value <= max);
    }

    public final long longValue() {
        return intValue & ASN32_MAX_VALUE;
    }

    @Override
    protected int doHashCode() {
        return intValue;
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof Asn) {
            long otherValue = ((Asn) obj).longValue();
            if (longValue() < otherValue) {
                return -1;
            } else if (longValue() > otherValue) {
                return +1;
            } else {
                return 0;
            }
        } else {
            return super.doCompareTo(obj);
        }
    }

    @Override
    public String toString() {
        return "AS" + longValue();
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress lowerBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public final BigInteger getValue() {
        return BigInteger.valueOf(longValue());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        if (!gf.defaulted("intValue"))
            this.intValue = gf.get("intValue", 0);
        else
            this.intValue = (int) gf.get("value", 0L);
    }
}
