package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.TreeMap;

import static net.ripe.ipresource.scratch.NumberResourceRange.RANGE_END_COMPARATOR;
import static net.ripe.ipresource.scratch.NumberResourceRange.intersect;
import static net.ripe.ipresource.scratch.NumberResourceRange.merge;
import static net.ripe.ipresource.scratch.NumberResourceRange.overlaps;

public final class Util {
    private Util() {
    }

    static void add(@NotNull TreeMap<NumberResource, NumberResourceRange> resourcesByEndPoint, @NotNull NumberResourceRange resource) {
        var iterator = resourcesByEndPoint.tailMap(predecessorOrFirstOfStart(resource), true).values().iterator();
        while (iterator.hasNext()) {
            NumberResourceRange potentialMatch = iterator.next();
            var merged = merge(resource, potentialMatch);
            if (merged == null) {
                break;
            } else {
                iterator.remove();
                resource = merged;
            }
        }

        resourcesByEndPoint.put(resource.end(), resource);
    }

    static void addAll(@NotNull TreeMap<NumberResource, NumberResourceRange> resourcesByEndPoint, @NotNull Iterable<? extends NumberResourceRange> resources) {
        for (NumberResourceRange resource: resources) {
            add(resourcesByEndPoint, resource);
        }
    }

    static void remove(@NotNull TreeMap<NumberResource, NumberResourceRange> resourcesByEndPoint, @NotNull NumberResourceRange resource) {
        NumberResource start = resource.start();
        var potentialMatch = resourcesByEndPoint.ceilingEntry(start);
        while (potentialMatch != null && overlaps(potentialMatch.getValue(), resource)) {
            resourcesByEndPoint.remove(potentialMatch.getKey());
            for (NumberResourceRange range : potentialMatch.getValue().subtract(resource)) {
                resourcesByEndPoint.put(range.end(), range);
            }
            potentialMatch = resourcesByEndPoint.ceilingEntry(start);
        }
   }

    static void removeAll(@NotNull TreeMap<NumberResource, NumberResourceRange> resourcesByEndPoint, @NotNull Iterable<? extends NumberResourceRange> resources) {
        for (NumberResourceRange resource: resources) {
            remove(resourcesByEndPoint, resource);
        }
    }

    static boolean contains(@NotNull TreeMap<NumberResource, NumberResourceRange> resourcesByEndPoint, @NotNull NumberResourceRange resourceRange) {
        var potentialMatch = resourcesByEndPoint.ceilingEntry(predecessorOrFirstOfStart(resourceRange));
        return potentialMatch != null && potentialMatch.getValue().contains(resourceRange);
    }

   static boolean intersects(@NotNull TreeMap<NumberResource, NumberResourceRange> left, @NotNull TreeMap<NumberResource, NumberResourceRange> right) {
       if (left.isEmpty() || right.isEmpty()) {
           return false;
       }
       Iterator<NumberResourceRange> thisIterator = left.values().iterator();
       Iterator<NumberResourceRange> thatIterator = right.values().iterator();
       NumberResourceRange leftResource = thisIterator.next();
       NumberResourceRange rightResource = thatIterator.next();
       while (leftResource != null && rightResource != null) {
           if (overlaps(leftResource, rightResource)) {
               return true;
           }
           int compareTo = RANGE_END_COMPARATOR.compare(leftResource, rightResource);
           if (compareTo <= 0) {
               leftResource = thisIterator.hasNext() ? thisIterator.next() : null;
           }
           if (compareTo >= 0) {
               rightResource = thatIterator.hasNext() ? thatIterator.next() : null;
           }
       }
       return false;
   }

    public static @NotNull TreeMap<NumberResource, NumberResourceRange> intersection(@NotNull TreeMap<NumberResource, NumberResourceRange> left, @NotNull TreeMap<NumberResource, NumberResourceRange> right) {
        if (left.isEmpty()) {
            return left;
        } else if (right.isEmpty()) {
            return right;
        } else {
            var result = new TreeMap<NumberResource, NumberResourceRange>();
            Iterator<NumberResourceRange> thisIterator = left.values().iterator();
            Iterator<NumberResourceRange> thatIterator = right.values().iterator();
            NumberResourceRange thisResource = thisIterator.next();
            NumberResourceRange thatResource = thatIterator.next();
            while (thisResource != null && thatResource != null) {
                NumberResourceRange intersect = intersect(thisResource, thatResource);
                if (intersect != null) {
                    result.put(intersect.end(), intersect);
                }
                int compareTo = RANGE_END_COMPARATOR.compare(thisResource, thatResource);
                if (compareTo <= 0) {
                    thisResource = thisIterator.hasNext() ? thisIterator.next() : null;
                }
                if (compareTo >= 0) {
                    thatResource = thatIterator.hasNext() ? thatIterator.next() : null;
                }
            }
            return result;
        }
    }

    @NotNull
    private static NumberResource predecessorOrFirstOfStart(NumberResourceRange resource) {
        return switch (resource) {
            case AsnRange asnRange -> Asn.of(Math.max(0, asnRange.lowerBound() - 1));
            case Ipv4Prefix ipv4Prefix -> Ipv4Address.of(Math.max(0L, ipv4Prefix.lowerBound() - 1));
            case Ipv4Range ipv4Range -> Ipv4Address.of(Math.max(0L, ipv4Range.lowerBound() - 1));
            case Ipv6Prefix ipv6Prefix -> ipv6Prefix.prefix().predecessorOrFirst();
            case Ipv6Range ipv6RangeImpl -> ipv6RangeImpl.start().predecessorOrFirst();
        };
    }
}
