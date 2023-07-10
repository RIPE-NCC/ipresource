package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

public final class Ipv6Range extends Ipv6Block {
    final Ipv6Address start;
    final Ipv6Address end;

    Ipv6Range(Ipv6Address start, Ipv6Address end) {
        this.start = start;
        this.end = end;
        if (this.start.compareTo(this.end) > 0) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }
        if (isLegalPrefix(start, end)) {
            throw new IllegalArgumentException("proper prefix must not be represented by range");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ipv6Range that && this.start.equals(that.start) && this.end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return '6' + 31 * 31 * start.hashCode() + 31 * end.hashCode();
    }

    @Override
    public String toString() {
        return start() + "-" + end();
    }

    @Override
    public @NotNull Ipv6Address start() {
        return start;
    }

    @Override
    public @NotNull Ipv6Address end() {
        return end;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
