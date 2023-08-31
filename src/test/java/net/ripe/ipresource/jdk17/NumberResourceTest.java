package net.ripe.ipresource.jdk17;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberResourceTest {

    @Test
    void should_parse_string() {
        assertEquals(Asn.of(123), NumberResource.parse("AS123"));
        assertEquals(Ipv4Address.of(123), NumberResource.parse("0.0.0.123"));
        assertEquals(Ipv6Address.of(BigInteger.valueOf(123)), NumberResource.parse("::7b"));
    }
}