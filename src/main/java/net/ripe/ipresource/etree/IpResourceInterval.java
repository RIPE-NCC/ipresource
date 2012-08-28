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
package net.ripe.ipresource.etree;

import net.ripe.ipresource.IpResourceRange;

public class IpResourceInterval<T extends IpResourceRange> implements Comparable<IpResourceInterval<T>>, Interval<IpResourceInterval<T>> {

    private final T resource;

    public IpResourceInterval(T resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }
        this.resource = resource;
    }

    public T get() {
        return resource;
    }

    @Override
    public boolean contains(IpResourceInterval<T> that) {
        return this.resource.contains(that.resource);
    }

    @Override
    public boolean overlaps(IpResourceInterval<T> that) {
        return this.resource.overlaps(that.resource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IpResourceInterval<T> singletonIntervalAtLowerBound() {
        return new IpResourceInterval<T>((T) resource.getStart().upTo(resource.getStart()));
    }

    @Override
    public int compareUpperBound(IpResourceInterval<T> that) {
        return resource.getEnd().compareTo(that.resource.getEnd());
    }

    @Override
    public int compareTo(IpResourceInterval<T> that) {
        return resource.compareTo(that.resource);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof IpResourceInterval))
            return false;
        IpResourceInterval<?> that = (IpResourceInterval<?>) obj;
        return resource.equals(that.resource);
    }

    @Override
    public String toString() {
        return "IpResourceInterval [" + resource + "]";
    }
}
