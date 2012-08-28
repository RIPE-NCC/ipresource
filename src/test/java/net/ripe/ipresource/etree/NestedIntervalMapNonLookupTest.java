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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ripe.ipresource.etree.NestedIntervalMap;
import net.ripe.ipresource.etree.OverlappingIntervalException;

import org.apache.commons.lang.Validate;
import org.junit.Test;

public class NestedIntervalMapNonLookupTest {

    private static final Map<String, TestInterval> cache = new HashMap<String, TestInterval>();

    private static final TestInterval node(Object toStringify) {
        return node(toStringify.toString());
    }

    private static final TestInterval node(String node) {
        Validate.isTrue(cache.containsKey(node), "Node " + node + " not in cache");
        return cache.get(node);
    }

    static {
        for (int i = 0; i <= 10; i++) {
            for (int j = i; j <= 10; j++) {
                TestInterval resource = new TestInterval(i, j);
                cache.put(String.format("%d-%d", i, j), resource);
                if (i == j) {
                    cache.put(Integer.toString(i), resource);
                }
            }
        }
    }

    private NestedIntervalMap<TestInterval, TestInterval> subject = new NestedIntervalMap<TestInterval, TestInterval>();

    private List<TestInterval> allNodes() {
        return subject.findAllMoreSpecific(TestInterval.MAX_RANGE);
    }

    private TestInterval add(TestInterval node) {
        subject.put(node, node);
        return node;
    }

    /* Single node */
    @Test(expected = IllegalArgumentException.class)
    public void addingNullShouldFail() {
        add(null);
    }

    @Test
    public void addSingleEntryNode() {
        TestInterval node = node(1);
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertNotNull(subject.findExact(node));
    }

    @Test
    public void addRangedNode() {
        TestInterval node = node("1-2");
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertNotNull(subject.findExact(node));
    }

    /* Siblings - root - simple */
    @Test
    public void addSiblingToRoot() {
        TestInterval node1 = add(node(1));
        TestInterval node2 = add(node(2));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
    }

    @Test
    public void addSiblingRangeLeftOfExisting() {
        add(node("3-4"));
        TestInterval node = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeRightOfExisting() {
        add(node("1-2"));
        TestInterval node = add(node("3-4"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeBetweenExisting() {
        add(node("1-2"));
        add(node("5-6"));
        TestInterval node = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    /* Children */
    @Test
    public void addChild() {
        TestInterval parent = add(node("1-10"));
        TestInterval node = add(node("2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToFirstExisting() {
        TestInterval parent = add(node("1-2"));
        add(node("3-4"));
        TestInterval node = add(node("2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToLastExisting() {
        add(node("1-2"));
        TestInterval parent = add(node("3-4"));
        TestInterval node = add(node("4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToMiddleExisting() {
        add(node("1-2"));
        TestInterval parent = add(node("3-4"));
        add(node("5-6"));
        TestInterval node = add(node("4"));

        assertThat(allNodes(), hasSize(4));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildrenClearBounds() {
        List<TestInterval> parents = new ArrayList<TestInterval>();
        parents.add(add(node("1-7")));
        parents.add(add(node("2-6")));
        parents.add(add(node("3-5")));
        TestInterval node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));
        for (TestInterval parent : parents) {
            assertNotNull(subject.findExact(parent));
        }
        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenLeftBounds() {
        List<TestInterval> parents = new ArrayList<TestInterval>();
        parents.add(add(node("1-4")));
        parents.add(add(node("1-3")));
        parents.add(add(node("1-2")));
        TestInterval node = add(node("1"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenRightBounds() {
        List<TestInterval> parents = new ArrayList<TestInterval>();
        parents.add(add(node("1-4")));
        parents.add(add(node("2-4")));
        parents.add(add(node("3-4")));
        TestInterval node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    /* Create parent */
    @Test
    public void addParentForSingleNode() {
        TestInterval node = add(node("1"));
        TestInterval parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentForMultipleNode() {
        TestInterval node1 = add(node(1));
        TestInterval node2 = add(node(2));
        TestInterval parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node1), hasItems(parent));
        assertThat(subject.findAllLessSpecific(node2), hasItems(parent));
    }

    @Test
    public void addParentInbetweenTwoNodes() {
        TestInterval parent = add(node("1-4"));
        add(node(2));
        TestInterval node = add(node("2-3"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentStartOverlap() {
        TestInterval node = add(node(2));
        add(node(3));
        TestInterval parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(parent));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentEndOverlap() {
        add(node(2));
        TestInterval node = add(node(3));
        TestInterval parent = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(parent));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentMiddleOverlap() {
        add(node(1));
        TestInterval node1 = add(node(2));
        TestInterval node2 = add(node(3));
        add(node(4));
        TestInterval parent = add(node("2-3"));

        assertThat(allNodes(), hasSize(5));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node1), hasItems(parent));
        assertThat(subject.findAllLessSpecific(node2), hasItems(parent));
    }

    /* Test clone + equals */
    @Test
    public void cloneTree() {
        add(node(1));
        add(node(2));
        add(node(3));
        add(node("1-2"));

        assertThat(allNodes(), hasSize(4));

        NestedIntervalMap<TestInterval, TestInterval> original = subject;
        subject = new NestedIntervalMap<TestInterval, TestInterval>(original);

        assertThat(allNodes(), hasSize(4));
        assertThat(original, is(subject));
        for (TestInterval node : allNodes()) {
            assertNotNull(subject.findExact(node));
        }

        add(node(4));

        assertFalse(original.equals(subject));
        assertFalse(original.equals(null));
        assertFalse(original.equals("foo"));
    }

    /* Test overlaps */
    @Test(expected = OverlappingIntervalException.class)
    public void leftOverlap() {
        add(node("2-3"));
        add(node("1-2"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void rightOverlap() {
        add(node("2-3"));
        add(node("3-4"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void fullLeftOverlap() {
        add(node("2-4"));
        add(node("1-3"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void fullRightOverlap() {
        add(node("2-4"));
        add(node("3-5"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void childLeftOverlap() {
        add(node("2"));
        add(node("3-4"));
        add(node("1-3"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void childRightOverlap() {
        add(node("2-3"));
        add(node("4"));
        add(node("3-5"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void childOverlapMiddleLeft() {
        add(node("2-3"));
        add(node("4"));
        add(node("5-6"));
        add(node("2-5"));
    }

    @Test(expected = OverlappingIntervalException.class)
    public void childOverlapMiddleRight() {
        add(node("2-3"));
        add(node("4"));
        add(node("5-6"));
        add(node("3-6"));
    }

    @Test
    public void clear() {
        add(node("1-5"));
        add(node("6-10"));
        add(node("2"));
        add(node("5"));
        add(node("6"));
        add(node("7"));
        add(node("8"));
        assertThat(allNodes(), hasSize(7));

        subject.clear();

        assertThat(allNodes(), hasSize(0));
    }
}
