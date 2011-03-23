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

    public static final IpRange RANGE_127_128 = new Ipv4Range(Ipv4Address.parse("127.0.0.0"), Ipv4Address.parse("128.0.0.0"));
    public static final IpRange PREFIX_127_8 = Ipv4Range.prefix(Ipv4Address.parse("127.0.0.0"), 8);

    @Test
    public void shouldSupportPrefixNotation() {
        assertEquals(PREFIX_127_8, IpRange.parse("127.0.0.0/8"));
        assertEquals("127.0.0.0/8", PREFIX_127_8.toString());
        assertEquals(Ipv4Address.parse("127.0.0.0"), PREFIX_127_8.start);
        assertEquals(Ipv4Address.parse("127.255.255.255"), PREFIX_127_8.end);
    }

    @Test
    public void shouldSupportRangeNotation() {
        assertEquals(RANGE_127_128, IpRange.parse("127.0.0.0-128.0.0.0"));
        assertEquals("127.0.0.0-128.0.0.0", RANGE_127_128.toString());
    }

    @Test
    public void shouldSupportOverlap() {
        assertTrue(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("100.0.0.0"), Ipv4Address.parse("200.0.0.0"))));
        assertTrue(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("127.0.0.10"), Ipv4Address.parse("127.0.0.10"))));
        assertTrue(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("127.0.0.10"), Ipv4Address.parse("129.0.0.10"))));
        assertTrue(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("100.0.0.10"), Ipv4Address.parse("127.0.0.10"))));
        assertFalse(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("100.0.0.10"), Ipv4Address.parse("101.0.0.0"))));
        assertFalse(RANGE_127_128.overlaps(new Ipv4Range(Ipv4Address.parse("180.0.0.10"), Ipv4Address.parse("181.0.0.0"))));
    }

    @Test
    public void shouldConvertIPv4RangeToPrefixesIfRangeIsNotALegalPrefix() {
        List<Ipv4Range> prefixes = Ipv4Range.parse("188.247.21.0 - 188.247.28.255").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(Ipv4Range.parse("188.247.21.0/24"), Ipv4Range.parse("188.247.22.0/23"),
                Ipv4Range.parse("188.247.24.0/22"), Ipv4Range.parse("188.247.28.0/24"))));
    }

    @Test
    public void shouldConvertIPv4RangeToPrefixIfRangeIsALegalPrefix() {
        List<Ipv4Range> prefixes = Ipv4Range.parse("188.247.0.0 - 188.247.255.255").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(Ipv4Range.parse("188.247.0.0/16"))));
    }


    @Test
    public void shouldConvertIPv6RangeToPrefixesIfRangeIsNotALegalPrefix() {
        List<Ipv6Range> prefixes = Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:0:0 - 2001:67c:2e8:13:21e:c2ff:7f:0").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff::/106"), Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:40:0/107"),
                Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:60:0/108"), Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:70:0/109"),
                Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:78:0/110"), Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:7c:0/111"),
                Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:7e:0/112"), Ipv6Range.parse("2001:67c:2e8:13:21e:c2ff:7f:0/128"))));
    }

    @Test
    public void shouldConvertIPv6RangeToPrefixIfRangeIsALegalPrefix() {
        List<Ipv6Range> prefixes = Ipv6Range.parse("2001:0:0:0:0:0:0:0 - 2001:ffff:ffff:ffff:ffff:ffff:ffff:ffff").splitToPrefixes();

        assertThat(prefixes, is(Arrays.asList(Ipv6Range.parse("2001::/16"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectAsnRanges() {
        IpRange.parse("AS3333-AS4444");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckPrefixLength_NonNegative() {
        Ipv4Range.prefix(Ipv4Address.parse("127.0.0.0"), -4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckPrefixLength_NotGreatherThanAddressBitSize() {
        Ipv4Range.prefix(Ipv4Address.parse("127.0.0.0"), 34);
    }
}
