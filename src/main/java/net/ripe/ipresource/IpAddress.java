package net.ripe.ipresource;

import java.math.BigInteger;

public abstract class IpAddress extends Resource {

    private static final long serialVersionUID = 2L;

    protected IpAddress(BigInteger value) {
        super(value);
    }

    public static IpAddress parse(String s) {
        return parse(s, false);
    }

    public static IpAddress parse(String s, boolean defaultMissingOctets) {
        try {
            try {
                return Ipv4Address.parse(s, defaultMissingOctets);
            } catch (IllegalArgumentException e) {
                return Ipv6Address.parse(s);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("illegal IP address: %s (%s)", s, e.getMessage()));
        }
    }

    /**
     * Returns the position of the least significant '1' for an IP address; returns {@link IpResourceType#getBitSize()} if there is no
     * '1'; i.e. for 0.0.0.0
     * 
     * @return
     */
    public int getLeastSignificantOne() {
        return getLeastSignificant(true);
    }

    /**
     * Returns the position of the least significant '0' for an IP address; returns {@link IpResourceType#getBitSize()} if there is no
     * '0'; i.e. for 255.255.255.255
     * 
     * @return
     */
    public int getLeastSignificantZero() {
        return getLeastSignificant(false);
    }

    private int getLeastSignificant(boolean bit) {
        for (int i = 0; i < getBitSize(); i++) {
            if (value.testBit(i) == bit) {
                return i;
            }
        }
        return getBitSize();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public abstract String toString(boolean defaultMissingOctets);

    protected abstract int getBitSize();
}
