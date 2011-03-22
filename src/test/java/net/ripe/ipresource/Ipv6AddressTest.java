package net.ripe.ipresource;

import static net.ripe.ipresource.Ipv6Address.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

public class Ipv6AddressTest {

	private final static String ADDRESS_ONE = "1:4::a:f:1000:23:1d";
	private final static String EXPECTED_ADDRESS_ONE = "1:4:0:a:f:1000:23:1d";

	private final static String ADDRESS_TWO = "12f:0000:45:109:ffff:1000:9923:1d";
	private final static String EXPECTED_ADDRESS_TWO = "12f:0:45:109:ffff:1000:9923:1d";

	private final static String ADDRESS_ALL = "::";

	private final static String COMPRESSED_NOTATION = "12::34";
	private final static String EXPECTED_COMPRESSED_NOTATION = "12::34";

	private final static String COMPRESSED_NOTATION_AT_END = "12::";
	private final static String EXPECTED_COMPRESSED_NOTATION_AT_END = "12::";

	private final static String COMPRESSED_NOTATION_AT_BEGIN = "::12";
	private final static String EXPECTED_NOTATION_AT_BEGIN = "::12";

	private final static String CLASSLESS_NOTATION = "1:2:3:4/64";

	@Test
	public void shouldParseColonNotation() {
		assertEquals(EXPECTED_ADDRESS_ONE, Ipv6Address.parse(ADDRESS_ONE).toString());
		assertEquals(EXPECTED_ADDRESS_TWO, Ipv6Address.parse(ADDRESS_TWO).toString());
	}

	@Test
	public void shouldParseIPv6AddressWithLeadingAndTrailingSpaces() {
	    assertEquals(EXPECTED_ADDRESS_ONE, Ipv6Address.parse("  " + ADDRESS_ONE + "  ").toString());
	}

	@Test
	public void testExpandAllString() {
		assertEquals(ADDRESS_ALL, Ipv6Address.parse(ADDRESS_ALL).toString());
	}

	@Test
	public void testExplandToExpandString() {
		assertEquals(EXPECTED_COMPRESSED_NOTATION, Ipv6Address.parse(COMPRESSED_NOTATION).toString());
		assertEquals(EXPECTED_COMPRESSED_NOTATION_AT_END, Ipv6Address.parse(COMPRESSED_NOTATION_AT_END).toString());

		assertEquals(new BigInteger("12", 16), Ipv6Address.parse(COMPRESSED_NOTATION_AT_BEGIN).getValue());
		assertEquals(EXPECTED_NOTATION_AT_BEGIN, Ipv6Address.parse(COMPRESSED_NOTATION_AT_BEGIN).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailOnMissingGroups() {
	    Ipv6Address.parse(":1.2.3.4");
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
    public void shouldCompressLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:beef:0:12", Ipv6Address.parse("ffce:0:0:0:dead:beef:0:12").toString());
    }

    @Test
    public void shouldCompressLeftmostLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:0:0:0", Ipv6Address.parse("ffce:0:0:0:dead:0:0:0").toString());
    }

    @Test
    public void shouldNotCompressSingleZero() {
        assertEquals("ffce:0:a:0:dead:0:b:0", Ipv6Address.parse("ffce:0:a:0:dead:0:b:0").toString());
    }

    @Test
    public void shouldCompressOnLeft() {
        assertEquals("::a:0:dead:0:b:0", Ipv6Address.parse("0:0:a:0:dead:0:b:0").toString());
    }

    @Test
    public void shouldCompressOnRight() {
        assertEquals("a:0:a:0:dead::", Ipv6Address.parse("a:0:a:0:dead:0:0:0").toString());
    }

    @Test
    public void shouldCompressOnLeftNotRight() {
        assertEquals("::a:0:dead:a:0:0", Ipv6Address.parse("0:0:a:0:dead:a:0:0").toString());
    }

    @Test
    public void testCompareTo() {
        assertTrue(parse("ffce::32").compareTo(parse("ffce::32")) == 0);
        assertTrue(parse("ffce::32").compareTo(parse("ffce::33")) < 0);
        assertTrue(parse("ffce::32").compareTo(parse("ffcd::32")) > 0);
        assertTrue(parse("ffce::32").compareTo(parse("ffce::32").upTo(parse("ffce::32"))) == 0);
        assertTrue(parse("ffce::32").upTo(parse("ffce::32")).compareTo(parse("ffce::32")) == 0);
    }

    @Test
    public void shouldCalculateCommonPrefix() {
        assertEquals(parse("ffce::"), parse("ffce::1").getCommonPrefix(parse("ffce:de::")));
        assertEquals(parse("::"), parse("::1").getCommonPrefix(parse("fd::")));
        assertEquals(parse("23:23:33:112:33:fce:fa:0"), parse("23:23:33:112:33:fce:fa:16").getCommonPrefix(parse("23:23:33:112:33:fce:fa:24")));
    }

    @Test
    public void shouldCalculatePrefixRange() {
        assertEquals(parse("ffce:abc0::"), parse("ffce:abcd::").lowerBoundForPrefix(28));
        assertEquals(parse("ffce:abcf:ffff:ffff:ffff:ffff:ffff:ffff"), parse("ffce:abcd::").upperBoundForPrefix(28));
    }

    @Test
    public void testIsValidNetmask() {
        assertTrue(parse("ffff::").isValidNetmask());
        assertTrue(parse("8000::").isValidNetmask());
        assertTrue(parse("c000::").isValidNetmask());
        assertTrue(parse("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff80").isValidNetmask());
        assertTrue(parse("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").isValidNetmask());
        assertFalse(parse("ffff::ffff").isValidNetmask());
        assertFalse(parse("::").isValidNetmask());
    }

}
