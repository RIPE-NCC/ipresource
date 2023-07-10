package net.ripe.ipresource.scratch;

import net.ripe.ipresource.IpResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.ripe.ipresource.scratch.Ipv6Prefix.lowerBoundForPrefix;
import static net.ripe.ipresource.scratch.Ipv6Prefix.upperBoundForPrefix;

public sealed abstract class Ipv6Block implements IpBlock permits Ipv6Prefix, Ipv6Range {
    public abstract @NotNull Ipv6Address start();
    public abstract @NotNull Ipv6Address end();

    public static @NotNull Ipv6Block of(@NotNull Ipv6Address start, @NotNull Ipv6Address end) {
        Ipv6Prefix prefix = prefixOrNull(start, end);
        return prefix != null ? prefix : new Ipv6Range(start, end);
    }

    @Override
    public int compareTo(@NotNull NumberResourceRange o) {
        return switch (o) {
            case AsnRange ignored -> 1;
            case Ipv4Prefix ignored -> 1;
            case Ipv4Range ignored -> 1;
            case Ipv6Prefix that -> {
                int rc = start().compareTo(that.start());
                if (rc != 0) {
                    yield rc;
                }
                yield -end().compareTo(that.end());
            }
            case Ipv6Range that -> {
                int rc = start().compareTo(that.start());
                if (rc != 0) {
                    yield rc;
                }
                yield -end().compareTo(that.end());
            }
        };
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv4;
    }

    @Override
    public boolean contains(@Nullable NumberResourceRange other) {
        return switch (other) {
            case null -> false;
            case AsnRange ignored -> false;
            case Ipv4Prefix ignored -> false;
            case Ipv4Range ignored -> false;
            case Ipv6Prefix that ->
                start().compareTo(that.start()) <= 0 && end().compareTo(that.end()) >= 0;
            case Ipv6Block that -> start().compareTo(that.start()) <= 0 && end().compareTo(that.end()) >= 0;
        };
    }

    @Override
    public @NotNull List<@NotNull NumberResourceRange> subtract(@Nullable NumberResourceRange other) {
        if (other == null || other instanceof AsnRange) {
            return Collections.emptyList();
        } else if (other instanceof Ipv6Block that) {
            if (other.contains(this)) {
                return Collections.emptyList();
            } else if (overlaps(this, that)) {
                var result = new ArrayList<@NotNull NumberResourceRange>(2);
                if (start().compareTo(that.start()) < 0) {
                    result.add(Ipv6Block.of(start(), that.start().predecessorOrFirst()));
                }
                if (end().compareTo(that.end()) > 0) {
                    result.add(Ipv6Block.of(that.end().successorOrLast(), end()));
                }
                return result;
            } else {
                return Collections.singletonList(this);
            }
        } else {
            throw new IllegalArgumentException("unknown type");
        }
    }

    public static @Nullable Ipv6Block intersection(@Nullable Ipv6Block a, @Nullable Ipv6Block b) {
        if (a == null || b == null) {
            return null;
        }

        var start = a.start().max(b.start());
        var end = a.end().min(b.end());
        return start.compareTo(end) <= 0 ? Ipv6Block.of(start, end) : null;
    }

    public static boolean overlaps(@Nullable Ipv6Block a, @Nullable Ipv6Block b) {
        return a != null
            && b != null
            && a.start().compareTo(b.end()) <= 0
            && a.end().compareTo(b.start()) >= 0;
    }

    public static @Nullable Ipv6Block merge(@Nullable Ipv6Block a, @Nullable Ipv6Block b) {
        if (!mergeable(a, b)) {
            return null;
        } else {
            return Ipv6Block.of(
                a.start().min(b.start()),
                a.end().max(b.end())
            );
        }
    }

    public static boolean mergeable(@Nullable Ipv6Block a, @Nullable Ipv6Block b) {
        if (a == null || b == null) {
            return false;
        } else if (overlaps(a, b)) {
            return true;
        } else {
            return a.end().successorOrLast().equals(b.start())
                || b.end().successorOrLast().equals(a.start());
        }
    }

    static @Nullable Ipv6Prefix prefixOrNull(Ipv6Address start, Ipv6Address end) {
        int length = getCommonPrefixLength(start, end);
        if (start.equals(lowerBoundForPrefix(start, length)) && end.equals(upperBoundForPrefix(end, length))) {
            return new Ipv6Prefix(start, length);
        } else {
            return null;
        }
    }

    public static boolean isLegalPrefix(Ipv6Address start, Ipv6Address end) {
        var prefixLength = getCommonPrefixLength(start, end);
        if (prefixLength == 0) {
            return start.hi == 0 && start.lo == 0 && end.hi == -1 && end.lo == -1;
        } else if (prefixLength < 64) {
            long mask = (1L << Ipv6Address.NUMBER_OF_BITS - prefixLength - 64) - 1;
            return start.lo == 0
                && (start.hi & mask) == 0L
                && end.lo == -1
                && (end.hi & mask) == mask;
        } else if (prefixLength == 64) {
            return start.hi == end.hi && start.lo == 0 && end.lo == -1;
        } else {
            long mask = (1L << Ipv6Address.NUMBER_OF_BITS - prefixLength) - 1;
            return start.hi == end.hi
                && (start.lo & mask) == 0L
                && (end.lo & mask) == mask;
        }
    }

    public static int getCommonPrefixLength(Ipv6Address start, Ipv6Address end) {
        int length;
        if (start.hi != end.hi) {
            long temp = start.hi ^ end.hi;
            length = Long.numberOfLeadingZeros(temp);
        } else {
            long temp = start.lo ^ end.lo;
            length = 64 + Long.numberOfLeadingZeros(temp);
        }
        return length;
    }

    public static boolean isPrefixLowerBound(@NotNull Ipv6Address prefix, int prefixLength) {
        if (prefixLength == 0) {
            return prefix.hi == 0 && prefix.lo == 0;
        } else if (prefixLength < 64) {
            long mask = (1L << Ipv6Address.NUMBER_OF_BITS - prefixLength - 64) - 1;
            return prefix.lo == 0
                && (prefix.hi & mask) == 0;
        } else if (prefixLength == 64) {
            return prefix.lo == 0;
        } else {
            long mask = (1L << Ipv6Address.NUMBER_OF_BITS - prefixLength) - 1;
            return (prefix.lo & mask) == 0;
        }
    }
}
