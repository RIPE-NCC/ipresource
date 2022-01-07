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

import static net.ripe.ipresource.IpResource.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class IpResourceSetTest {

    private IpResourceSet subject = new IpResourceSet();

    @Test
    public void containsAllIpv4Resources() {
        IpResourceSet resources = new IpResourceSet();
        resources.add(parse("0.0.0.0/0"));
        assertEquals("0.0.0.0/0", resources.toString());
    }

    @Test
    public void shouldNormalizeAccordingToRfc3779() {
        IpResourceSet resources = new IpResourceSet();
        resources.add(parse("127.0.0.1"));
        resources.add(parse("10.0.0.0/8"));
        resources.add(parse("255.255.255.255-255.255.255.255"));
        resources.add(parse("193.0.0.0/8"));
        resources.add(parse("194.0.0.0/8"));
        resources.add(parse("194.16.0.0/16"));
        resources.add(parse("195.0.0.0/8"));
        resources.add(parse("195.1.0.0-196.255.255.255"));
        assertEquals("10.0.0.0/8, 127.0.0.1, 193.0.0.0-196.255.255.255, 255.255.255.255", resources.toString());
    }

    @Test
    public void shouldNormalizeSingletonRangeToUniqueIpResource() {
        IpResourceSet resources = new IpResourceSet(parse("127.0.0.1-127.0.0.1"));
        assertEquals("127.0.0.1", resources.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_lowerPartFirst() {
        subject.add(parse("10.0.0.0/9"));
        subject.add(parse("10.128.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());
    }

    @Test
    public void shouldMergeAdjacentResources_higherPartFirst() {
        subject.add(parse("10.128.0.0/9"));
        subject.add(parse("10.0.0.0/9"));

        assertEquals("10.0.0.0/8", subject.toString());

    }

    @Test
    public void shouldCheckForType() {
        subject.add(parse("AS13"));
        assertTrue(subject.containsType(IpResourceType.ASN));
        assertFalse(subject.containsType(IpResourceType.IPv4));
    }

    @Test
    public void shouldNormalizeUniqueResources() {
        subject.add(parse("AS1-AS10"));
        assertEquals(IpResourceRange.class, subject.iterator().next().getClass());

        subject.remove(parse("AS2-AS10"));
        assertEquals(Asn.class, subject.iterator().next().getClass());
    }

    @Test
    public void shouldMergeOverlappingResources() {
        subject.add(parse("AS5-AS13"));
        subject.add(parse("AS3-AS8"));

        assertEquals("AS3-AS13", subject.toString());
    }

    @Test
    public void parseShouldIgnoreWhitespace() {
        assertEquals(IpResourceSet.parse("127.0.0.1,AS3333"), IpResourceSet.parse("\t   \n127.0.0.1,   AS3333"));
    }

    @Test
    public void testContains() {
        IpResourceSet a = IpResourceSet.parse("10.0.0.0/8,192.168.0.0/16");
        assertTrue(a.contains(a));
        assertTrue(a.contains(new IpResourceSet()));
        assertTrue(new IpResourceSet().contains(new IpResourceSet()));
        assertFalse(new IpResourceSet().contains(IpResourceSet.parse("10.0.0.0/24")));

        assertTrue(a.contains(IpResourceSet.parse("10.0.0.0/24")));
        assertTrue(a.contains(IpResourceSet.parse("192.168.1.131")));
        assertFalse(a.contains(IpResourceSet.parse("127.0.0.1")));
        assertFalse(a.contains(IpResourceSet.parse("192.168.0.0-192.172.255.255")));
        assertFalse(a.contains(IpResourceSet.parse("10.0.0.1,192.168.0.0-192.172.255.255")));
    }

    @Test
    public void testRemove() {
        IpResourceSet a = IpResourceSet.parse("AS3333-AS4444,10.0.0.0/8");
        assertTrue(a.remove(IpResource.parse("10.5.0.0/16")));
        assertFalse(a.remove(IpResource.parse("10.5.0.0/16")));

        assertTrue(a.contains(IpResource.parse("AS3333-AS4444")));
        assertEquals("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255", a.toString());

        a.remove(parse("2000::/16"));
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
        subject = IpResourceSet.parse("AS1-AS3, AS5-AS10, AS13-AS15");
        subject.remove(IpResource.parse("AS1-AS10"));
        assertEquals("AS13-AS15", subject.toString());
    }

    @Test
    public void randomized_testing() {
        List<IpResourceRange> ranges = new ArrayList<IpResourceRange>();
        Random random = new Random();
        for (int i = 0; i < 1000; ++i) {
            BigInteger start = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE - Integer.MAX_VALUE / 256 - 1));
            BigInteger end = start.add(BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE / 256))).add(BigInteger.ONE);
            IpResourceRange range = IpResourceRange.range(new Asn(start), new Asn(end));
            ranges.add(range);
            subject.add(range);
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
            subject.remove(range);
        }

        assertTrue("all resources removed", subject.isEmpty());
    }
}
