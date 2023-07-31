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
package net.ripe.ipresource.jdk17;


import net.ripe.ipresource.IpResourceType;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import static net.ripe.ipresource.jdk17.NumberResourceBlock.mergeable;
import static net.ripe.ipresource.jdk17.NumberResourceBlock.parse;
import static net.ripe.ipresource.jdk17.NumberResourceSet.ALL_PRIVATE_USE_RESOURCES;
import static net.ripe.ipresource.jdk17.NumberResourceSet.empty;
import static net.ripe.ipresource.jdk17.NumberResourceSet.universal;
import static org.junit.Assert.*;

public class NumberResourceSetTest {

    public static final int RANDOM_SIZE = 250;

    private final Random random = new Random(123);

    @Test
    public void builder_can_only_be_used_once() {
        NumberResourceSet.Builder builder = new NumberResourceSet.Builder();
        builder.build();

        NumberResourceBlock address = parse("10.0.0.1/32");

        assertThrows(IllegalStateException.class, () -> builder.add(address));
        assertThrows(IllegalStateException.class, () -> builder.remove(address));
        assertThrows(IllegalStateException.class, () -> builder.addAll(ALL_PRIVATE_USE_RESOURCES));
        assertThrows(IllegalStateException.class, () -> builder.removeAll(ALL_PRIVATE_USE_RESOURCES));
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    public void should_share_from_ImmutableResourceSet() {
        assertSame(NumberResourceSet.empty(), NumberResourceSet.of(NumberResourceSet.parse("")));
        NumberResourceSet resources = NumberResourceSet.parse("10.0.0.0/8");
        assertSame(resources, NumberResourceSet.of(resources));
    }

    @Test
    public void should_have_constants_for_private_use_resources() {
        assertEquals("AS64512-AS65534, AS4200000000-AS4294967294", NumberResourceSet.ASN_PRIVATE_USE_RESOURCES.toString());
        assertEquals("10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, fc00::/7", NumberResourceSet.IP_PRIVATE_USE_RESOURCES.toString());
        assertEquals(
            "AS64512-AS65534, AS4200000000-AS4294967294, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, fc00::/7",
            NumberResourceSet.ALL_PRIVATE_USE_RESOURCES.toString()
        );
    }

    @Test
    public void containsAllIpv4Resources() {
        NumberResourceSet resources = NumberResourceSet.of(parse("0.0.0.0/0"));
        assertEquals("0.0.0.0/0", resources.toString());
    }

    @Test
    public void shouldNormalizeAccordingToRfc3779() {
        NumberResourceSet resources = NumberResourceSet.of(
            parse("127.0.0.1/32"),
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
        NumberResourceSet resources = NumberResourceSet.parse("127.0.0.1-127.0.0.1");
        assertEquals("127.0.0.1", resources.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_lowerPartFirst() {
        NumberResourceSet subject = empty()
            .add(parse("10.0.0.0/9"))
            .add(parse("10.128.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_higherPartFirst() {
        NumberResourceSet subject = empty()
            .add(parse("10.128.0.0/9"))
            .add(parse("10.0.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());

    }

    @Test
    public void shouldCheckForType() {
        NumberResourceSet subject = NumberResourceSet.of(parse("AS13"));
        assertTrue(subject.containsType(IpResourceType.ASN));
        assertFalse(subject.containsType(IpResourceType.IPv4));
        assertFalse(subject.containsType(IpResourceType.IPv6));
    }

    @Test
    public void shouldNormalizeUniqueResources() {
        NumberResourceSet subject = NumberResourceSet.of(parse("AS1-AS10"));
        assertEquals(AsnBlock.class, subject.iterator().next().getClass());

        subject = subject.remove(parse("AS2-AS10"));
        assertEquals(AsnBlock.class, subject.iterator().next().getClass());
        assertEquals("AS1", subject.toString());
    }

    @Test
    public void shouldMergeOverlappingResources() {
        NumberResourceSet subject = empty()
            .add(parse("AS5-AS13"))
            .add(parse("AS3-AS8"));

        assertEquals("AS3-AS13", subject.toString());
    }

    @Test
    public void parseShouldIgnoreWhitespace() {
        assertEquals(NumberResourceSet.parse("127.0.0.1,AS3333"), NumberResourceSet.parse("\t   \n127.0.0.1,   AS3333"));
    }

    @Test
    public void testContains() {
        NumberResourceSet a = NumberResourceSet.parse("10.0.0.0/8,192.168.0.0/16");
        assertTrue(a.contains(a));
        assertTrue(a.contains(empty()));
        assertTrue(empty().contains(empty()));
        assertFalse(empty().contains(NumberResourceSet.parse("10.0.0.0/24")));

        assertTrue(a.contains(NumberResourceSet.parse("10.0.0.0/24")));
        assertTrue(a.contains(NumberResourceSet.parse("192.168.1.131/32")));
        assertFalse(a.contains(NumberResourceSet.parse("127.0.0.1/32")));
        assertFalse(a.contains(NumberResourceSet.parse("192.168.0.0-192.172.255.255")));
        assertFalse(a.contains(NumberResourceSet.parse("10.0.0.1/32,192.168.0.0-192.172.255.255")));
    }

    @Test
    public void testRemove() {
        NumberResourceSet a = NumberResourceSet.parse("AS3333-AS4444,10.0.0.0/8").remove(parse("10.5.0.0/16"));
        assertEquals(NumberResourceSet.parse("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255"), a);

        assertTrue(a.contains(parse("AS3333-AS4444")));

        a = a.remove(parse("2000::/16"));
        assertEquals("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255", a.toString());
    }

    @Test
    public void test_difference() {
        NumberResourceSet a = NumberResourceSet.parse("AS3333-AS4444,10.0.0.0/8");
        NumberResourceSet difference = a.difference(NumberResourceSet.parse("10.5.0.0/16, AS3335-AS3335"));
        assertEquals(NumberResourceSet.parse("AS3333-AS3334, AS3336-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255"), difference);
    }

    @Test
    public void test_intersection() {
        NumberResourceSet empty = NumberResourceSet.parse("");
        assertEquals("", empty.intersection(NumberResourceSet.parse("AS1-AS10,AS3300-AS4420,10.0.0.0/9")).toString());

        NumberResourceSet a = NumberResourceSet.parse("AS8-AS3315,AS3333-AS4444,10.0.0.0/8");
        a = a.intersection(NumberResourceSet.parse("AS1-AS10,AS3300-AS4420,10.0.0.0/9"));
        assertEquals(NumberResourceSet.parse("AS8-AS10,AS3300-AS3315,AS3333-AS4420,10.0.0.0/9"), a);

        a = a.intersection(NumberResourceSet.parse("AS3300-AS3320"));
        assertEquals("AS3300-AS3315", a.toString());

        a = a.intersection(NumberResourceSet.parse("AS3300-AS3320, 10.0.0.0/9"));
        assertEquals("AS3300-AS3315", a.toString());

        a = a.intersection(empty);
        assertTrue(a.isEmpty());
        assertEquals("", a.toString());
    }

    @Test
    public void shouldNormalizeRetainedResources() {
        // Without normalization on difference the single IP resource was retained as the range AS64513-AS64513.
        NumberResourceSet subject = NumberResourceSet.parse("AS64513");
        assertEquals("AS64513", subject.intersection(ALL_PRIVATE_USE_RESOURCES).toString());
    }

    @Test
    public void test_removal_of_multiple_overlapping_resources() {
        NumberResourceSet subject = NumberResourceSet.parse("AS1-AS3, AS5-AS10, AS13-AS15")
            .remove(parse("AS1-AS10"));
        assertEquals("AS13-AS15", subject.toString());
    }

    @Test
    public void test_intersects() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);

            assertFalse(a.intersects(empty()));
            assertFalse(a.intersects(a.complement()));
            assertTrue(a.isEmpty() || a.intersects(a));
            assertTrue(a.isEmpty() || a.intersects(universal()));
        }

        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);

            assertTrue(a.isEmpty() || b.isEmpty() || (a.union(b).intersects(a) && a.union(b).intersects(b)));
            assertNotEquals(a.intersection(b).isEmpty(), a.intersects(b));
        }
    }

    @Test
    public void union_is_associative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);
            NumberResourceSet c = randomSet(i);

            assertEquals(a.union(b).union(c), a.union(b.union(c)));
        }
    }

    @Test
    public void union_is_commutative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);

            assertEquals(a.union(b), b.union(a));
        }
    }

    @Test
    public void intersection_is_associative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);
            NumberResourceSet c = randomSet(i);

            assertEquals(a.intersection(b).intersection(c), a.intersection(b.intersection(c)));
        }
    }

    @Test
    public void intersection_is_commutative() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);

            assertEquals(a.intersection(b), b.intersection(a));
        }
    }

    @Test
    public void union_and_intersection_are_distributive() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);
            NumberResourceSet c = randomSet(i);

            assertEquals(a.union(b.intersection(c)), (a.union(b)).intersection(a.union(c)));
            assertEquals(a.intersection(b.union(c)), (a.intersection(b)).union(a.intersection(c)));
        }
    }

    @Test
    public void identity_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);

            assertEquals(a, a.union(empty()));
            assertEquals(a, a.intersection(universal()));
        }
    }

    @Test
    public void complement_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);

            assertEquals(universal(), a.union(a.complement()));
            assertEquals(empty(), a.intersection(a.complement()));
        }
    }

    // Proposition 9 of https://www.umsl.edu/~siegelj/SetTheoryandTopology/The_algebra_of_sets.html
    @Test
    public void difference_laws() {
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            NumberResourceSet a = randomSet(i);
            NumberResourceSet b = randomSet(i);
            NumberResourceSet c = randomSet(i);

            assertEquals(c.difference(a.intersection(b)), (c.difference(a)).union(c.difference(b)));
            assertEquals(c.difference(a.union(b)), (c.difference(a)).intersection(c.difference(b)));
            assertEquals(c.difference(b.difference(a)), (a.intersection(c)).union(c.difference(b)));
        }

    }

    @Test
    public void randomized_testing() {
        NumberResourceSet subject = empty();
        var ranges = new ArrayList<NumberResourceBlock>();
        Random random = new Random();
        for (int i = 0; i < RANDOM_SIZE; ++i) {
            var start = Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
            var end = start + Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE / 256)) + 1;
            var range = AsnBlock.range(Asn.of(start), Asn.of(end));
            ranges.add(range);
            subject = subject.add(range);
        }

        for (var range: ranges) {
            assertTrue(range + " contained in set", subject.contains(range));
        }

        var iterator = subject.iterator();
        var previous = iterator.next();
        while (iterator.hasNext()) {
            var next = iterator.next();
            assertTrue("resources out of order <" + previous + "> not before <" + next + ">", previous.compareTo(next) < 0);
            assertFalse("no mergeable resource in set", mergeable(previous, next));
            previous = next;
        }

        for (var range: ranges) {
            subject = subject.remove(range);
        }

        assertTrue("all resources removed: " + subject, subject.isEmpty());
    }

    private NumberResourceSet randomSet(int size) {
        return Stream.generate(this::randomResourceRange)
            .limit(random.nextInt(size + 1))
            .collect(NumberResourceSet.collector());
    }

    private NumberResourceBlock randomResourceRange() {
        IpResourceType type = IpResourceType.values()[random.nextInt(IpResourceType.values().length)];
        return switch (type) {
            case ASN -> {
                var start = Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
                var end = start + Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE / 256)) + 1;
                yield AsnBlock.range(Asn.of(start), Asn.of(end));
            }
            case IPv4 -> {
                if (random.nextInt(5) == 0) {
                    var prefix = Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
                    yield Ipv4Prefix.prefix(prefix, Ipv4Address.NUMBER_OF_BITS - Long.numberOfTrailingZeros(prefix));
                } else {
                    var start = Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
                    var end = start + Integer.toUnsignedLong(random.nextInt(Integer.MAX_VALUE / 256)) + 1;
                    yield Ipv4Block.of(Ipv4Address.of(start), Ipv4Address.of(end));
                }
            }
            case IPv6 -> {
                if (random.nextInt(5) == 0) {
                    var prefix = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1)).multiply(BigInteger.valueOf(random.nextInt(10_000_000)));
                    int numberOfTrailingZeros = Math.max(0, prefix.getLowestSetBit());
                    yield Ipv6Prefix.prefix(Ipv6Address.of(prefix), Ipv6Address.NUMBER_OF_BITS - numberOfTrailingZeros);
                } else {
                    var start = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1)).multiply(BigInteger.valueOf(random.nextInt(10_000_000)));
                    var end = start.add(BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE / 256)).multiply(BigInteger.valueOf(random.nextInt(10_000_000)))).add(BigInteger.ONE);
                    yield Ipv6Block.of(Ipv6Address.of(start), Ipv6Address.of(end));
                }
            }
        };
    }
}
