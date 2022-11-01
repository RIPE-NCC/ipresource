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

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static net.ripe.ipresource.ImmutableResourceSet.empty;
import static net.ripe.ipresource.ImmutableResourceSet.universal;
import static net.ripe.ipresource.IpResource.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImmutableResourceSetTest {

    public static final int RANDOM_SIZE = 250;

    private final Random random = new Random();

    @Test
    public void containsAllIpv4Resources() {
        ImmutableResourceSet resources = new ImmutableResourceSet(parse("0.0.0.0/0"));
        assertEquals("0.0.0.0/0", resources.toString());
    }

    @Test
    public void shouldNormalizeAccordingToRfc3779() {
        ImmutableResourceSet resources = new ImmutableResourceSet(
            parse("127.0.0.1"),
            parse("10.0.0.0/8"),
            parse("255.255.255.255-255.255.255.255"),
            parse("193.0.0.0/8"),
            parse("194.0.0.0/8"),
            parse("194.16.0.0/16"),
            parse("195.0.0.0/8"),
            parse("195.1.0.0-196.255.255.255")
        );
        assertEquals("10.0.0.0/8, 127.0.0.1, 193.0.0.0-196.255.255.255, 255.255.255.255", resources.toString());
    }

    @Test
    public void shouldNormalizeSingletonRangeToUniqueIpResource() {
        IpResourceSet resources = new IpResourceSet(parse("127.0.0.1-127.0.0.1"));
        assertEquals("127.0.0.1", resources.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_lowerPartFirst() {
        ImmutableResourceSet subject = empty()
            .add(parse("10.0.0.0/9"))
            .add(parse("10.128.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_higherPartFirst() {
        ImmutableResourceSet subject = empty()
            .add(parse("10.128.0.0/9"))
            .add(parse("10.0.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());

    }

    @Test
    public void shouldCheckForType() {
        ImmutableResourceSet subject = ImmutableResourceSet.of(parse("AS13"));
        assertTrue(subject.containsType(IpResourceType.ASN));
        assertFalse(subject.containsType(IpResourceType.IPv4));
        assertFalse(subject.containsType(IpResourceType.IPv6));
    }

    @Test
    public void shouldNormalizeUniqueResources() {
        ImmutableResourceSet subject = ImmutableResourceSet.of(parse("AS1-AS10"));
        assertEquals(IpResourceRange.class, subject.iterator().next().getClass());

        subject = subject.remove(parse("AS2-AS10"));
        assertEquals(Asn.class, subject.iterator().next().getClass());
        assertEquals("AS1", subject.toString());
    }

    @Test
    public void shouldMergeOverlappingResources() {
        ImmutableResourceSet subject = empty()
            .add(parse("AS5-AS13"))
            .add(parse("AS3-AS8"));

        assertEquals("AS3-AS13", subject.toString());
    }

    @Test
    public void parseShouldIgnoreWhitespace() {
        assertEquals(ImmutableResourceSet.parse("127.0.0.1,AS3333"), ImmutableResourceSet.parse("\t   \n127.0.0.1,   AS3333"));
    }

    @Test
    public void testContains() {
        ImmutableResourceSet a = ImmutableResourceSet.parse("10.0.0.0/8,192.168.0.0/16");
        assertTrue(a.contains(a));
        assertTrue(a.contains(empty()));
        assertTrue(empty().contains(empty()));
        assertFalse(empty().contains(ImmutableResourceSet.parse("10.0.0.0/24")));

        assertTrue(a.contains(ImmutableResourceSet.parse("10.0.0.0/24")));
        assertTrue(a.contains(ImmutableResourceSet.parse("192.168.1.131")));
        assertFalse(a.contains(ImmutableResourceSet.parse("127.0.0.1")));
        assertFalse(a.contains(ImmutableResourceSet.parse("192.168.0.0-192.172.255.255")));
        assertFalse(a.contains(ImmutableResourceSet.parse("10.0.0.1,192.168.0.0-192.172.255.255")));
    }

    @Test
    public void testRemove() {
        ImmutableResourceSet a = ImmutableResourceSet.parse("AS3333-AS4444,10.0.0.0/8").remove(IpResource.parse("10.5.0.0/16"));
        assertEquals(ImmutableResourceSet.parse("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255"), a);

        assertTrue(a.contains(IpResource.parse("AS3333-AS4444")));

        a = a.remove(parse("2000::/16"));
        assertEquals("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255", a.toString());
    }

    @Test
    public void testRemoveAll() {
        IpResourceSet a = IpResourceSet.parse("AS3333-AS4444,10.0.0.0/8");
        a.removeAll(IpResourceSet.parse("10.5.0.0/16, AS3335"));
        assertEquals(IpResourceSet.parse("AS3333-AS3334, AS3336-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255"), a);
    }

    @Test
    public void testRetainAll() {
        IpResourceSet empty = IpResourceSet.parse("");
        empty.retainAll(IpResourceSet.parse("AS1-AS10,AS3300-AS4420,10.0.0.0/9"));
        assertEquals("", empty.toString());

        IpResourceSet a = IpResourceSet.parse("AS8-AS3315,AS3333-AS4444,10.0.0.0/8");
        a.retainAll(IpResourceSet.parse("AS1-AS10,AS3300-AS4420,10.0.0.0/9"));
        assertEquals(IpResourceSet.parse("AS8-AS10,AS3300-AS3315,AS3333-AS4420,10.0.0.0/9"), a);

        a.retainAll(IpResourceSet.parse("AS3300-AS3320"));
        assertEquals("AS3300-AS3315", a.toString());

        a.retainAll(IpResourceSet.parse("AS3300-AS3320, 10.0.0.0/9"));
        assertEquals("AS3300-AS3315", a.toString());

        a.retainAll(empty);
        assertTrue(a.isEmpty());
        assertEquals("", a.toString());
    }

    @Test
    public void shouldNormalizeRetainedResources() {
        // Without normalization on retainAll the single IP resource was retained as the range AS64513-AS64513.
        IpResourceSet subject = IpResourceSet.parse("AS64513");
        subject.retainAll(IpResourceSet.ALL_PRIVATE_USE_RESOURCES);
        assertEquals("AS64513", subject.toString());
    }

    @Test
    public void test_removal_of_multiple_overlapping_resources() {
        ImmutableResourceSet subject = ImmutableResourceSet.parse("AS1-AS3, AS5-AS10, AS13-AS15")
            .remove(IpResource.parse("AS1-AS10"));
        assertEquals("AS13-AS15", subject.toString());
    }

    @Test
    public void test_intersects() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);

            assertFalse(a.intersects(empty()));
            assertFalse(a.intersects(a.complement()));
            assertTrue(a.isEmpty() || a.intersects(a));
            assertTrue(a.isEmpty() || a.intersects(universal()));
        }

        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);

            assertTrue(a.isEmpty() || b.isEmpty() || (a.union(b).intersects(a) && a.union(b).intersects(b)));
            assertTrue(a.intersection(b).isEmpty() != a.intersects(b));
        }
    }

    @Test
    public void union_is_associative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);
            ImmutableResourceSet c = randomSet(i);

            assertEquals(a.union(b).union(c), a.union(b.union(c)));
        }
    }

    @Test
    public void union_is_commutative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);

            assertEquals(a.union(b), b.union(a));
        }
    }

    @Test
    public void intersection_is_associative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);
            ImmutableResourceSet c = randomSet(i);

            assertEquals(a.intersection(b).intersection(c), a.intersection(b.intersection(c)));
        }
    }

    @Test
    public void intersection_is_commutative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);

            assertEquals(a.intersection(b), b.intersection(a));
        }
    }

    @Test
    public void union_and_intersection_are_distributive() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);
            ImmutableResourceSet c = randomSet(i);

            assertEquals(a.union(b.intersection(c)), (a.union(b)).intersection(a.union(c)));
            assertEquals(a.intersection(b.union(c)), (a.intersection(b)).union(a.intersection(c)));
        }
    }

    @Test
    public void identity_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);

            assertEquals(a, a.union(empty()));
            assertEquals(a, a.intersection(universal()));
        }
    }

    @Test
    public void complement_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);

            assertEquals(universal(), a.union(a.complement()));
            assertEquals(empty(), a.intersection(a.complement()));
        }
    }

    // Proposition 9 of https://www.umsl.edu/~siegelj/SetTheoryandTopology/The_algebra_of_sets.html
    @Test
    public void difference_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            ImmutableResourceSet a = randomSet(i);
            ImmutableResourceSet b = randomSet(i);
            ImmutableResourceSet c = randomSet(i);

            assertEquals(c.difference(a.intersection(b)), (c.difference(a)).union(c.difference(b)));
            assertEquals(c.difference(a.union(b)), (c.difference(a)).intersection(c.difference(b)));
            assertEquals(c.difference(b.difference(a)), (a.intersection(c)).union(c.difference(b)));
        }

    }

    @Test
    public void randomized_testing() {
        ImmutableResourceSet subject = empty();
        List<IpResourceRange> ranges = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            BigInteger start = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
            BigInteger end = start.add(BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE / 256))).add(BigInteger.ONE);
            IpResourceRange range = IpResourceRange.range(new Asn(start), new Asn(end));
            ranges.add(range);
            subject = subject.add(range);
        }

        for (IpResourceRange range: ranges) {
            assertTrue(range + " contained in set", subject.contains(range));
        }

        Iterator<IpResource> iterator = subject.iterator();
        IpResource previous = iterator.next();
        while (iterator.hasNext()) {
            IpResource next = iterator.next();
            assertTrue("resources out of order <" + previous + "> not before <" + next + ">", previous.compareTo(next) < 0);
            assertFalse("no mergeable resource in set", previous.isMergeable(next));
            previous = next;
        }

        for (IpResourceRange range: ranges) {
            subject = subject.remove(range);
        }

        assertTrue("all resources removed: " + subject, subject.isEmpty());
    }

    private ImmutableResourceSet randomSet(int size) {
        return Stream.generate(this::randomResourceRange)
            .limit(random.nextInt(size + 1))
            .collect(ImmutableResourceSet.collector());
    }

    private IpResourceRange randomResourceRange() {
        IpResourceType type = IpResourceType.values()[random.nextInt(IpResourceType.values().length)];
        BigInteger start = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
        BigInteger end = start.add(BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE / 256))).add(BigInteger.ONE);
        return IpResourceRange.range(type.fromBigInteger(start), type.fromBigInteger(end));
    }
}
