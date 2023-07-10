/**
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
package net.ripe.ipresource;

import static net.ripe.ipresource.UniqueIpResource.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import org.junit.Test;


public class UniqueIpResourceTest {

    @Test
    public void should_parse_any_kind_of_resource() {
        assertEquals(new Asn(1), parse("AS1"));
        assertEquals(new Ipv4Address(1), parse("0.0.0.1"));
        assertEquals(new Ipv6Address(BigInteger.ONE), parse("::1"));
    }

    @Test
    public void should_fail_when_string_is_not_parseable() {
        try {
            UniqueIpResource.parse("foo");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Invalid IPv4, IPv6 or ASN resource: foo", expected.getMessage());
        }
    }

    @Test
    public void should_check_for_resource_adjacency() {
        assertTrue(parse("AS1").adjacent(parse("AS2")));
        assertTrue(parse("AS2").adjacent(parse("AS1")));
        assertTrue(parse("10.0.0.0").adjacent(parse("10.0.0.1")));
        assertTrue(parse("10.0.0.1").adjacent(parse("10.0.0.0")));
        assertTrue(parse("::1").adjacent(parse("::2")));
        assertTrue(parse("::2").adjacent(parse("::1")));

        assertFalse(parse("AS1").adjacent(parse("AS3")));
        assertFalse(parse("AS3").adjacent(parse("AS1")));
        assertFalse(parse("10.0.0.0").adjacent(parse("10.0.0.2")));
        assertFalse(parse("10.0.0.2").adjacent(parse("10.0.0.0")));
        assertFalse(parse("::1").adjacent(parse("::3")));
        assertFalse(parse("::3").adjacent(parse("::1")));

        // Different types are never adjacent.
        assertFalse(new Asn(Asn.ASN32_MAX_VALUE).adjacent(parse("0.0.0.0")));
        assertFalse(parse("255.255.255.255").adjacent(parse("::0")));
    }
}
