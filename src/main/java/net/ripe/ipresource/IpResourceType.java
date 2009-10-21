package net.ripe.ipresource;

import java.math.BigInteger;

public enum IpResourceType {
    ASN("Autonomous System Number", 32) {
        @Override
        public boolean allowPrefixNotation() {
            return false;
        }

        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Asn(value);
        }
    },

    IPv4("IPv4 Address", 32) {
        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Ipv4Address(value);
        }
    },
    IPv6("IPv6 Address", 128) {
        @Override
        public UniqueIpResource fromBigInteger(BigInteger value) {
            return new Ipv6Address(value);
        }
    };

    private final String description;
    private final int bitSize;
    
    private IpResourceType(String description, int bitSize) {
        this.description = description;
        this.bitSize = bitSize;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public int getBitSize() {
        return bitSize;
    }

    public boolean allowPrefixNotation() {
        return true;
    }

    public UniqueIpResource getMinimum() {
        return fromBigInteger(BigInteger.ZERO);
    }
    
    public UniqueIpResource getMaximum() {
        return fromBigInteger(BigInteger.ONE.shiftLeft(getBitSize()).subtract(BigInteger.ONE));
    }
    
    public abstract UniqueIpResource fromBigInteger(BigInteger value);

    /**
     * Necessary for FitNesse.
     */
    public static IpResourceType parse(String s) {
        return IpResourceType.valueOf(s);
    }
}
