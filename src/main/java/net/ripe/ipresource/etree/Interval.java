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

/**
 * An interval with a lower-bound and upper-bound. Both bounds are considered to
 * be <em>inclusive</em>.
 *
 * Implementations of this interface must be immutable and must correctly
 * override {@link Object#equals(Object)} and {@link Object#hashCode()} based on
 * the bounds.
 *
 * @param <K>
 *            the interval type
 */
public interface Interval<K extends Interval<K>> {

    /**
     * Tests if this interval contains <code>that</code>. Note that if two
     * intervals are <em>equal</em>, they also contain each other (and vice
     * versa). An interval always contains itself.
     *
     * @param that
     *            the interval to test for containment
     * @return true if <code>this</code> contains <code>that</code> interval
     */
    boolean contains(K that);

    /**
     * Tests if these two intervals overlap. Two intervals overlap if there
     * exists a point which is contained within both intervals. An interval
     * always overlaps itself.
     *
     * @param that
     *            the other interval to test for overlap
     * @return true if <code>this</code> interval overlaps <code>that</code>
     *         interval
     */
    boolean overlaps(K that);

    /**
     * Copies this interval into a new interval that has both its lower and
     * upper-bound set to the original's lower-bound. This is used to be able to
     * compare an interval's lower-bound with another interval's upper-bound.
     *
     * <pre>
     *   Interval a = ...
     *   Interval b = ...
     *   if (a.singletonIntervalAtLowerBound().compareUpperBoundTo(b) < 0) ...
     * </pre>
     *
     * @return a new interval that has both its lower- and upper-bound set to
     *         this interval's lower-bound
     */
    K singletonIntervalAtLowerBound();

    /**
     * Compare two intervals based on their upper-bounds. This is used by the
     * {@link NestedIntervalMap} implementation to quickly find two potentially
     * overlapping intervals by ordering intervals on the upper-bound and
     * searching based on the lower-bound.
     *
     * @param that
     *            the interval to compare the upper-bound with
     * @return &lt;0 if this upper-bound is less than that upper-bound,<br> =0 if
     *         this upper-bound equals that upper-bound,<br> &gt;0 if this
     *         upper-bound is greater than that upper-bound
     */
    int compareUpperBound(K that);
}
