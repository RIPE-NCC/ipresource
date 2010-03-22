package net.ripe.ipresource;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link Asn}.
 */
public class AsnTest {

    public static final Asn ASN3333 = new Asn(3333);
    public static final Asn ASN12_3333 = new Asn((12 << 16) | 3333);

    /**
     * Tests that the textual representation of an ASN conforms to
     * http://tools.ietf.org/html/draft-michaelson-4byte-as-representation-05.
     */
    @Test
    public void shouldHaveConformingTextualRepresentation() {
        assertEquals(null, Asn.parse(null));

        assertEquals(ASN3333, Asn.parse("AS3333"));
        assertEquals(ASN12_3333, Asn.parse("AS12.3333"));

        assertEquals("AS3333", String.valueOf(ASN3333));
        assertEquals("AS789765", String.valueOf(ASN12_3333));
    }

    @Test
    public void shouldParseShortVersion() {
        assertEquals(ASN3333, Asn.parse("3333"));
        assertEquals(ASN12_3333, Asn.parse("12.3333"));
        assertEquals(new Asn(65536), Asn.parse("  65536  "));
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBadFormat() {
        Asn.parse("AS23.321.12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnIllegalRange() {
        Asn.parse("AS23.321412");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnIllegalValue() {
        Asn.parse("AS232442321412");
    }

    @Test
    public void shouldParseDotNotatedAsNumber() {
        assertEquals(new Asn(65536), Asn.parse("AS1.0"));
    }

    @Test
    public void shouldParseContinuousNumberNotation() {
        assertEquals(new Asn(65536), Asn.parse("AS65536"));
    }

    @Test
    public void parseShouldBeCaseInsensitive() {
        assertEquals(Asn.parse("AS3333"), Asn.parse("as3333"));
    }

    @Test
    public void shouldParseNumberWithLeadingAndTrailingSpaces() {
        assertEquals(new Asn(65536), Asn.parse("  AS65536  "));
    }

    @Test
    public void shouldParseHighestPossibleAsn() {
        Asn.parse("AS4294967295");
    }

    @Test
    public void testCompareTo() {
        assertTrue(Asn.parse("AS3333").compareTo(Asn.parse("AS3333")) == 0);
        assertTrue(Asn.parse("AS3333").compareTo(Asn.parse("AS3334")) < 0);
        assertTrue(Asn.parse("AS3333").compareTo(Asn.parse("AS3332")) > 0);
        assertTrue(Asn.parse("AS3333").compareTo(Asn.parse("AS3333").upTo(Asn.parse("AS3333"))) == 0);
        assertTrue(Asn.parse("AS3333").upTo(Asn.parse("AS3333")).compareTo(Asn.parse("AS3333")) == 0);
    }
    
}

