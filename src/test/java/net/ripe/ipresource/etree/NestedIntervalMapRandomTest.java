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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link net.ripe.ipresource.etree.NestedIntervalMap} using random data so we can flush out bugs
 * that we didn't expect.
 */
public class NestedIntervalMapRandomTest {

    private final long seed = System.currentTimeMillis();
    private final Random random = new Random(seed);

    private List<TestInterval> everything;
    private Map<TestInterval, List<TestInterval>> childrenByParent;
    private NestedIntervalMap<TestInterval, TestInterval> subject;

    private List<TestInterval> generateRandomSiblings(TestInterval parent, int count) {
        List<TestInterval> result = new ArrayList<TestInterval>();
        if (count == 0)
            return result;

        long size = parent.size();
        long sizePerChild = size / count;

        long start = parent.start();
        for (int i = 0; i < count; ++i) {
            long gapBefore = sizePerChild * random.nextInt(4) / 10L;
            long gapAfter = sizePerChild * random.nextInt(1) / 10L;
            TestInterval child = new TestInterval(start + gapBefore, start + sizePerChild - gapAfter - 1);
            start += sizePerChild;
            assertTrue("generated child not inside parent (seed = " + seed + ")", parent.contains(child));
            if (!parent.equals(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private void generateRandomTree(Map<TestInterval, List<TestInterval>> result, TestInterval parent, int depth, int siblingCount) {
        List<TestInterval> children = generateRandomSiblings(parent, siblingCount * (7 + random.nextInt(7)) / 10);
        result.put(parent, children);

        if (depth > 0) {
            for (TestInterval child : children) {
                generateRandomTree(result, child, depth - 1, siblingCount);
            }
        }
    }

    @Before
    public void setup() {
        everything = new ArrayList<TestInterval>();
        childrenByParent = new HashMap<TestInterval, List<TestInterval>>();
        subject = new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY);

        List<TestInterval> roots = generateRandomSiblings(TestInterval.MAX_RANGE, random.nextInt(3) + 5);
        for (TestInterval root : roots) {
            generateRandomTree(childrenByParent, root, random.nextInt(4), random.nextInt(4) + 3);
        }
        everything.addAll(roots);
        for (List<TestInterval> children : childrenByParent.values()) {
            everything.addAll(children);
        }
        for (TestInterval interval : everything) {
            subject.put(interval, interval);
        }

        Collections.sort(everything);
        System.err.println("RANDOM NESTED INTERVAL MAP TEST: Generated " + everything.size() + " intervals with seed " + seed);
    }

    @Test
    public void should_find_everything() {
        assertEquals("failed with seed: " + seed, everything, subject.findExactAndAllMoreSpecific(TestInterval.MAX_RANGE));
    }

    @Test
    public void should_find_every_interval_individually() {
        for (TestInterval interval : everything) {
            assertThat("failed with seed: " + seed, subject.findExact(interval), is(interval));
        }
    }

    @Test
    public void should_find_all_more_specific() {
        for (int i = 0; i < 100; ++i) {
            TestInterval range = randomIpv4Interval();
            List<TestInterval> actual = subject.findExactAndAllMoreSpecific(range);
            List<TestInterval> expected = new ArrayList<TestInterval>();
            for (TestInterval interval : everything) {
                if (range.contains(interval)) {
                    expected.add(interval);
                }
            }
            assertEquals("failed with seed: " + seed, expected, actual);
        }
    }

    @Test
    public void should_find_all_less_specific() {
        for (int i = 0; i < 100; ++i) {
            TestInterval range = randomIpv4Interval();
            List<TestInterval> actual = subject.findExactAndAllLessSpecific(range);
            List<TestInterval> expected = new ArrayList<TestInterval>();
            for (TestInterval interval : everything) {
                if (interval.contains(range)) {
                    expected.add(interval);
                }
            }

            assertEquals("failed with seed: " + seed, expected, actual);
        }
    }

    @Test
    public void should_find_first_more_specific_for_every_contained_interval() {
        for (TestInterval interval : childrenByParent.keySet()) {
            assertEquals("interval: " + interval + ", seed = " + seed, childrenByParent.get(interval), subject.findFirstMoreSpecific(interval));
        }
    }

    @Test
    public void should_promote_children_of_delete_node_to_parent() {
        for (int i = 0; i < 10; ) {
            NestedIntervalMap<TestInterval, TestInterval> copy = new NestedIntervalMap<TestInterval, TestInterval>(subject, TestInterval.STRATEGY);
            TestInterval interval = everything.get(random.nextInt(everything.size()));
            if (childrenByParent.containsKey(interval)) {
                TestInterval parent = copy.findFirstLessSpecific(interval);
                if (parent != null) {
                    copy.remove(interval);
                    List<TestInterval> actual = copy.findFirstMoreSpecific(parent);
                    assertTrue("interval " + interval + " did not move all children to parent " + parent + " on deletion (seed = " + seed + "): "
                            + actual, actual.containsAll(childrenByParent.get(interval)));
                    ++i;
                }
            }

        }
    }

    @Test
    public void should_contain_first_more_specific_for_random_intervals() {
        for (int i = 0; i < 100; ++i) {
            TestInterval range = randomIpv4Interval();
            List<TestInterval> actual = subject.findFirstMoreSpecific(range);
            List<TestInterval> allMoreSpecific = subject.findAllMoreSpecific(range);
            assertTrue("first more specific is subset of all more specific", allMoreSpecific.containsAll(actual));
            for (TestInterval moreSpecific : allMoreSpecific) {
                boolean covered = false;
                for (TestInterval firstMoreSpecific : actual) {
                    if (firstMoreSpecific.contains(moreSpecific)) {
                        covered = true;
                        break;
                    }
                }
                assertTrue("All more specific " + moreSpecific + " must be contained by first more specific", covered);
            }
        }
    }

    @Test
    public void should_remove_all_intervals_starting_with_child_nodes() {
        Collections.reverse(everything);
        for (TestInterval interval : everything) {
            subject.remove(interval);
        }
        assertEquals(Collections.emptyList(), subject.findAllMoreSpecific(TestInterval.MAX_RANGE));
    }

    private TestInterval randomIpv4Interval() {
        return new TestInterval(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE) + (TestInterval.MAX_RANGE.end() / 2));
    }

}
