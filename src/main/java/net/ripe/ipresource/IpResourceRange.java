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

import java.math.BigInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Example: AS1-AS20
 */
public class IpResourceRange extends IpResource {

	private static final long serialVersionUID = 1L;

    private UniqueIpResource start;
    private UniqueIpResource end;

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

    // ------------------------------------------------------------ Range
    // Interface

    public UniqueIpResource getStart() {
        return start;
    }

    public UniqueIpResource getEnd() {
        return end;
    }

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
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof UniqueIpResource) {
            return -obj.compareTo(this);
        } else if (obj instanceof IpResourceRange) {
            IpResource that = (IpResource) obj;
            int rc = getStart().doCompareTo(that.getStart());
            if (rc != 0) {
                return rc;
            } else {
                return -getEnd().doCompareTo(that.getEnd());
            }
        } else {
            throw new IllegalArgumentException("unknown resource type: " + obj);
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

    public static IpResourceRange parse(String s) {
        if(s.indexOf(',') >= 0) {
        	int slashIdx = s.indexOf('/');
        	String startAddress = s.substring(0, slashIdx);
        	
        	UniqueIpResource start = UniqueIpResource.parse(startAddress);
        	UniqueIpResource end = UniqueIpResource.parse(startAddress);
        	
        	String sizes = s.substring(slashIdx);
        	for(String sizeStr: sizes.split(",")) {
        		int size = Integer.parseInt(sizeStr.substring(1));
        		end = end.upperBoundForPrefix(size).successor();
        	}
        	
            return IpResourceRange.range(start, end.predecessor());
        }

        int idx = s.indexOf('/');
        if (idx >= 0) {
            IpAddress prefix = IpAddress.parse(s.substring(0, idx), true);
            int length = Integer.parseInt(s.substring(idx + 1));
            return IpRange.prefix(prefix, length);
        }

        idx = s.indexOf('-');
        if (idx >= 0) {
            UniqueIpResource start = UniqueIpResource.parse(s.substring(0, idx));
            UniqueIpResource end = UniqueIpResource.parse(s.substring(idx + 1));
            if (start.getType() != end.getType()) {
                throw new IllegalArgumentException("resource types in range do not match");
            }
            return IpResourceRange.range(start, end);
        }

        throw new IllegalArgumentException("illegal resource range: " + s);
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
