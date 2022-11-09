/**
 * The BSD License
 *
 * Copyright (c) 2010-2022 RIPE NCC
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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigInteger;

/**
 * Example: AS1-AS20
 */
public class IpResourceRange extends IpResource {

    private static final long serialVersionUID = 1L;

    private final UniqueIpResource start;
    private final UniqueIpResource end;

    public static IpResourceRange range(UniqueIpResource start, UniqueIpResource end) {
        if (start instanceof IpAddress && end instanceof IpAddress) {
            return IpRange.range((IpAddress) start, (IpAddress) end);
        } else {
            return new IpResourceRange(start, end);
        }
    }

    protected IpResourceRange(UniqueIpResource start, UniqueIpResource end) {
        // ASSUMPTION: All ranges are bounded
        // This is the invariant!
        Validate.notNull(start, "start is null");
        Validate.notNull(end, "end is null");
        Validate.isTrue(start.getType() == end.getType(), "resource types do not match");
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("range must not be empty: " + start + "-" + end);
        }

        this.start = start;
        this.end = end;
    }

    @Override
    public UniqueIpResource getStart() {
        return start;
    }

    @Override
    public UniqueIpResource getEnd() {
        return end;
    }

    @Override
    public IpResourceType getType() {
        return start.getType();
    }

    @Override
    public boolean isValidNetmask() {
        return isUnique() && unique().isValidNetmask();
    }

    @Override
    public String toString() {
        return start.toString() + "-" + end.toString();
    }

    @Override
    protected int doCompareTo(IpResource that) {
        if (that instanceof UniqueIpResource) {
            return -that.compareTo(this);
        } else if (that instanceof IpResourceRange) {
            int rc = getStart().doCompareTo(that.getStart());
            if (rc != 0) {
                return rc;
            } else {
                return -getEnd().doCompareTo(that.getEnd());
            }
        } else {
            throw new IllegalArgumentException("unknown resource type: " + that);
        }
    }

    @Override
    protected int doHashCode() {
        return isUnique() ? unique().hashCode() : new HashCodeBuilder().append(start).append(end).toHashCode();
    }

    @Override
    public UniqueIpResource unique() {
        Validate.isTrue(isUnique(), "resource not unique");
        return getStart();
    }

    /**
     * <p>Create a range of IpResources. Note that a range can only contain 1 type of IpResource and a continuous range is implied by the name</p>
     * <p>For a collection of ranges and/or single resources of multiple types see IpResourceSet</p>
     * <p>Allowed notations:</p>
     * <ul>
     *   <li>IpResourceRange.parse("10.0.0.0/16") &rarr; The usual format for an IPv4 Prefix
     *   <li>IpResourceRange.parse("10.0.0.0-10.1.2.3") &rarr; Arbitrary ranges are denoted with a "-"
     *   <li>IpResourceRange.parse("10.0.0.0/16,/24,/25,/31") &rarr; Parses 10.0.0.0/16 plus the adjacent networks /24, /25 and /31. Note that the mask of these networks MUST increase. I.e. go from big to small..
     *   <li>IpResourceRange.parse("AS0-AS4294967295") &rarr; For AS numbers ranges may be used
     * </ul>
     *
     * @see IpResourceSet
     */
    public static IpResourceRange parse(String s) {
        if (s.indexOf(',') >= 0) {
            return parseCommaPrefixNotation(s);
        } else if (s.indexOf('/') >= 0) {
            return parseAsSingleSlashNotatedRange(s);
        } else if (s.indexOf('-') >= 0) {
            return parseAsRangeDenotedBySingleStartAndEndAddress(s);
        }
        throw new IllegalArgumentException("illegal resource range: " + s);
    }

    private static IpResourceRange parseAsRangeDenotedBySingleStartAndEndAddress(String s) {
        int idx = s.indexOf('-');
        UniqueIpResource start = UniqueIpResource.parse(s.substring(0, idx));
        UniqueIpResource end = UniqueIpResource.parse(s.substring(idx + 1));
        if (start.getType() != end.getType()) {
            throw new IllegalArgumentException("resource types in range do not match");
        }
        return IpResourceRange.range(start, end);
    }

    private static IpResourceRange parseAsSingleSlashNotatedRange(String s) {
        int idx = s.indexOf('/');
        IpAddress prefix = IpAddress.parse(s.substring(0, idx), true);
        int length = Integer.parseInt(s.substring(idx + 1));
        return IpRange.prefix(prefix, length);
    }

    /**
     * <p>Expects a notation like: "10.0.0.0/16,/24,/25,/31"</P>
     * <p>This parses:</p>
     * <ul>
     * <li>10.0.0.0/16 plus the adjacent prefixes:
     * <li>/24
     * <li>/25
     * <li>/31
     * </ul>
     * <p><b>Note</b> that the mask of these <b>prefixes</b> MUST increase. I.e. go from big to small..</p>
     */
    private static IpResourceRange parseCommaPrefixNotation(String s) {
        int slashIdx = s.indexOf('/');

        if (slashIdx == -1) {
            throw new IllegalArgumentException("Comma separated notation can only be used for adjacent prefix notations like: 10.0.0.0/16,/24,/25,/31");
        }

        String startAddress = s.substring(0, slashIdx);

        UniqueIpResource start = UniqueIpResource.parse(startAddress);
        UniqueIpResource end = UniqueIpResource.parse(startAddress);

        String prefixSizeList = s.substring(slashIdx);
        int lastSeenPrefixMask = -1;
        for(String prefixStr: prefixSizeList.split(",")) {
            int prefixMask = Integer.parseInt(prefixStr.substring(1)); // Will throw IllegalArgumentException when string is not a valid Integer
            if (lastSeenPrefixMask < prefixMask) {
                end = end.upperBoundForPrefix(prefixMask).successor();
                lastSeenPrefixMask = prefixMask;
            } else {
                throw new IllegalArgumentException("Mask of prefix " + prefixMask + " is bigger than previous: " + lastSeenPrefixMask);
            }
        }

        return IpResourceRange.range(start, end.predecessor());
    }

    public static IpResourceRange assemble(BigInteger start, BigInteger end, IpResourceType type) {
        return type.fromBigInteger(start).upTo(type.fromBigInteger(end));
    }

    public static IpResourceRange parseWithNetmask(String ipStr, String netmaskStr) {
        UniqueIpResource start = UniqueIpResource.parse(ipStr);
        UniqueIpResource netmask = UniqueIpResource.parse(netmaskStr);
        if (!netmask.isValidNetmask()) {
            throw new IllegalArgumentException("netmask '" + netmaskStr + "' is not a valid netmask");
        }

        int size = netmask.getValue().bitCount();
        UniqueIpResource end = start.upperBoundForPrefix(size);

        return IpResourceRange.range(start, end);
    }
}
