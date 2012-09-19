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

public abstract class UniqueIpResource extends IpResource {

    private static final long serialVersionUID = 1L;

    public abstract BigInteger getValue();

    public abstract int getCommonPrefixLength(UniqueIpResource other);

    public abstract IpAddress lowerBoundForPrefix(int prefixLength);

    public abstract IpAddress upperBoundForPrefix(int prefixLength);

    @Override
    public final UniqueIpResource getStart() {
        return this;
    }

    @Override
    public final UniqueIpResource getEnd() {
        return this;
    }

    public final UniqueIpResource min(UniqueIpResource other) {
        return compareTo(other) < 0 ? this : other;
    }

    public final UniqueIpResource max(UniqueIpResource other) {
        return compareTo(other) >= 0 ? this : other;
    }

    protected boolean adjacent(UniqueIpResource other) {
        return getType() == other.getType() && getValue().subtract(other.getValue()).abs().equals(BigInteger.ONE);
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof UniqueIpResource) {
            throw new IllegalStateException("should be overriden by subclass");
        } else if (obj instanceof IpResourceRange) {
            return upTo(this).compareTo(obj);
        } else {
            throw new IllegalArgumentException("not a valid resource type: " + obj);
        }
    }

    @Override
    public final UniqueIpResource unique() {
        return this;
    }

    public final IpResourceRange upTo(UniqueIpResource end) {
        return IpResourceRange.range(this, end);
    }

    public UniqueIpResource predecessor() {
        return getType().fromBigInteger(getValue().subtract(BigInteger.ONE));
    }

    public UniqueIpResource successor() {
        return getType().fromBigInteger(getValue().add(BigInteger.ONE));
    }

    // Parsing.

    public static UniqueIpResource parse(String s) {
        try {
            try {
                return Ipv4Address.parse(s);
            } catch (IllegalArgumentException ex4) {
            	try {
            		return Ipv6Address.parse(s);
            	} catch (IllegalArgumentException ex6) {
            		return Asn.parse(s);
            	}
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid IPv4, IPv6 or ASN resource: %s", s));
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = in.readFields();
        try {
            gf.get("type", null); // Ignore, but for older version compatibility.
        } catch (IllegalArgumentException ignored) {
        }
    }
}
