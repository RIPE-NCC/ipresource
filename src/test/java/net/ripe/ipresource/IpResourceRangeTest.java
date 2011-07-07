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
package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class IpResourceRangeTest {

    private static final IpResourceRange RANGE_127_0_16 = IpResourceRange.parse("127.0.0.0/16");
    private static final IpResource RANGE_127_1_16 = IpResourceRange.parse("127.1.0.0/16");
    private static final IpResource RANGE_127_8 = IpResourceRange.parse("127.0.0.0/8");

    private static final IpResourceRange IPV6_RANGE_127_0_32 = IpResourceRange.parse("127:0::/32");
    private static final IpResourceRange IPV6_RANGE_127_1_32 = IpResourceRange.parse("127:1::/32");
    private static final IpResourceRange IPV6_RANGE_127_16 = IpResourceRange.parse("127::/16");

    private static final String IPV6_RANGE_STR = "2001:db8:8000::/33";
    private static final IpResourceRange IPV6_RANGE = IpResourceRange.parse(IPV6_RANGE_STR);

    @Test
    public void shouldParseIpv6Range() {
    	assertEquals(IPV6_RANGE_STR, IPV6_RANGE.toString());
    }


    @Test
    public void shouldSupportAsnRange() {
        assertEquals(new IpResourceRange(Asn.parse("AS3333"), Asn.parse("AS4444")), IpResourceRange.parse("AS3333-AS4444"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnEmptyRange() {
        IpResourceRange.parse("AS3333-AS2222");
    }

    @Test
    public void shouldParsePrefix() {
        assertEquals(Ipv4Address.parse("10.15.0.0"), IpResourceRange.parse("10.15.0.0/16").getStart());
        assertEquals(Ipv4Address.parse("10.15.255.255"), IpResourceRange.parse("10.15.0.0/16").getEnd());
    }

    @Test
    public void shouldParseRipePrefixNotation() {
        assertEquals("0.0.0.0/0", IpResourceRange.parse("0/0").toString());
    }

    @Test
    public void shouldParseCommaPrefixNotation() {
        assertEquals("10.0.0.0-10.1.1.129", IpResourceRange.parse("10.0.0.0/16,/24,/25,/31").toString());
    }

    @Test
    public void shouldParseNetmaskPrefixNotation() {
        assertEquals("193.0.0.0/19", IpResourceRange.parseWithNetmask("193.0.0.0", "255.255.224.0").toString());

        try {
            IpResourceRange.parseWithNetmask("193.0.0.0", "193.0.0.19");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void subtractRange() {
        assertEquals(Collections.singletonList(RANGE_127_0_16), RANGE_127_0_16.subtract(RANGE_127_1_16));
        assertEquals(Collections.emptyList(), RANGE_127_0_16.subtract(RANGE_127_8));
        assertEquals(Collections.singletonList(IpResourceRange.parse("127.1.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_0_16));
        assertEquals(Arrays.asList(RANGE_127_0_16, IpResourceRange.parse("127.2.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_1_16));
    }

    @Test
    public void singletonRangeShouldEqualUniqueIpv4Resource() {
    	singletonRangeShouldEqualUniqueIpResource("127.0.0.1");
    }

    @Test
    public void singletonRangeShouldEqualUniqueIpv6Resource() {
    	singletonRangeShouldEqualUniqueIpResource("127::1");
    }

    public void singletonRangeShouldEqualUniqueIpResource(String resource) {
        UniqueIpResource unique = UniqueIpResource.parse(resource);
        IpResourceRange range = IpResourceRange.range(unique, unique);
        assertEquals(range, unique);
        assertEquals(unique, range);
        assertEquals(range.hashCode(), unique.hashCode());
        assertEquals(unique.hashCode(), range.hashCode());
        assertEquals(0, unique.compareTo(range));
        assertEquals(0, range.compareTo(unique));
    }

    @Test
    public void shouldParseIPv6ClasslessNotation() {
    	IpResourceRange.parse("1:2:3::/64");
    }

    @Test
    public void shouldParseIpv6Prefix() {
        assertEquals(IpAddress.parse("10::"), IpResourceRange.parse("10::/16").getStart());
        assertEquals(IpAddress.parse("10:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"), IpResourceRange.parse("10::/16").getEnd());
    }

    @Test
    public void subtractIPv6Range() {
        assertEquals(Collections.singletonList(IPV6_RANGE_127_0_32), IPV6_RANGE_127_0_32.subtract(IPV6_RANGE_127_1_32));
        assertEquals(Collections.emptyList(), IPV6_RANGE_127_0_32.subtract(IPV6_RANGE_127_16));
        assertEquals(Collections.singletonList(IpResourceRange.parse("127:1::-127:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")), IPV6_RANGE_127_16.subtract(IPV6_RANGE_127_0_32));
        assertEquals(Arrays.asList(IPV6_RANGE_127_0_32, IpResourceRange.parse("127:2::-127:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")), IPV6_RANGE_127_16.subtract(IPV6_RANGE_127_1_32));
    }

    @Test
    public void shouldParseFullAsnRange() {
        IpResourceRange.parse("AS0-AS4294967295");
    }

}
