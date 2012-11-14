/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
        assertEquals("AS4294967295", String.valueOf(new Asn(Asn.ASN32_MAX_VALUE)));
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

    @Test
    public void testBoundaryConditions() {
        assertEquals(Asn.ASN16_MAX_VALUE, Asn.parse("" + Asn.ASN16_MAX_VALUE).longValue());
        assertEquals(Asn.ASN32_MAX_VALUE, Asn.parse("" + Asn.ASN32_MAX_VALUE).longValue());
        assertTrue(new Asn(Asn.ASN16_MAX_VALUE).compareTo(new Asn(Asn.ASN32_MAX_VALUE)) < 0);
    }
}
