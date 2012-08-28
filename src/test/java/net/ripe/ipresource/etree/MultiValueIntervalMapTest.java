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

import java.util.List;

import net.ripe.ipresource.etree.MultiValueIntervalMap;

import org.junit.Before;
import org.junit.Test;

public class MultiValueIntervalMapTest {

    private MultiValueIntervalMap<TestInterval, String> subject;
    private TestInterval k_11;
    private TestInterval k_12;
    private TestInterval k_13;
    private TestInterval k_14;

    private String v_11 = "1-1";
    private String v_121 = "1-2 1";
    private String v_122 = "1-2 2";
    private String v_131 = "1-3 1";
    private String v_132 = "1-3 2";
    private String v_133 = "1-3 3";

    @Before
    public void setUp() throws Exception {
        subject = new MultiValueIntervalMap<TestInterval, String>();

        k_11 = new TestInterval(1, 1);
        k_12 = new TestInterval(1, 2);
        k_13 = new TestInterval(1, 3);
        k_14 = new TestInterval(1, 4);

        subject.put(k_11, v_11);

        subject.put(k_12, v_122);
        subject.put(k_12, v_121);

        subject.put(k_13, v_131);
        subject.put(k_13, v_133);
        subject.put(k_13, v_132);
    }

    @Test
    public void findExact_k11() {
        final List<String> result = subject.findExact(k_11);
        assertThat(result, contains(v_11));
    }

    @Test
    public void findExact_k13() {
        final List<String> result = subject.findExact(k_13);
        assertThat(result, contains(v_131, v_132, v_133));
    }

    @Test
    public void remove() {
        subject.remove(k_11);

        final List<String> result = subject.findExact(k_11);
        assertThat(result, hasSize(0));
    }

    @Test
    public void remove_with_value() {
        subject.remove(k_12, v_122);
        assertThat(subject.findExact(k_12), contains(v_121));

        subject.remove(k_12, v_121);
        assertThat(subject.findExact(k_12), hasSize(0));

    }

    @Test
    public void remove_with_value_key_unknown() {
        subject.remove(k_14, v_11);
        final List<String> result = subject.findAllMoreSpecific(TestInterval.MAX_RANGE);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122, v_11));
    }

    @Test
    public void clear() {
        subject.clear();

        final List<String> result = subject.findAllMoreSpecific(TestInterval.MAX_RANGE);
        assertThat(result, hasSize(0));
    }

    @Test
    public void findFirstLessSpecific() {
        final List<String> result = subject.findFirstLessSpecific(k_11);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findExact() {
        final List<String> result = subject.findExact(k_12);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findExactOrFirstLessSpecific() {
        final List<String> result = subject.findExactOrFirstLessSpecific(k_12);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findAllLessSpecific() {
        final List<String> result = subject.findAllLessSpecific(k_11);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122));
    }

    @Test
    public void findExactAndAllLessSpecific() {
        final List<String> result = subject.findExactAndAllLessSpecific(k_12);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122));
    }

    @Test
    public void findFirstMoreSpecific() {
        final List<String> result = subject.findFirstMoreSpecific(k_12);
        assertThat(result, contains(v_11));
    }

    @Test
    public void findAllMoreSpecific() {
        final List<String> result = subject.findAllMoreSpecific(TestInterval.MAX_RANGE);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122, v_11));
    }

    @Test
    public void findExactAndAllMoreSpecific() {
        final List<String> result = subject.findExactAndAllMoreSpecific(k_12);
        assertThat(result, contains(v_121, v_122, v_11));
    }
}
