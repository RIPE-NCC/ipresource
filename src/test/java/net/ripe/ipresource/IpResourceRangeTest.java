package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class IpResourceRangeTest {

    private static final IpResourceRange RANGE_127_0_16 = IpResourceRange.parse("127.0.0.0/16");
    private static final IpResource RANGE_127_1_16 = IpResourceRange.parse("127.1.0.0/16");
    private static final IpResource RANGE_127_8 = IpResourceRange.parse("127.0.0.0/8");

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
    public void subtractRange() {
        assertEquals(Collections.singletonList(RANGE_127_0_16), RANGE_127_0_16.subtract(RANGE_127_1_16));
        assertEquals(Collections.emptyList(), RANGE_127_0_16.subtract(RANGE_127_8));
        assertEquals(Collections.singletonList(IpResourceRange.parse("127.1.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_0_16));
        assertEquals(Arrays.asList(RANGE_127_0_16, IpResourceRange.parse("127.2.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_1_16));
    }

    @Test
    public void singletonRangeShouldEqualUniqueIpResource() {
        UniqueIpResource unique = UniqueIpResource.parse("127.0.0.1");
        IpResourceRange range = IpResourceRange.range(unique, unique);
        assertEquals(range, unique);
        assertEquals(unique, range);
        assertEquals(range.hashCode(), unique.hashCode());
        assertEquals(unique.hashCode(), range.hashCode());
        assertEquals(0, unique.compareTo(range));
        assertEquals(0, range.compareTo(unique));
    }
}
