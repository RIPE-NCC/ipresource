package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

public final class Ipv4Prefix extends Ipv4Block implements IpPrefix {
    private final int prefix;
    private final byte length;

    Ipv4Prefix(long prefix, int length) {
        this.prefix = (int) prefix;
        this.length = (byte) length;
        if (Integer.toUnsignedLong(this.prefix) != prefix) {
            throw new IllegalArgumentException("IPv4 prefix out of bounds");
        }
        if (Byte.toUnsignedInt(this.length) != length || length > Ipv4Address.NUMBER_OF_BITS) {
            throw new IllegalArgumentException("IPv4 prefix length out of bounds");
        }
        if (lowerBoundForPrefix(prefix, length) != prefix) {
            throw new IllegalArgumentException("not a proper IPv4 prefix");
        }
    }

    public static Ipv4Prefix prefix(long prefix, int length) {
        return new Ipv4Prefix(prefix, length);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv4Prefix that &&
            this.prefix == that.prefix && this.length == that.length;
    }

    @Override
    public int hashCode() {
        return '4' + 31 * 31 * Integer.hashCode(prefix) + 31 * Byte.hashCode(length);
    }

    @Override
    public String toString() {
        if (isSingleton()) {
            return String.valueOf(prefix());
        } else {
            return prefix() + "/" + length();
        }
    }

    @Override
    public int compareTo(@NotNull NumberResourceRange o) {
        return switch (o) {
            case AsnRange ignored ->
                1;
            case Ipv4Prefix that -> {
                int rc = Integer.compareUnsigned(this.prefix, that.prefix);
                if (rc != 0) {
                    yield rc;
                }
                yield Byte.compareUnsigned(this.length, that.length);
            }
            case Ipv4Range that -> {
                int rc = Long.compare(this.lowerBound(), that.lowerBound());
                if (rc != 0) {
                    yield rc;
                }
                yield -Long.compare(this.upperBound(), that.upperBound());
            }
            case Ipv6Prefix ignored -> -1;
            case Ipv6Range ignored -> -1;
        };
    }

    @Override
    public @NotNull Ipv4Address start() {
        return prefix();
    }

    @Override
    public @NotNull Ipv4Address end() {
        return new Ipv4Address(upperBoundForPrefix(Integer.toUnsignedLong(prefix), length()));
    }

    @Override
    public boolean isSingleton() {
        return length() == Ipv4Address.NUMBER_OF_BITS;
    }

    public @NotNull Ipv4Address prefix() {
        return new Ipv4Address(Integer.toUnsignedLong(prefix));
    }

    public int length() {
        return Byte.toUnsignedInt(this.length);
    }

    long lowerBound() {
        return Integer.toUnsignedLong(prefix);
    }

    long upperBound() {
        return upperBoundForPrefix(Integer.toUnsignedLong(prefix), length());
    }

    static long lowerBoundForPrefix(long prefix, int prefixLength) {
        long mask = -(1L << (Ipv4Address.NUMBER_OF_BITS - prefixLength));
        return prefix & mask;
    }

    static long upperBoundForPrefix(long prefix, int prefixLength) {
        long mask = (1L << (Ipv4Address.NUMBER_OF_BITS - prefixLength)) - 1;
        return prefix | mask;
    }
}
