package net.ripe.ipresource;

import static net.ripe.ipresource.IpResource.*;
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
        assertTrue(parse("193.0.0.0/8").compareTo(parse("193.0.0.0/12")) < 0);
        assertTrue(parse("193.16.0.0/16").compareTo(parse("193.16.0.0/12")) > 0);
        assertTrue(parse("10.32.0.0/12").compareTo(parse("10.64.0.0/16")) < 0);

        // Bigger ranges are sorted before smaller ranges, just like smaller
        // prefixes (bigger space) are sorted before larger prefixes (smaller
        // space).
        assertTrue(parse("193.0.0.0/8").compareTo(parse("193.0.0.0-195.255.255.255")) > 0);
        assertTrue(parse("193.0.0.0-193.1.255.255").compareTo(parse("193.0.0.0-195.255.255.255")) > 0);
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
    
}
