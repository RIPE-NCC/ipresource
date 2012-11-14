/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
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

import java.math.BigInteger;

public abstract class IpAddress extends UniqueIpResource {

    private static final long serialVersionUID = 2L;

    public static IpAddress parse(String s) {
        return parse(s, false);
    }

    public static IpAddress parse(String s, boolean defaultMissingOctets) {
        try {
            try {
                return Ipv4Address.parse(s, defaultMissingOctets);
            } catch (IllegalArgumentException e) {
                return Ipv6Address.parse(s);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid IP address: " + s));
        }
    }

    protected static BigInteger bitMask(int prefixLength, IpResourceType type) {
        final BigInteger MINUS_ONE = new BigInteger("-1");
        return BigInteger.ONE.shiftLeft(type.getBitSize() - prefixLength).add(MINUS_ONE);
    }

    public IpAddress getCommonPrefix(IpAddress other) {
        return lowerBoundForPrefix(getCommonPrefixLength(other));
    }

    protected IpAddress createOfSameType(BigInteger value) {
        return (IpAddress) getType().fromBigInteger(value);
    }

    /**
     * Returns the position of the least significant '1' for an IP address;
     * returns {@link IpResourceType#getBitSize()} if there is no '1'; i.e. for 0.0.0.0
     * @return
     */
    public int getLeastSignificantOne() {
        return getLeastSignificant(true);
    }

    /**
     * Returns the position of the least significant '0' for an IP address;
     * returns {@link IpResourceType#getBitSize()} if there is no '0'; i.e. for 255.255.255.255
     * @return
     */
    public int getLeastSignificantZero() {
        return getLeastSignificant(false);
    }

    private int getLeastSignificant(boolean bit) {
        for (int i = 0; i < getType().getBitSize(); i++) {
            if (getValue().testBit(i) == bit) {
                return i;
            }
        }
        return getType().getBitSize();
    }

    public IpAddress stripLeastSignificantOnes() {
            int leastSignificantZero = getLeastSignificantZero();
            return createOfSameType(getValue().shiftRight(leastSignificantZero).shiftLeft(leastSignificantZero));
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public abstract String toString(boolean defaultMissingOctets);
}
