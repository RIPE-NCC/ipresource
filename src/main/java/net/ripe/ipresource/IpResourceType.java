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
package net.ripe.ipresource;

import java.math.BigInteger;

public enum IpResourceType {
    ASN("Autonomous System Number", 32) {
        @Override
        public boolean allowPrefixNotation() {
            return false;
        }

        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Asn(value.longValue());
        }
    },

    IPv4("IPv4 Address", 32) {
        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Ipv4Address(value.longValue());
        }
    },
    IPv6("IPv6 Address", 128) {
        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Ipv6Address(value);
        }
    };

    private final String description;
    private final int bitSize;

    private IpResourceType(String description, int bitSize) {
        this.description = description;
        this.bitSize = bitSize;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public int getBitSize() {
        return bitSize;
    }

    public boolean allowPrefixNotation() {
        return true;
    }

    public UniqueIpResource getMinimum() {
        return fromBigInteger(BigInteger.ZERO);
    }
    
    public UniqueIpResource getMaximum() {
        return fromBigInteger(BigInteger.ONE.shiftLeft(bitSize).subtract(BigInteger.ONE));
    }

    public abstract UniqueIpResource fromBigInteger(BigInteger value);

    /**
     * Necessary for FitNesse.
     */
    public static IpResourceType parse(String s) {
        return IpResourceType.valueOf(s);
    }
}
