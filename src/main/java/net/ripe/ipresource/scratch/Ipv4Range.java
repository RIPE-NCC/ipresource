package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

public final class Ipv4Range extends Ipv4Block {
    final int start;
    final int end;

    Ipv4Range(long start, long end) {
        this.start = (int) start;
        this.end = (int) end;
        if (Integer.toUnsignedLong(this.start) != start) {
            throw new IllegalArgumentException("start out of bounds");
        }
        if (Integer.toUnsignedLong(this.end) != end) {
            throw new IllegalArgumentException("end out of bounds");
        }
        if (Integer.compareUnsigned(this.start, this.end) > 0) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }
        if (Ipv4Block.isLegalPrefix(start, end)) {
            throw new IllegalArgumentException("proper prefix must not be represented by range");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv4Range that && this.start == that.start && this.end == that.end;
    }

    @Override
    public int hashCode() {
        return '4' + 31 * 31 * Integer.hashCode(start) + 31 * Integer.hashCode(end);
    }

    @Override
    public String toString() {
        return start() + "-" + end();
    }

    @Override
    public @NotNull Ipv4Address start() {
        return new Ipv4Address(start);
    }

    @Override
    public @NotNull Ipv4Address end() {
        return new Ipv4Address(end);
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    long lowerBound() {
        return Integer.toUnsignedLong(start);
    }

    @Override
    long upperBound() {
        return Integer.toUnsignedLong(end);
    }
}
