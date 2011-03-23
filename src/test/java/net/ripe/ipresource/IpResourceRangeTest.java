package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class IpResourceRangeTest {

    private static final Ipv4Range RANGE_127_0_16 = Ipv4Range.parse("127.0.0.0/16");
    private static final Ipv4Range RANGE_127_1_16 = Ipv4Range.parse("127.1.0.0/16");
    private static final Ipv4Range RANGE_127_8 = Ipv4Range.parse("127.0.0.0/8");

    private static final Ipv6Range IPV6_RANGE_127_0_32 = Ipv6Range.parse("127:0::/32");
    private static final Ipv6Range IPV6_RANGE_127_1_32 = Ipv6Range.parse("127:1::/32");
    private static final Ipv6Range IPV6_RANGE_127_16 = Ipv6Range.parse("127::/16");

    private static final String IPV6_RANGE_STR = "2001:db8:8000::/33";
    private static final ResourceRange IPV6_RANGE = ResourceRange.parse(IPV6_RANGE_STR);

    @Test
    public void shouldParseIpv6Range() {
    	assertEquals(IPV6_RANGE_STR, IPV6_RANGE.toString());
    }


    @Test
    public void shouldSupportAsnRange() {
        assertEquals(new AsnRange(Asn.parse("AS3333"), Asn.parse("AS4444")), ResourceRange.parse("AS3333-AS4444"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnEmptyRange() {
        ResourceRange.parse("AS3333-AS2222");
    }

    @Test
    public void shouldParsePrefix() {
        assertEquals(Ipv4Address.parse("10.15.0.0"), ResourceRange.parse("10.15.0.0/16").start);
        assertEquals(Ipv4Address.parse("10.15.255.255"), ResourceRange.parse("10.15.0.0/16").end);
    }

    @Test
    public void shouldParseRipePrefixNotation() {
        assertEquals("0.0.0.0/0", ResourceRange.parse("0/0").toString());
    }

    @Test
    public void shouldParseCommaPrefixNotation() {
        assertEquals("10.0.0.0-10.1.1.129", ResourceRange.parse("10.0.0.0/16,/24,/25,/31").toString());
    }

    @Test
    public void shouldParseNetmaskPrefixNotation() {
        assertEquals("193.0.0.0/19", Ipv4Range.parseWithNetmask("193.0.0.0", "255.255.224.0").toString());

        try {
            Ipv4Range.parseWithNetmask("193.0.0.0", "193.0.0.19");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void subtractRange() {
        assertEquals(Collections.singletonList(RANGE_127_0_16), RANGE_127_0_16.subtract(RANGE_127_1_16));
        assertEquals(Collections.emptyList(), RANGE_127_0_16.subtract(RANGE_127_8));
        assertEquals(Collections.singletonList(ResourceRange.parse("127.1.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_0_16));
        assertEquals(Arrays.asList(RANGE_127_0_16, ResourceRange.parse("127.2.0.0-127.255.255.255")), RANGE_127_8.subtract(RANGE_127_1_16));
    }

    @Test
    public void shouldParseIPv6ClasslessNotation() {
    	ResourceRange.parse("1:2:3::/64");
    }

    @Test
    public void shouldParseIpv6Prefix() {
        assertEquals(IpAddress.parse("10::"), ResourceRange.parse("10::/16").start);
        assertEquals(IpAddress.parse("10:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"), ResourceRange.parse("10::/16").end);
    }

    @Test
    public void subtractIPv6Range() {
        assertEquals(Collections.singletonList(IPV6_RANGE_127_0_32), IPV6_RANGE_127_0_32.subtract(IPV6_RANGE_127_1_32));
        assertEquals(Collections.emptyList(), IPV6_RANGE_127_0_32.subtract(IPV6_RANGE_127_16));
        assertEquals(Collections.singletonList(ResourceRange.parse("127:1::-127:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")), IPV6_RANGE_127_16.subtract(IPV6_RANGE_127_0_32));
        assertEquals(Arrays.asList(IPV6_RANGE_127_0_32, ResourceRange.parse("127:2::-127:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")), IPV6_RANGE_127_16.subtract(IPV6_RANGE_127_1_32));
    }

    @Test
    public void shouldParseFullAsnRange() {
        ResourceRange.parse("AS0-AS4294967295");
    }

}
