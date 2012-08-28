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


public class TestInterval implements Comparable<TestInterval>, Interval<TestInterval> {

    private final long start;
    private final long end;

    public static final TestInterval MAX_RANGE = new TestInterval(0, (1L<<32) - 1);

    public TestInterval(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(TestInterval that) {
        if (this.start < that.start) {
            return -1;
        } else if (this.start > that.start) {
            return 1;
        } else if (this.end < that.end) {
            return 1;
        } else if (this.end > that.end) {
            return -1;
        } else {
            return 0;
        }
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public long size() {
        return end - start + 1;
    }

    @Override
    public boolean contains(TestInterval that) {
        return start <= that.start && end >= that.end;
    }

    @Override
    public boolean overlaps(TestInterval that) {
        return this.start <= that.end && this.end >= that.start;
    }

    @Override
    public TestInterval singletonIntervalAtLowerBound() {
        return new TestInterval(start, start);
    }

    @Override
    public int compareUpperBound(TestInterval that) {
        if (this.end < that.end) {
            return -1;
        } else if (this.end > that.end) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (end ^ (end >>> 32));
        result = prime * result + (int) (start ^ (start >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TestInterval))
            return false;
        TestInterval other = (TestInterval) obj;
        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}
