/*
 * The BSD License
 *
 * Copyright (c) 2010-2023 RIPE NCC
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
package net.ripe.ipresource.jdk17;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;

import static java.util.Collections.singletonList;
import static net.ripe.ipresource.jdk17.Ipv6Address.parse;
import static org.junit.jupiter.api.Assertions.*;

class Ipv6AddressTest {
    final static String ADDRESS_ALL = "::";

    final static String COMPRESSED_NOTATION = "12::34";
    final static String EXPECTED_COMPRESSED_NOTATION = "12::34";

    final static String COMPRESSED_NOTATION_AT_END = "12::";
    final static String EXPECTED_COMPRESSED_NOTATION_AT_END = "12::";

    final static String COMPRESSED_NOTATION_AT_BEGIN = "::12";
    final static String EXPECTED_NOTATION_AT_BEGIN = "::12";

    final static String CLASSLESS_NOTATION = "1:2:3:4/64";


    @Test
    void shouldParseFullAddressesCaseInsensitively() {
        assertEquals("2001:0:1234::c1c0:abcd:876", parse("2001:0000:1234:0000:0000:C1C0:ABCD:0876").toString());
        assertEquals("3ffe:b00::1:0:0:a", parse("3ffe:0b00:0000:0000:0001:0000:0000:000a").toString());
        assertEquals("::", parse("0000:0000:0000:0000:0000:0000:0000:0000").toString());
    }

    @Test
    void shouldParseCompressedAddresses() {
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
    void shouldCompressLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:beef:0:12", parse("ffce:0:0:0:dead:beef:0:12").toString());
    }

    @Test
    void shouldCompressLeftmostLongestSequenceOfZeroes() {
        assertEquals("ffce::dead:0:0:0", parse("ffce:0:0:0:dead:0:0:0").toString());
    }

    @Test
    void shouldNotCompressSingleZero() {
        assertEquals("ffce:0:a:0:dead:0:b:0", parse("ffce:0:a:0:dead:0:b:0").toString());
    }

    @Test
    void shouldCompressOnLeft() {
        assertEquals("::a:0:dead:0:b:0", parse("0:0:a:0:dead:0:b:0").toString());
    }

    @Test
    void shouldCompressOnRight() {
        assertEquals("a:0:a:0:dead::", parse("a:0:a:0:dead:0:0:0").toString());
    }

    @Test
    void shouldCompressOnLeftNotRight() {
        assertEquals("::a:0:dead:a:0:0", parse("0:0:a:0:dead:a:0:0").toString());
    }

    @Test
    void shouldParseAddressesWithLeadingZerosOmitted() {
        assertEquals("::", parse("0:0:0:0:0:0:0:0").toString());
        assertEquals("::1", parse("0:0:0:0:0:0:0:1").toString());
        assertEquals("2001:db8::8:800:200c:417a", parse("2001:DB8:0:0:8:800:200C:417A").toString());
        assertEquals("2001:db8::8:800:200c:417a", parse("2001:DB8::8:800:200C:417A").toString());
    }

    @Test
    void shouldParseAddressesWithLeadingOrTrailingSpaces() {
        assertEquals("1:2:3:4:5:6:0:8", parse("   1:2:3:4:5:6::8").toString());
        assertEquals("1:2:3:4:5:6:0:8", parse("1:2:3:4:5:6::8    ").toString());
    }

    @Test
    void shouldFailOnEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> parse(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2001:0000:1234: 0000:0000:C1C0:ABCD:0876",
        "2001:0000:1234:0000 :0000:C1C0:ABCD:0876",
    })
    void shouldFailOnInternalSpace(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "02001:0000:1234:0000:0000:C1C0:ABCD:0876",
        "2001:0000:01234:0000:0000:C1C0:ABCD:0876",
    })
    void shouldFailOnExtraZero(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @Test
    void shouldFailOnExtraSegment() {
        assertThrows(IllegalArgumentException.class, () -> parse("1:2:3:4:5:6:7:8:9"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "3ffe:b00::1::a",
        "::1111:2222:3333:4444:5555:6666::",
    })
    void shouldFailOnMultipleDoubleColons(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2ffff::10",
        "-2::10",
    })
    void shouldFailIfSegmentOutOfBound(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @Test
    void shouldNotParseIpv6AddressesWithLessThan7ColonsWithoutDoubleColon() {
        assertThrows(IllegalArgumentException.class, () -> parse("a:b:c"));
    }

    @Test
    void shouldNotParseIpv6AddressesWith7ColonsOnly() {
        assertThrows(IllegalArgumentException.class, () -> parse(":::::::"));
    }

    @Test
    void shouldNotParseNull() {
        assertThrows(RuntimeException.class, () -> parse(null));
    }

    @Test
    void shouldParseIpv4EmbeddedIpv6Address() {
        assertEquals("1:2:3:4:5:6:102:304", parse("1:2:3:4:5:6:1.2.3.4").toString());
        assertEquals("::102:304", parse("0:0:0:0:0:0:1.2.3.4").toString());
        assertEquals("::ffff:c8c9:cacb", parse("::ffff:200.201.202.203").toString());
    }

    @Test
    void shouldParseIpv4EmbeddedIpv6AddressInCompressedFormat() {
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

    @ParameterizedTest
    @ValueSource(strings = {
        "1::5:400.2.3.4",
        "1::5:260.2.3.4",
        "1::5:256.2.3.4",
        "1::5:1.256.3.4",
        "1::5:1.2.256.4",
        "1::5:1.2.256.256",
        "::300.2.3.4",
        "::1.300.3.4",
        "::1.2.300.4",
        "::1.2.3.300",
    })
    void shouldFailIfIpv4PartExceedsBounds(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2001:1:1:1:1:1:255Z255X255Y255",
        "::ffff:192x168.1.26",
    })
    void shouldFailIfIpv4PartContainsInvalidCharacters(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.2.3.4:1111:2222:3333:4444::5555",
        "1.2.3.4::",
    })
    void shouldFailIfIpv4PartIsMislocated(String address) {
        assertThrows(IllegalArgumentException.class, () -> parse(address));
    }

    @Test
    void shouldFailIfIpv4PartContainsLeadingZeros() {
        assertThrows(IllegalArgumentException.class, () -> parse("fe80:0000:0000:0000:0204:61ff:254.157.241.086"));
    }

    @Test
    void testExpandAllString() {
        assertEquals(ADDRESS_ALL, parse(ADDRESS_ALL).toString());
    }

    @Test
    void testExplandToExpandString() {
        assertEquals(EXPECTED_COMPRESSED_NOTATION, parse(COMPRESSED_NOTATION).toString());
        assertEquals(EXPECTED_COMPRESSED_NOTATION_AT_END, parse(COMPRESSED_NOTATION_AT_END).toString());

        assertEquals(new BigInteger("12", 16), parse(COMPRESSED_NOTATION_AT_BEGIN).getValue());
        assertEquals(EXPECTED_NOTATION_AT_BEGIN, parse(COMPRESSED_NOTATION_AT_BEGIN).toString());
    }

     @Test
    void shouldFailSinceUniqueAddressIsNotARange() {
        assertThrows(IllegalArgumentException.class, () -> parse(CLASSLESS_NOTATION));
    }

    @Test
    void testCompareTo() {
        assertEquals(0, parse("ffce::32").compareTo(parse("ffce::32")));
        assertTrue(parse("ffce::32").compareTo(parse("ffce::33")) < 0);
        assertTrue(parse("ffce::32").compareTo(parse("ffcd::32")) > 0);
    }

    @Test
    void shouldCalculateCommonPrefix() {
        assertEquals(parse("ffce::"), parse("ffce::1").getCommonPrefix(parse("ffce:de::")));
        assertEquals(parse("::"), parse("::1").getCommonPrefix(parse("fd::")));
        assertEquals(parse("23:23:33:112:33:fce:fa:0"), parse("23:23:33:112:33:fce:fa:16").getCommonPrefix(parse("23:23:33:112:33:fce:fa:24")));
    }

    @Test
    void shouldSubtract() {
        assertEquals(singletonList(NumberResourceBlock.parse("8000::/1")), NumberResourceBlock.parse("::/0").subtract(NumberResourceBlock.parse("::/1")));
        assertEquals(singletonList(NumberResourceBlock.parse("0:0:0:1::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")), NumberResourceBlock.parse("::/0").subtract(NumberResourceBlock.parse("::/64")));
    }
}
