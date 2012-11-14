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

import org.apache.commons.lang.Validate;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Example IP Range: 192.168.0.1-192.168.1.10
 */
public class IpRange extends IpResourceRange {

    private static final long serialVersionUID = 1L;

    public static IpRange range(IpAddress start, IpAddress end) {
        return new IpRange(start, end);
    }

    public static IpRange prefix(IpAddress networkNumber, int prefixLength) {
        Validate.notNull(networkNumber, "network number not null");
        Validate.isTrue(prefixLength >= 0);
        Validate.isTrue(prefixLength <= networkNumber.getType().getBitSize());
        return new IpRange(networkNumber, prefixLength);
    }

    protected IpRange(IpAddress start, IpAddress end) {
        super(start, end);
    }

    /**
     * Parses an IP address range in either <em>prefix</em> or <em>range</em>
     * notation.
     *
     * @param s
     *            the string to parse (non-null).
     * @return an IP address range (non-null).
     * @exception IllegalArgumentException
     *                the string to parse does not represent a valid IP address
     *                range or prefix.
     */
    public static IpRange parse(String s) {
        IpResource result = IpResourceRange.parse(s);
        Validate.isTrue(result instanceof IpRange, "range is not an IP address range: " + s);
        return (IpRange) result;
    }

    protected IpRange(IpAddress networkNumber, int prefixLength) {
        this(networkNumber, networkNumber.upperBoundForPrefix(prefixLength));
        if (!networkNumber.equals(networkNumber.lowerBoundForPrefix(prefixLength))) {
            throw new IllegalArgumentException("not a valid prefix: " + networkNumber + "/" + prefixLength);
        }
    }

    public boolean isLegalPrefix() {
        int n = getPrefixLength();
        return getStart().equals(getStart().lowerBoundForPrefix(n)) && getEnd().equals(getEnd().upperBoundForPrefix(n));
    }

    public int getPrefixLength() {
        return getStart().getCommonPrefixLength(getEnd());
    }

    public List<IpRange> splitToPrefixes() {
        BigInteger rangeEnd = getEnd().getValue();
        BigInteger currentRangeStart = getStart().getValue();
        int startingPrefixLength = getType().getBitSize();
        List<IpRange> prefixes = new LinkedList<IpRange>();

        while (currentRangeStart.compareTo(rangeEnd) <= 0) {
            int maximumPrefixLength = getMaximumLengthOfPrefixStartingAtIpAddressValue(currentRangeStart, startingPrefixLength);
            BigInteger maximumSizeOfPrefix = rangeEnd.subtract(currentRangeStart).add(BigInteger.ONE);
            BigInteger currentSizeOfPrefix = BigInteger.valueOf(2).pow(maximumPrefixLength);

            while ((currentSizeOfPrefix.compareTo(maximumSizeOfPrefix) > 0) && (maximumPrefixLength > 0)) {
                maximumPrefixLength--;
                currentSizeOfPrefix = BigInteger.valueOf(2).pow(maximumPrefixLength);
            }

            BigInteger currentRangeEnd = currentRangeStart.add(BigInteger.valueOf(2).pow(maximumPrefixLength).subtract(BigInteger.ONE));
            IpRange prefix = (IpRange) IpResourceRange.assemble(currentRangeStart, currentRangeEnd, getType());

            prefixes.add(prefix);

            currentRangeStart = currentRangeEnd.add(BigInteger.ONE);
        }

        return prefixes;
    }

    private int getMaximumLengthOfPrefixStartingAtIpAddressValue(BigInteger ipAddressValue, int startingPrefixLength) {
        int prefixLength = startingPrefixLength;

        while ((prefixLength >= 0) && !canBeDividedByThePowerOfTwo(ipAddressValue, prefixLength)) {
            prefixLength--;
        }

        return prefixLength;
    }

    private boolean canBeDividedByThePowerOfTwo(BigInteger number, int power) {
        return number.remainder(BigInteger.valueOf(2).pow(power)).equals(BigInteger.ZERO);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean defaultMissingOctets) {
        if (isLegalPrefix()) {
            return ((IpAddress) getStart()).toString(defaultMissingOctets) + "/" + getPrefixLength();
        } else {
            return super.toString();
        }
    }
}
