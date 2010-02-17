package net.ripe.ipresource;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

public class Ipv6AddressTest {

	private final static String ADDRESS_ONE = "1:4::a:f:1000:23:1d";
	private final static String ADDRESS_TWO = "12f:2:45:109:ffff:1000:9923:1d";

	private final static String ADDRESS_ALL = "::";
	private final static String COMPRESSED_NOTATION = "12::34";
	private final static String COMPRESSED_NOTATION_AT_END = "12::";
	private final static String COMPRESSED_NOTATION_AT_BEGIN = "::12";

	private final static String CLASSLESS_NOTATION = "1:2:3:4/64";

	@Test
	public void shouldParseColonNotation() {
		assertEquals(ADDRESS_ONE, Ipv6Address.parse(ADDRESS_ONE).toString());
		assertEquals(ADDRESS_TWO, Ipv6Address.parse(ADDRESS_TWO).toString());
	}

	@Test
	public void shouldParseIPv6AddressWithLeadingAndTrailingSpaces() {
	    assertEquals(ADDRESS_ONE, Ipv6Address.parse("  " + ADDRESS_ONE + "  ").toString());
	}

	@Test
	public void testExpandAllString() {
		assertEquals(ADDRESS_ALL, Ipv6Address.parse(ADDRESS_ALL).toString());
	}

	@Test
	public void testExplandToExpandString() {
		assertEquals(COMPRESSED_NOTATION, Ipv6Address.parse(COMPRESSED_NOTATION).toString());
		assertEquals(COMPRESSED_NOTATION_AT_END, Ipv6Address.parse(COMPRESSED_NOTATION_AT_END).toString());

		assertEquals(new BigInteger("12", 16), Ipv6Address.parse(COMPRESSED_NOTATION_AT_BEGIN).getValue());
		assertEquals(COMPRESSED_NOTATION_AT_BEGIN, Ipv6Address.parse(COMPRESSED_NOTATION_AT_BEGIN).toString());
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

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailSinceUniqueAddressIsNotARange() {
    	assertEquals(CLASSLESS_NOTATION, Ipv6Address.parse(CLASSLESS_NOTATION).toString());
    }

    @Test
    public void shouldOnlyCompressFirstSequenceOfZeroes() {
    	assertEquals("ffce::dead:beef:0:12", Ipv6Address.parse("ffce:0:0:0:dead:beef:0:12").toString());
    }

}
