package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class IpResourceRangeTest {

    private static final IpResourceRange RANGE_127_0_16 = IpResourceRange.parse("127.0.0.0/16");
    private static final IpResource RANGE_127_1_16 = IpResourceRange.parse("127.1.0.0/16");
    private static final IpResource RANGE_127_8 = IpResourceRange.parse("127.0.0.0/8");

    private static final IpResourceRange IPV6_RANGE_127_0_32 = IpResourceRange.parse("127:0::/32");
    private static final IpResourceRange IPV6_RANGE_127_1_32 = IpResourceRange.parse("127:1::/32");
    private static final IpResourceRange IPV6_RANGE_127_16 = IpResourceRange.parse("127::/16");

    
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

}
