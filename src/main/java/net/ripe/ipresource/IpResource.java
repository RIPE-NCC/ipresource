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

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class IpResource implements Serializable, Comparable<IpResource> {

    private static final long serialVersionUID = 1L;

    public abstract IpResourceType getType();

    public int compareTo(IpResource that) {
        int rc;

        rc = this.getType().compareTo(that.getType());
        if (rc != 0) {
            return rc;
        }

        return doCompareTo(that);
    }

    public boolean contains(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        return getStart().compareTo(other.getStart()) <= 0 && getEnd().compareTo(other.getEnd()) >= 0;
    }

    public boolean overlaps(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        return getStart().compareTo(other.getEnd()) <= 0 && getEnd().compareTo(other.getStart()) >= 0;
    }

    public boolean adjacent(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        if (overlaps(other)) {
            return false;
        }
        return getEnd().adjacent(other.getStart()) || getStart().adjacent(other.getEnd());
    }

    public boolean isMergeable(IpResource other) {
        return this.overlaps(other) || this.adjacent(other);
    }

    public IpResource merge(IpResource other) {
        Validate.isTrue(this.isMergeable(other));
        return getStart().min(other.getStart()).upTo(getEnd().max(other.getEnd()));
    }

    public boolean isUnique() {
        return getStart().equals(getEnd());
    }

    public abstract UniqueIpResource unique();

    public abstract UniqueIpResource getStart();

    public abstract UniqueIpResource getEnd();

    public boolean isValidNetmask() {
        return false;
    }
    
    protected abstract int doCompareTo(IpResource that);

    protected abstract int doHashCode();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof IpResource)) {
            return false;
        }
        return compareTo((IpResource) other) == 0;
    }

    @Override
    public final int hashCode() {
        return doHashCode();
    }

    /**
     * Subtracts the range <code>value</code> from the current resource.
     * Depending on the operands this can result in 0, 1, or 2 new ranges being
     * returned.
     *
     * @param value the range to subtract.
     * @return non-null list (possibly empty) of resulting ranges.
     */
    public List<IpResourceRange> subtract(IpResource value) {
        if (!this.overlaps(value)) {
            return Collections.singletonList(getStart().upTo(getEnd()));
        } else if (value.contains(this)) {
            return Collections.emptyList();
        } else {
            final List<IpResourceRange> result = new ArrayList<IpResourceRange>(2);
            final UniqueIpResource start = getStart();
            final UniqueIpResource valueStart = value.getStart();
            if (start.compareTo(valueStart) < 0) {
                result.add(start.upTo(valueStart.predecessor()));
            }
            final UniqueIpResource valueEnd = value.getEnd();
            final UniqueIpResource end = getEnd();
            if (valueEnd.compareTo(end) < 0) {
                result.add(valueEnd.successor().upTo(end));
            }
            return result;
        }
    }

    public IpResource intersect(IpResource value) {
        if (getType() != value.getType()) {
            return null;
        }
        
        UniqueIpResource start = getStart().max(value.getStart());
        UniqueIpResource end = getEnd().min(value.getEnd());
        if (start.compareTo(end) > 0) {
            return null;
        } else {
            return start.upTo(end);
        }
    }

    public static IpResource parse(String s) {
        try {
            return IpResourceRange.parse(s);
        } catch (IllegalArgumentException ex) {
            return UniqueIpResource.parse(s);
        }
    }
}
