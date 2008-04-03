package net.ripe.ipresource;

import static org.junit.Assert.*;

import org.junit.Test;

public class Ipv6AddressTest {

	private final static String ADDRESS_ONE = "1:4::a:f:1000:23:1d";
	private final static String ADDRESS_TWO = "12f:2:45:109:ffff:1000:9923:1d";
	private final static String ADDRESS_ALL = "::";
	private final static String ADDRESS_TO_EXPAND = "12::34";
	private final static String ADDRESS_TO_EXPAND_AT_END = "12::";
	
	@Test
	public void shouldParseColonNotation() {
		assertEquals(ADDRESS_ONE, Ipv6Address.parse(ADDRESS_ONE).toString());
		assertEquals(ADDRESS_TWO, Ipv6Address.parse(ADDRESS_TWO).toString());
	}

	@Test
	public void testExpandAllString() {
		assertEquals(ADDRESS_ALL, Ipv6Address.parse(ADDRESS_ALL).toString());
	}

	@Test
	public void testExplandToExpandString() {
		assertEquals(ADDRESS_TO_EXPAND, Ipv6Address.parse(ADDRESS_TO_EXPAND).toString());
		assertEquals(ADDRESS_TO_EXPAND_AT_END, Ipv6Address.parse(ADDRESS_TO_EXPAND_AT_END).toString());
	}

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsByte() {
        Ipv6Address.parse("10000::");
    }

    @Test
    public void shouldNotFailAtEdge() {
        Ipv6Address.parse("FFFF::");        
        Ipv6Address.parse("256::");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsPart_Negative() {
        Ipv6Address.parse("-40::");
        Ipv6Address.parse("::-256");
    }

	
}
