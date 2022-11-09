/**
 * The BSD License
 *
 * Copyright (c) 2010-2022 RIPE NCC
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

import org.junit.Test;

import java.math.BigInteger;

import static net.ripe.ipresource.IpAddress.parse;
import static net.ripe.ipresource.Ipv6AddressTest.ADDRESS_ALL;
import static net.ripe.ipresource.Ipv6AddressTest.COMPRESSED_NOTATION;
import static net.ripe.ipresource.Ipv6AddressTest.COMPRESSED_NOTATION_AT_BEGIN;
import static net.ripe.ipresource.Ipv6AddressTest.COMPRESSED_NOTATION_AT_END;
import static net.ripe.ipresource.Ipv6AddressTest.EXPECTED_COMPRESSED_NOTATION;
import static net.ripe.ipresource.Ipv6AddressTest.EXPECTED_COMPRESSED_NOTATION_AT_END;
import static net.ripe.ipresource.Ipv6AddressTest.EXPECTED_NOTATION_AT_BEGIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IpAddressTest {

    @Test
    public void shouldParseDottedDecimalNotation() {
        assertEquals("127.0.8.23", parse("127.0.8.23").toString());
        assertEquals("193.168.15.255", parse("193.168.15.255").toString());
    }

    @Test
    public void shouldOptionallyDefaultMissingOctets() {
        assertEquals("0", parse("0", true).toString(true));
        assertEquals("127", parse("127", true).toString(true));
        assertEquals("127.3", parse("127.3", true).toString(true));
        assertEquals("127.0.8", parse("127.0.8", true).toString(true));
        assertEquals("127.0.8.12", parse("127.0.8.12", true).toString(true));
    }

    @Test
    public void shouldParseIPv4AddressWithLeadingAndTrailingSpaces() {
        assertEquals("127.0.8.12", parse("  127.0.8.12  ").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsByte() {
        parse("256.0.0.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsByte_NegativeByte() {
        parse("13.-40.0.0");
    }

    @Test
    public void shouldParseFullAddressesCaseInsensitively() {
        assertEquals("2001:0:1234::c1c0:abcd:876", parse("2001:0000:1234:0000:0000:C1C0:ABCD:0876").toString());
        assertEquals("3ffe:b00::1:0:0:a", parse("3ffe:0b00:0000:0000:0001:0000:0000:000a").toString());
        assertEquals("::", parse("0000:0000:0000:0000:0000:0000:0000:0000").toString());
    }

    @Test
    public void shouldParseCompressedAddresses() {
        assertEquals("::1", parse("::1").toString());
        assertEquals("::", parse("::").toString());
        assertEquals("::", parse("0000:0000:0000:0000:0000:0000:0000:0000").toString());
        assertEquals("2::10", parse("2::10").toString());
        assertEquals("fe80::", parse("fe80::").toString());
        assertEquals("fe80::1", parse("fe80::1").toString());
        assertEquals("2001:db8::", parse("2001:db8::").toString());
        assertEquals("1:2:3:4:5:6:0:8", parse("1:2:3:4:5:6::8").toString());
        assertEquals("5f:4688:d998:321a:2fb5:b15:ccc2:0", parse("5f:4688:d998:321a:2fb5:b15:ccc2:0").toString());
        assertEquals("0:1:4d09:ffff:1:ffff:0:ffff", parse("0:1:4d09:ffff:1:ffff:0:ffff").toString());
        assertEquals("0:16b1:ffff:ffff:1c18:4e9b:31b6:0", parse("0:16b1:ffff:ffff:1c18:4e9b:31b6:0").toString());
        assertEquals("::ffff:ffff:1c18:4e9b:31b6:0", parse("0:0:ffff:ffff:1c18:4e9b:31b6:0").toString());
    }

    @Test
    public void shouldCompressLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:beef:0:12", parse("ffce:0:0:0:dead:beef:0:12").toString());
    }

    @Test
    public void shouldCompressLeftmostLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:0:0:0", parse("ffce:0:0:0:dead:0:0:0").toString());
    }

    @Test
    public void shouldNotCompressSingleZero() {
        assertEquals("ffce:0:a:0:dead:0:b:0", parse("ffce:0:a:0:dead:0:b:0").toString());
    }

    @Test
    public void shouldCompressOnLeft() {
        assertEquals("::a:0:dead:0:b:0", parse("0:0:a:0:dead:0:b:0").toString());
    }

    @Test
    public void shouldCompressOnRight() {
        assertEquals("a:0:a:0:dead::", parse("a:0:a:0:dead:0:0:0").toString());
    }

    @Test
    public void shouldCompressOnLeftNotRight() {
        assertEquals("::a:0:dead:a:0:0", parse("0:0:a:0:dead:a:0:0").toString());
    }

    @Test
    public void shouldParseAddressesWithLeadingZerosOmitted() {
        assertEquals("::", parse("0:0:0:0:0:0:0:0").toString());
        assertEquals("::1", parse("0:0:0:0:0:0:0:1").toString());
        assertEquals("2001:db8::8:800:200c:417a", parse("2001:DB8:0:0:8:800:200C:417A").toString());
        assertEquals("2001:db8::8:800:200c:417a", parse("2001:DB8::8:800:200C:417A").toString());
    }

    @Test
    public void shouldParseAddressesWithLeadingOrTrailingSpaces() {
        assertEquals("1:2:3:4:5:6:0:8", parse("   1:2:3:4:5:6::8").toString());
        assertEquals("1:2:3:4:5:6:0:8", parse("1:2:3:4:5:6::8    ").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnEmptyString() {
        parse("");
    }

    @Test
    public void shouldFailOnInternalSpace() {
        try {
            parse("2001:0000:1234: 0000:0000:C1C0:ABCD:0876");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("2001:0000:1234:0000 :0000:C1C0:ABCD:0876");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldFailOnExtraZero() {
        try {
            parse("02001:0000:1234:0000:0000:C1C0:ABCD:0876");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("2001:0000:01234:0000:0000:C1C0:ABCD:0876");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnExtraSegment() {
        parse("1:2:3:4:5:6:7:8:9");
    }

    @Test
    public void shouldFailOnMultipleDoubleColons() {
        try {
            parse("3ffe:b00::1::a");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::1111:2222:3333:4444:5555:6666::");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldFailIfSegmentOutOfBound() {
        try {
            parse("2ffff::10");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("-2::10");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotParseIpv6AddressesWithLessThan7ColonsWithoutDoubleColon() {
        parse("a:b:c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotParseIpv6AddressesWith7ColonsOnly() {
        parse(":::::::");
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotParseNull() {
        parse(null);
    }

    @Test
    public void shouldParseIpv4EmbeddedIpv6Address() {
        assertEquals("1:2:3:4:5:6:102:304", parse("1:2:3:4:5:6:1.2.3.4").toString());
        assertEquals("::102:304", parse("0:0:0:0:0:0:1.2.3.4").toString());
    }

    @Test
    public void shouldParseIpv4EmbeddedIpv6AddressInCompressedFormat() {
        assertEquals("1:2:3:4:5:0:102:304", parse("1:2:3:4:5::1.2.3.4").toString());
        assertEquals("2001:db8:122:344::102:304", parse("2001:db8:122:344::1.2.3.4").toString());
        assertEquals("::122:344:0:102:304", parse("::122:344:0:1.2.3.4").toString());

        assertEquals("1:2:3:4:5:0:102:304", parse("1:2:3:4:5::1.2.3.4").toString());
        assertEquals("1:2:3:4::102:304", parse("1:2:3:4::1.2.3.4").toString());
        assertEquals("1:2:3::102:304", parse("1:2:3::1.2.3.4").toString());
        assertEquals("1:2:3::102:304", parse("1:2:3::1.2.3.4").toString());
        assertEquals("1:2::102:304", parse("1:2::1.2.3.4").toString());
        assertEquals("1::102:304", parse("1::1.2.3.4").toString());
        assertEquals("::102:304", parse("::1.2.3.4").toString());
    }

    @Test
    public void shouldFailIfIpv4PartExceedsBounds() {
        try {
            parse("1::5:400.2.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1::5:260.2.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1::5:256.2.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1::5:1.256.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1::5:1.2.256.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1::5:1.2.256.256");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::300.2.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::1.300.3.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::1.2.300.4");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::1.2.3.300");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldFailIfIpv4PartContainsInvalidCharacters() {
        try {
            parse("2001:1:1:1:1:1:255Z255X255Y255");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("::ffff:192x168.1.26");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldFailIfIpv4PartIsMislocated() {
        try {
            parse("1.2.3.4:1111:2222:3333:4444::5555");
            fail();
        } catch(IllegalArgumentException e) {
        }
        try {
            parse("1.2.3.4::");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldFailIfIpv4PartContainsLeadingZeros() {
        try {
            parse("fe80:0000:0000:0000:0204:61ff:254.157.241.086");
            fail();
        } catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void testExpandAllString() {
        assertEquals(ADDRESS_ALL, parse(ADDRESS_ALL).toString());
    }

    @Test
    public void testExplandToExpandString() {
        assertEquals(EXPECTED_COMPRESSED_NOTATION, parse(COMPRESSED_NOTATION).toString());
        assertEquals(EXPECTED_COMPRESSED_NOTATION_AT_END, parse(COMPRESSED_NOTATION_AT_END).toString());

        assertEquals(new BigInteger("12", 16), parse(COMPRESSED_NOTATION_AT_BEGIN).getValue());
        assertEquals(EXPECTED_NOTATION_AT_BEGIN, parse(COMPRESSED_NOTATION_AT_BEGIN).toString());
    }

}
