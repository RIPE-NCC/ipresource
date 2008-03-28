package net.ripe.ipresource;

import java.math.BigInteger;

public abstract class IpAddress extends UniqueIpResource {

    protected IpAddress(IpResourceType type, BigInteger value) {
        super(type, value);
    }

    public static IpAddress parse(String s) {
        return Ipv4Address.parse(s);
    }

}
