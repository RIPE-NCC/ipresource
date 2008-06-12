package net.ripe.ipresource;

import static net.ripe.ipresource.IpResource.parse;
import static org.junit.Assert.*;
import org.junit.Test;

public class IpResourceSetTest {

    @Test
    public void shouldNormalizeAccordingToRfc3779() {
        IpResourceSet resources = new IpResourceSet();
        resources.add(parse("127.0.0.1"));
        resources.add(parse("10.0.0.0/8"));
        resources.add(parse("255.255.255.255-255.255.255.255"));
        resources.add(parse("193.0.0.0/8"));
        resources.add(parse("194.0.0.0/8"));
        resources.add(parse("194.16.0.0/16"));
        resources.add(parse("195.0.0.0/8"));
        resources.add(parse("195.1.0.0-196.255.255.255"));
        assertEquals("10/8, 127.0.0.1, 193.0.0.0-196.255.255.255, 255.255.255.255", resources.toString());
    }

    @Test
    public void shouldNormalizeSingletonRangeToUniqueIpResource() {
        IpResourceSet resources = new IpResourceSet(parse("127.0.0.1-127.0.0.1"));
        assertEquals("127.0.0.1", resources.toString());
    }

    @Test
    public void parseShouldIgnoreWhitespace() {
        assertEquals(IpResourceSet.parse("127.0.0.1,AS3333"), IpResourceSet.parse("\t   \n127.0.0.1,   AS3333"));
    }

    @Test
    public void testContains() {
        IpResourceSet a = IpResourceSet.parse("10.0.0.0/8,192.168.0.0/16");
        assertTrue(a.contains(a));
        assertTrue(a.contains(new IpResourceSet()));
        assertTrue(new IpResourceSet().contains(new IpResourceSet()));
        assertFalse(new IpResourceSet().contains(IpResourceSet.parse("10.0.0.0/24")));

        assertTrue(a.contains(IpResourceSet.parse("10.0.0.0/24")));
        assertTrue(a.contains(IpResourceSet.parse("192.168.1.131")));
        assertFalse(a.contains(IpResourceSet.parse("127.0.0.1")));
        assertFalse(a.contains(IpResourceSet.parse("192.168.0.0-192.172.255.255")));
        assertFalse(a.contains(IpResourceSet.parse("10.0.0.1,192.168.0.0-192.172.255.255")));
    }

    @Test
    public void testRemove() {
        IpResourceSet a = IpResourceSet.parse("AS3333-AS4444,10.0.0.0/8");
        assertTrue(a.remove(IpResource.parse("10.5.0.0/16")));
        assertFalse(a.remove(IpResource.parse("10.5.0.0/16")));

        assertTrue(a.contains(IpResource.parse("AS3333-AS4444")));
        assertEquals("AS3333-AS4444, 10.0.0.0-10.4.255.255, 10.6.0.0-10.255.255.255", a.toString());
    }
}
