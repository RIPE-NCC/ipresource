/*
 * The BSD License
 *
 * Copyright (c) 2010-2023 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.TreeMap;

import static net.ripe.ipresource.scratch.NumberResourceBlock.RANGE_END_COMPARATOR;
import static net.ripe.ipresource.scratch.NumberResourceBlock.intersect;
import static net.ripe.ipresource.scratch.NumberResourceBlock.merge;
import static net.ripe.ipresource.scratch.NumberResourceBlock.overlaps;

public final class Util {
    private Util() {
    }

    static void add(@NotNull TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint, @NotNull NumberResourceBlock resource) {
        var iterator = resourcesByEndPoint.tailMap(predecessorOrFirstOfStart(resource), true).values().iterator();
        while (iterator.hasNext()) {
            NumberResourceBlock potentialMatch = iterator.next();
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

    static void addAll(@NotNull TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint, @NotNull Iterable<? extends NumberResourceBlock> resources) {
        for (NumberResourceBlock resource: resources) {
            add(resourcesByEndPoint, resource);
        }
    }

    static void remove(@NotNull TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint, @NotNull NumberResourceBlock resource) {
        NumberResource start = resource.start();
        var potentialMatch = resourcesByEndPoint.ceilingEntry(start);
        while (potentialMatch != null && overlaps(potentialMatch.getValue(), resource)) {
            resourcesByEndPoint.remove(potentialMatch.getKey());
            for (NumberResourceBlock range : potentialMatch.getValue().subtract(resource)) {
                resourcesByEndPoint.put(range.end(), range);
            }
            potentialMatch = resourcesByEndPoint.ceilingEntry(start);
        }
   }

    static void removeAll(@NotNull TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint, @NotNull Iterable<? extends NumberResourceBlock> resources) {
        for (NumberResourceBlock resource: resources) {
            remove(resourcesByEndPoint, resource);
        }
    }

    static boolean contains(@NotNull TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint, @NotNull NumberResourceBlock resourceRange) {
        var potentialMatch = resourcesByEndPoint.ceilingEntry(predecessorOrFirstOfStart(resourceRange));
        return potentialMatch != null && potentialMatch.getValue().contains(resourceRange);
    }

   static boolean intersects(@NotNull TreeMap<NumberResource, NumberResourceBlock> left, @NotNull TreeMap<NumberResource, NumberResourceBlock> right) {
       if (left.isEmpty() || right.isEmpty()) {
           return false;
       }
       Iterator<NumberResourceBlock> thisIterator = left.values().iterator();
       Iterator<NumberResourceBlock> thatIterator = right.values().iterator();
       NumberResourceBlock leftResource = thisIterator.next();
       NumberResourceBlock rightResource = thatIterator.next();
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

    public static @NotNull TreeMap<NumberResource, NumberResourceBlock> intersection(@NotNull TreeMap<NumberResource, NumberResourceBlock> left, @NotNull TreeMap<NumberResource, NumberResourceBlock> right) {
        if (left.isEmpty()) {
            return left;
        } else if (right.isEmpty()) {
            return right;
        } else {
            var result = new TreeMap<NumberResource, NumberResourceBlock>();
            Iterator<NumberResourceBlock> thisIterator = left.values().iterator();
            Iterator<NumberResourceBlock> thatIterator = right.values().iterator();
            NumberResourceBlock thisResource = thisIterator.next();
            NumberResourceBlock thatResource = thatIterator.next();
            while (thisResource != null && thatResource != null) {
                NumberResourceBlock intersect = intersect(thisResource, thatResource);
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
    private static NumberResource predecessorOrFirstOfStart(NumberResourceBlock resource) {
        return switch (resource) {
            case AsnBlock asnBlock -> Asn.of(Math.max(0, asnBlock.lowerBound() - 1));
            case Ipv4Prefix ipv4Prefix -> Ipv4Address.of(Math.max(0L, ipv4Prefix.lowerBound() - 1));
            case Ipv4Range ipv4Range -> Ipv4Address.of(Math.max(0L, ipv4Range.lowerBound() - 1));
            case Ipv6Prefix ipv6Prefix -> ipv6Prefix.prefix().predecessorOrFirst();
            case Ipv6Range ipv6RangeImpl -> ipv6RangeImpl.start().predecessorOrFirst();
        };
    }
}
