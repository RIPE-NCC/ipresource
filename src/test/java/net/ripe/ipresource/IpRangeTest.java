package net.ripe.ipresource;

import static org.junit.Assert.*;

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
