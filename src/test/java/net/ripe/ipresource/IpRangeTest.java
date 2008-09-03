package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;
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
