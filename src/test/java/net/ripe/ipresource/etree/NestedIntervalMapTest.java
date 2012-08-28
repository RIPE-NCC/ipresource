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

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class NestedIntervalMapTest {

    private NestedIntervalMap<TestInterval, TestInterval> subject = new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY);
    private TestInterval N1_12 = new TestInterval(1, 12);
    private TestInterval N1_4 = new TestInterval(1, 4);
    private TestInterval N5_10 = new TestInterval(5, 10);
    private TestInterval N1_1 = new TestInterval(1, 1);
    private TestInterval N2_2 = new TestInterval(2, 2);
    private TestInterval N3_3 = new TestInterval(3, 3);
    private TestInterval N4_4 = new TestInterval(4, 4);
    private TestInterval N5_5 = new TestInterval(5, 5);
    private TestInterval N6_6 = new TestInterval(6, 6);
    private TestInterval N7_7 = new TestInterval(7, 7);
    private TestInterval N8_8 = new TestInterval(8, 8);
    private TestInterval N9_9 = new TestInterval(9, 9);
    private TestInterval N10_10 = new TestInterval(10, 10);
    private TestInterval N3_4 = new TestInterval(3, 4);
    private TestInterval N5_8 = new TestInterval(5, 8);
    private TestInterval N9_10 = new TestInterval(9, 10);
    private TestInterval N11_12 = new TestInterval(11, 12);
    private List<TestInterval> all = new ArrayList<TestInterval>();

    @Before
    public void setup() {
        all.add(N1_12);
        all.add(N1_4);
        all.add(N5_10);
        all.add(N1_1);
        all.add(N2_2);
        all.add(N3_3);
        all.add(N4_4);
        all.add(N5_5);
        all.add(N6_6);
        all.add(N7_7);
//        all.add(N8_8);
        all.add(N9_9);
        all.add(N10_10);
        all.add(N3_4);
        all.add(N5_8);
        all.add(N9_10);
        all.add(N11_12);
        Collections.sort(all);

        for (TestInterval n : all) {
            subject.put(n, n);
        }
    }

    @Test
    public void clear() {
        subject.put(N1_12, N1_1);
        subject.clear();
        assertThat(subject.findExact(N1_12), is(not(N1_12)));
        assertThat(subject.findExact(N1_12), is(not(N1_1)));
    }

    @Test
    public void test_replace_n1_10() {
        subject.put(N1_12, N1_1);
        assertThat(subject.findExact(N1_12), is(N1_1));
    }

    @Test
    public void fail_on_overlapping_siblings() {
        try {
            subject.put(new TestInterval(8, 13), N1_1);
            fail("OverlappingIntervalException expected");
        } catch (OverlappingIntervalException expected) {
            assertEquals(new TestInterval(8, 13), expected.getInterval());
            assertEquals(asList(N1_12), expected.getOverlaps());
        }
    }

    @Test
    public void test_remove_n1_10() {
        subject.remove(N1_12);
        assertThat(subject.findExact(N1_12), nullValue());
        assertThat(subject.findFirstMoreSpecific(N1_12), contains(N1_4, N5_10, N11_12));
    }

    @Test
    public void test_remove_n5_8() {
        assertEquals(asList(N5_5, N6_6, N7_7), subject.findFirstMoreSpecific(N5_8));
        subject.remove(N5_8);
        assertThat(subject.findExact(N5_8), nullValue());
        assertThat(subject.findFirstMoreSpecific(N5_8), contains(N5_5, N6_6, N7_7));
    }

    @Test
    public void test_remove_key_value() {
        assertEquals(asList(N5_5, N6_6, N7_7), subject.findFirstMoreSpecific(N5_8));
        subject.remove(N5_8, N5_8);
        assertThat(subject.findExact(N5_8), nullValue());
        assertThat(subject.findFirstMoreSpecific(N5_8), contains(N5_5, N6_6, N7_7));
    }

    @Test
    public void test_remove_key_value_nonexistant() {
        NestedIntervalMap<TestInterval, TestInterval> copy = new NestedIntervalMap<TestInterval, TestInterval>(subject, TestInterval.STRATEGY);

        final TestInterval resource = new TestInterval(0, 100);
        subject.remove(resource, resource);
        assertEquals(copy, subject);
    }

    @Test
    public void test_remove_nonexistant() {
        NestedIntervalMap<TestInterval, TestInterval> copy = new NestedIntervalMap<TestInterval, TestInterval>(subject, TestInterval.STRATEGY);

        subject.remove(new TestInterval(0, 100));
        assertEquals(copy, subject);

        subject.remove(new TestInterval(1, 7));
        assertEquals(copy, subject);

        subject.remove(new TestInterval(12, 12));
        assertEquals(copy, subject);
    }

    @Test
    public void test_equals_hashcode() {
        assertFalse(subject.equals(null));
        assertEquals(subject, subject);
        assertFalse(subject.equals(new Object()));
        assertFalse(subject.equals(new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY)));

        assertEquals(subject.hashCode(), subject.hashCode());
        assertFalse(subject.hashCode() == new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY).hashCode());
    }

    @Test
    public void test_find_all_less_specific() {
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(new TestInterval(0, 100)));
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(new TestInterval(5, 13)));
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(N1_12));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findAllLessSpecific(N6_6));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findAllLessSpecific(N8_8));
        assertEquals(asList(N1_12, N1_4), subject.findAllLessSpecific(N2_2));
    }

    @Test
    public void test_find_exact_and_all_less_specific() {
        assertEquals(Collections.emptyList(), subject.findExactAndAllLessSpecific(new TestInterval(0, 100)));
        assertEquals(Collections.emptyList(), subject.findExactAndAllLessSpecific(new TestInterval(5, 13)));
        assertEquals(asList(N1_12), subject.findExactAndAllLessSpecific(N1_12));
        assertEquals(asList(N1_12, N5_10, N5_8, N6_6), subject.findExactAndAllLessSpecific(N6_6));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findExactAndAllLessSpecific(N8_8));
        assertEquals(asList(N1_12, N1_4, N2_2), subject.findExactAndAllLessSpecific(N2_2));
    }

    @Test
    public void test_find_exact_or_first_less_specific() {
        assertThat(subject.findExactOrFirstLessSpecific(new TestInterval(0, 100)), nullValue());
        assertThat(subject.findExactOrFirstLessSpecific(new TestInterval(5, 13)), nullValue());

        assertThat(subject.findExactOrFirstLessSpecific(N1_12), is(N1_12));
        assertThat(subject.findExactOrFirstLessSpecific(N6_6), is(N6_6));
        assertThat(subject.findExactOrFirstLessSpecific(N8_8), is(N5_8));
        assertThat(subject.findExactOrFirstLessSpecific(N2_2), is(N2_2));
    }

    @Test
    public void testFindFirstLessSpecific() {
        assertThat(subject.findFirstLessSpecific(N1_12), nullValue());

        assertThat(subject.findFirstLessSpecific(N6_6), is(N5_8));
        assertThat(subject.findFirstLessSpecific(N8_8), is(N5_8));
        assertThat(subject.findFirstLessSpecific(N2_2), is(N1_4));
        assertThat(subject.findFirstLessSpecific(new TestInterval(3, 7)), is(N1_12));
    }

    @Test
    public void testFindEverything() {
        assertEquals(all, subject.findExactAndAllMoreSpecific(TestInterval.MAX_RANGE));
        subject.put(TestInterval.MAX_RANGE, TestInterval.MAX_RANGE);
    }

    @Test
    public void testFindFirstMoreSpecific() {
        assertEquals(asList(N5_8, N9_10), subject.findFirstMoreSpecific(N5_10));
        assertEquals(asList(N1_1, N2_2, N3_4), subject.findFirstMoreSpecific(N1_4));
        assertEquals(asList(N7_7, N9_9), subject.findFirstMoreSpecific(new TestInterval(7, 9)));
        assertEquals(asList(N9_9), subject.findFirstMoreSpecific(new TestInterval(8, 9)));
    }

    @Test
    public void testFindExact() {
        for (TestInterval n : all) {
            assertThat(subject.findExact(n), is(n));
        }
    }

    @Test
    public void testFindAllMoreSpecific() {
        assertEquals(all.subList(1, all.size()), subject.findAllMoreSpecific(N1_12));
        assertEquals(asList(N3_4, N3_3, N4_4, N5_5, N6_6, N7_7), subject.findAllMoreSpecific(new TestInterval(3, 7)));
        assertEquals(asList(N9_9), subject.findAllMoreSpecific(new TestInterval(8, 9)));
    }

    @Test
    public void testFindExactAndAllMoreSpecific() {
        assertEquals(all, subject.findExactAndAllMoreSpecific(N1_12));
        assertEquals(asList(N1_4, N1_1, N2_2, N3_4, N3_3, N4_4), subject.findExactAndAllMoreSpecific(N1_4));
    }

    @Test
    public void detect_overlap_on_lower_bound_of_new_interval() {
        TestInterval child1 = new TestInterval(1, 10);
        TestInterval child2 = new TestInterval(11, 15);
        TestInterval child3 = new TestInterval(16, 25);
        TestInterval overlap = new TestInterval(8, 30);

        NestedIntervalMap<TestInterval, TestInterval> test = new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY);
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(overlap, overlap);
            fail("OverlappingIntervalException expected");
        } catch (OverlappingIntervalException expected) {
            assertEquals(overlap, expected.getInterval());
            assertEquals(asList(child1), expected.getOverlaps());
        }

        assertThat(subject.findExact(overlap), nullValue());
    }

    @Test
    public void detect_overlap_on_upper_bound_of_new_interval() {
        TestInterval child1 = new TestInterval(1, 10);
        TestInterval child2 = new TestInterval(11, 15);
        TestInterval child3 = new TestInterval(16, 25);
        TestInterval overlap = new TestInterval(1, 21);

        NestedIntervalMap<TestInterval, TestInterval> test = new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY);
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(overlap, overlap);
            fail("OverlappingIntervalException expected");
        } catch (OverlappingIntervalException expected) {
            assertEquals(overlap, expected.getInterval());
            assertEquals(asList(child3), expected.getOverlaps());
        }

        assertThat(subject.findExact(overlap), nullValue());
    }

    @Test
    public void detect_overlap_on_lower_and_upper_bound_of_new_interval() {
        TestInterval child1 = new TestInterval(1, 10);
        TestInterval child2 = new TestInterval(11, 15);
        TestInterval child3 = new TestInterval(16, 25);
        TestInterval overlap = new TestInterval(4, 21);

        NestedIntervalMap<TestInterval, TestInterval> test = new NestedIntervalMap<TestInterval, TestInterval>(TestInterval.STRATEGY);
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(overlap, overlap);
            fail("OverlappingIntervalException expected");
        } catch (OverlappingIntervalException expected) {
            assertEquals(overlap, expected.getInterval());
            assertEquals(asList(child1, child3), expected.getOverlaps());
        }

        assertThat(subject.findExact(overlap), nullValue());
    }
}
