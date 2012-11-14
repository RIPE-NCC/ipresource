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

import static net.ripe.ipresource.Ipv4Address.*;

import static org.junit.Assert.*;

import org.junit.Test;

public class Ipv4AddressTest {

    @Test
    public void shouldParseDottedDecimalNotation() {
        assertEquals("127.0.8.23", Ipv4Address.parse("127.0.8.23").toString());
        assertEquals("193.168.15.255", Ipv4Address.parse("193.168.15.255").toString());
    }

    @Test
    public void shouldOptionallyDefaultMissingOctets() {
        assertEquals("0", Ipv4Address.parse("0", true).toString(true));
        assertEquals("127", Ipv4Address.parse("127", true).toString(true));
        assertEquals("127.3", Ipv4Address.parse("127.3", true).toString(true));
        assertEquals("127.0.8", Ipv4Address.parse("127.0.8", true).toString(true));
        assertEquals("127.0.8.12", Ipv4Address.parse("127.0.8.12", true).toString(true));
    }

    @Test
    public void shouldParseIPv4AddressWithLeadingAndTrailingSpaces() {
        assertEquals("127.0.8.12", Ipv4Address.parse("  127.0.8.12  ").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsByte() {
        Ipv4Address.parse("256.0.0.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnOutOfBoundsByte_NegativeByte() {
        Ipv4Address.parse("13.-40.0.0");
    }

    @Test
    public void shouldCalculateCommonPrefix() {
        assertEquals(Ipv4Address.parse("127.0.0.0"), Ipv4Address.parse("127.0.0.1").getCommonPrefix(Ipv4Address.parse("127.15.0.0")));
        assertEquals(Ipv4Address.parse("0.0.0.0"), Ipv4Address.parse("127.0.0.1").getCommonPrefix(Ipv4Address.parse("128.0.0.0")));
        assertEquals(Ipv4Address.parse("193.17.48.16"), Ipv4Address.parse("193.17.48.18").getCommonPrefix(Ipv4Address.parse("193.17.48.24")));
    }

    @Test
    public void shouldCalculatePrefixRange() {
        assertEquals(Ipv4Address.parse("193.17.48.32"), Ipv4Address.parse("193.17.48.38").lowerBoundForPrefix(28));
        assertEquals(Ipv4Address.parse("193.17.48.47"), Ipv4Address.parse("193.17.48.38").upperBoundForPrefix(28));
    }

    @Test
    public void shouldCompare() {
        assertTrue(parse("193.17.48.32").compareTo(parse("193.17.48.33")) < 0);
        assertTrue(IpRange.parse("193.0.0.0/8").compareTo(IpRange.parse("193.0.0.0/12")) < 0);
        assertTrue(IpRange.parse("193.16.0.0/16").compareTo(IpRange.parse("193.16.0.0/12")) > 0);
        assertTrue(IpRange.parse("10.32.0.0/12").compareTo(IpRange.parse("10.64.0.0/16")) < 0);

        // Bigger ranges are sorted before smaller ranges, just like smaller
        // prefixes (bigger space) are sorted before larger prefixes (smaller
        // space).
        assertTrue(IpRange.parse("193.0.0.0/8").compareTo(IpRange.parse("193.0.0.0-195.255.255.255")) > 0);
        assertTrue(IpRange.parse("193.0.0.0-193.1.255.255").compareTo(IpRange.parse("193.0.0.0-195.255.255.255")) > 0);
    }

    @Test
    public void shouldKnowPredecessor() {
        assertEquals(Ipv4Address.parse("10.14.255.255"), Ipv4Address.parse("10.15.0.0").predecessor());
    }

    @Test
    public void shouldKnowSuccessor() {
        assertEquals(Ipv4Address.parse("10.15.0.1"), Ipv4Address.parse("10.15.0.0").successor());
    }

    @Test
    public void testGetLeastSignificantZero() {
        assertEquals(4, Ipv4Address.parse("10.0.0.15").getLeastSignificantZero());
        assertEquals(32, Ipv4Address.parse("255.255.255.255").getLeastSignificantZero());
        assertEquals(24, Ipv4Address.parse("0.255.255.255").getLeastSignificantZero());
    }

    @Test
    public void testGetLeastSignificantOne() {
        assertEquals(4, Ipv4Address.parse("10.0.0.16").getLeastSignificantOne());
        assertEquals(32, Ipv4Address.parse("0.0.0.0").getLeastSignificantOne());
        assertEquals(24, Ipv4Address.parse("255.0.0.0").getLeastSignificantOne());
    }

    @Test
    public void testStripLeastSignificantOnes() {
        assertEquals(Ipv4Address.parse("10.0.0.16"), Ipv4Address.parse("10.0.0.16").stripLeastSignificantOnes());
        assertEquals(Ipv4Address.parse("10.0.0.0"), Ipv4Address.parse("10.0.0.15").stripLeastSignificantOnes());
        assertEquals(Ipv4Address.parse("0.0.0.0"), Ipv4Address.parse("0.255.255.255").stripLeastSignificantOnes());
        assertEquals(Ipv4Address.parse("0.0.0.0"), Ipv4Address.parse("255.255.255.255").stripLeastSignificantOnes());
        assertEquals(Ipv4Address.parse("255.255.254.0"), Ipv4Address.parse("255.255.254.255").stripLeastSignificantOnes());
    }

    @Test
    public void testIsValidNetmask() {
        assertTrue(parse("255.0.0.0").isValidNetmask());
        assertTrue(parse("128.0.0.0").isValidNetmask());
        assertTrue(parse("192.0.0.0").isValidNetmask());
        assertTrue(parse("255.255.255.192").isValidNetmask());
        assertTrue(parse("255.255.255.255").isValidNetmask());
        assertFalse(parse("255.0.0.255").isValidNetmask());
        assertFalse(parse("0.0.0.0").isValidNetmask());
        
        // Ensure singleton ranges behave the same way as an regular address.
        assertTrue(IpRange.parse("255.0.0.0/32").isValidNetmask());
        assertFalse(IpRange.parse("255.0.0.0-255.255.0.0").isValidNetmask());
    }
    
}
