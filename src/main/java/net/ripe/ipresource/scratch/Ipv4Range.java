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
package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

public final class Ipv4Range extends Ipv4Block {
    final int start;
    final int end;

    Ipv4Range(long start, long end) {
        this.start = (int) start;
        this.end = (int) end;
        if (Integer.toUnsignedLong(this.start) != start) {
            throw new IllegalArgumentException("start out of bounds");
        }
        if (Integer.toUnsignedLong(this.end) != end) {
            throw new IllegalArgumentException("end out of bounds");
        }
        if (Integer.compareUnsigned(this.start, this.end) > 0) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }
        if (Ipv4Block.isLegalPrefix(start, end)) {
            throw new IllegalArgumentException("proper prefix must not be represented by range");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv4Range that && this.start == that.start && this.end == that.end;
    }

    @Override
    public int hashCode() {
        return '4' + 31 * 31 * Integer.hashCode(start) + 31 * Integer.hashCode(end);
    }

    @Override
    public String toString() {
        return start() + "-" + end();
    }

    @Override
    public @NotNull Ipv4Address start() {
        return new Ipv4Address(start);
    }

    @Override
    public @NotNull Ipv4Address end() {
        return new Ipv4Address(end);
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    long lowerBound() {
        return Integer.toUnsignedLong(start);
    }

    @Override
    long upperBound() {
        return Integer.toUnsignedLong(end);
    }
}
