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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link IpRange}.
 */
public class IpRangeTest {

    public static final IpRange RANGE_127_128 = IpRange.range(IpAddress.parse("127.0.0.0"), IpAddress.parse("128.0.0.0"));
    public static final IpRange PREFIX_127_8 = IpRange.prefix(IpAddress.parse("127.0.0.0"), 8);

    @Test
    public void shouldSupportPrefixNotation() {
        assertEquals(PREFIX_127_8, IpRange.parse("127.0.0.0/8"));
        assertEquals("127.0.0.0/8", PREFIX_127_8.toString());
        assertEquals(Ipv4Address.parse("127.0.0.0"), PREFIX_127_8.getStart());
        assertEquals(Ipv4Address.parse("127.255.255.255"), PREFIX_127_8.getEnd());
    }

    @Test
    public void shouldSupportRangeNotation() {
        assertEquals(RANGE_127_128, IpRange.parse("127.0.0.0-128.0.0.0"));
        assertEquals("127.0.0.0-128.0.0.0", RANGE_127_128.toString());
    }

    @Test
    public void shouldSupportOverlap() {
        assertTrue(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("100.0.0.0"), IpAddress.parse("200.0.0.0"))));
        assertTrue(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("127.0.0.10"), IpAddress.parse("127.0.0.10"))));
        assertTrue(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("127.0.0.10"), IpAddress.parse("129.0.0.10"))));
        assertTrue(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("100.0.0.10"), IpAddress.parse("127.0.0.10"))));
        assertFalse(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("100.0.0.10"), IpAddress.parse("101.0.0.0"))));
        assertFalse(RANGE_127_128.overlaps(IpRange.range(IpAddress.parse("180.0.0.10"), IpAddress.parse("181.0.0.0"))));
    }

    @Test
    public void shouldConvertIPv4RangeToPrefixesIfRangeIsNotALegalPrefix() {
        List<IpRange> prefixes = IpRange.parse("188.247.21.0 - 188.247.28.255").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(IpRange.parse("188.247.21.0/24"), IpRange.parse("188.247.22.0/23"),
                IpRange.parse("188.247.24.0/22"), IpRange.parse("188.247.28.0/24"))));
    }

    @Test
    public void shouldConvertIPv4RangeToPrefixIfRangeIsALegalPrefix() {
        List<IpRange> prefixes = IpRange.parse("188.247.0.0 - 188.247.255.255").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(IpRange.parse("188.247.0.0/16"))));
    }


    @Test
    public void shouldConvertIPv6RangeToPrefixesIfRangeIsNotALegalPrefix() {
        List<IpRange> prefixes = IpRange.parse("2001:67c:2e8:13:21e:c2ff:0:0 - 2001:67c:2e8:13:21e:c2ff:7f:0").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(IpRange.parse("2001:67c:2e8:13:21e:c2ff::/106"), IpRange.parse("2001:67c:2e8:13:21e:c2ff:40:0/107"),
                IpRange.parse("2001:67c:2e8:13:21e:c2ff:60:0/108"), IpRange.parse("2001:67c:2e8:13:21e:c2ff:70:0/109"),
                IpRange.parse("2001:67c:2e8:13:21e:c2ff:78:0/110"), IpRange.parse("2001:67c:2e8:13:21e:c2ff:7c:0/111"),
                IpRange.parse("2001:67c:2e8:13:21e:c2ff:7e:0/112"), IpRange.parse("2001:67c:2e8:13:21e:c2ff:7f:0/128"))));
    }

    @Test
    public void shouldConvertIPv6RangeToPrefixIfRangeIsALegalPrefix() {
        List<IpRange> prefixes = IpRange.parse("2001:0:0:0:0:0:0:0 - 2001:ffff:ffff:ffff:ffff:ffff:ffff:ffff").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(IpRange.parse("2001::/16"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectAsnRanges() {
        IpRange.parse("AS3333-AS4444");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckPrefixLength_NonNegative() {
        IpRange.prefix(IpAddress.parse("127.0.0.0"), -4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckPrefixLength_NotGreatherThanAddressBitSize() {
        IpRange.prefix(IpAddress.parse("127.0.0.0"), 34);
    }
}
