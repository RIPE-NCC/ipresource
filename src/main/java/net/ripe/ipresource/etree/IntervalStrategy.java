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
package net.ripe.ipresource.etree;

import java.util.Comparator;


/**
 * A strategy for intervals <code>K</code> with a lower-bound and upper-bound. Both bounds are
 * considered to be <em>inclusive</em>.
 *
 * @param <K> the interval type
 */
public interface IntervalStrategy<K> {

    /**
     * Tests if the <code>left</code> interval contains the <code>right</code> interval. Note that
     * if two intervals are <em>equal</em>, they also contain each other (and vice versa). An
     * interval always contains itself.
     *
     * @param left the interval that contains <code>right</code>
     * @param right the interval to test for containment
     * @return true if <code>left</code> contains <code>right</code>
     */
    boolean contains(K left, K right);

    /**
     * Tests if these two intervals overlap. Two intervals overlap if there exists a point which is
     * contained within both intervals. An interval always overlaps itself.
     *
     * @param left the interval to test for overlap
     * @param right the other interval to test for overlap
     * @return true if the <code>left</code> interval overlaps the <code>right</code> interval
     */
    boolean overlaps(K left, K right);

    /**
     * Copies the specified interval into a new interval that has both its lower and upper-bound set
     * to the original's lower-bound. This is used to be able to compare an interval's lower-bound
     * with another interval's upper-bound.
     *
     * @return a new interval that has both its lower- and upper-bound set to this interval's
     *         lower-bound
     */
    K singletonIntervalAtLowerBound(K interval);

    /**
     * A comparator that compares two intervals based on their upper-bounds. This is used by the
     * {@link NestedIntervalMap} implementation to quickly find two potentially overlapping
     * intervals by ordering intervals on the upper-bound and searching based on the lower-bound.
     */
    Comparator<K> upperBoundComparator();
}
