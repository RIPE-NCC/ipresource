package net.ripe.ipresource;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Tests for {@link Asn}.
 */
public class AsnTest {

    public static final Asn ASN3333 = new Asn(BigInteger.valueOf(3333));
    public static final Asn ASN12_3333 = new Asn(BigInteger.valueOf((12 << 16) | 3333));

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

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBadFormat() {
        Asn.parse("AS23.321.12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnIllegalRange() {
        Asn.parse("AS23.321412");
    }

    @Test
    public void shouldParseDotNotatedAsNumber() {
        assertEquals(new Asn(new BigInteger("65536")), Asn.parse("AS1.0"));
    }

    @Test
    public void shouldParseContinuousNumberNotation() {
        assertEquals(new Asn(new BigInteger("65536")), Asn.parse("AS65536"));
    }

    @Test
    public void parseShouldBeCaseInsensitive() {
        assertEquals(Asn.parse("AS3333"), Asn.parse("as3333"));
    }

    @Test
    public void shouldParseHighestPossibleAsn() {
        Asn.parse("AS4294967295");
    }
}

