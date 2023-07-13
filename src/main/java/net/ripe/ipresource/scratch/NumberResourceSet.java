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

import net.ripe.ipresource.IpResourceType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.ripe.ipresource.scratch.NumberResourceBlock.overlaps;

/**
 * An immutable set of IP resources. Resources can be ASNs, IPv4 addresses, IPv6
 * addresses, or ranges. Adjacent resources are merged. Single-sized ranges are
 * normalized into single resources.
 */
public final class NumberResourceSet implements Iterable<NumberResourceBlock> {

    public static final NumberResourceSet IP_PRIVATE_USE_RESOURCES = NumberResourceSet.parse("10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fc00::/7");
    public static final NumberResourceSet ASN_PRIVATE_USE_RESOURCES = NumberResourceSet.parse("AS64512-AS65534,AS4200000000-AS4294967294");
    public static final NumberResourceSet ALL_PRIVATE_USE_RESOURCES = ASN_PRIVATE_USE_RESOURCES.union(IP_PRIVATE_USE_RESOURCES);

    private static final NumberResourceSet EMPTY = new NumberResourceSet();
    private static final NumberResourceSet UNIVERSAL = NumberResourceSet.of(NumberResourceBlock.ALL_AS_RESOURCES, NumberResourceBlock.ALL_IPV4_RESOURCES, NumberResourceBlock.ALL_IPV6_RESOURCES);

    /*
     * Resources keyed by their end-point. This allows fast lookup to find potentially overlapping resources:
     *
     * resourcesByEndPoint.ceilingEntry(resourceToLookup.getStart())
     */
    private final TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint;

    private NumberResourceSet() {
        this.resourcesByEndPoint = new TreeMap<>();
    }

    private NumberResourceSet(TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint) {
        if (resourcesByEndPoint.isEmpty()) {
            throw new IllegalArgumentException("empty resource set must use ImmutableResourceSet.empty()");
        }
        this.resourcesByEndPoint = resourcesByEndPoint;
    }


    public static NumberResourceSet of() {
        return empty();
    }

    public static NumberResourceSet of(NumberResourceBlock resource) {
        TreeMap<NumberResource, NumberResourceBlock> resourcesByEndpoint = new TreeMap<>();
        resourcesByEndpoint.put(resource.end(), resource);
        return new NumberResourceSet(resourcesByEndpoint);
    }

    public static NumberResourceSet of(NumberResourceBlock... resources) {
        return resources.length == 0 ? empty() : NumberResourceSet.of(Arrays.asList(resources));
    }

    public static NumberResourceSet of(Iterable<? extends NumberResourceBlock> resources) {
        if (resources instanceof NumberResourceSet) {
            return (NumberResourceSet) resources;
        } else {
            return new Builder(resources).build();
        }
    }

    public static NumberResourceSet empty() {
        return EMPTY;
    }

    public static NumberResourceSet universal() {
        return UNIVERSAL;
    }

    public static Collector<NumberResourceBlock, NumberResourceSet.Builder, NumberResourceSet> collector() {
        return Collector.of(
            Builder::new,
            Builder::add,
            (a, b) -> a.addAll(b.resourcesByEndPoint.values()),
            Builder::build,
            Collector.Characteristics.UNORDERED
        );
    }

    public NumberResourceSet add(NumberResourceBlock value) {
        if (this.contains(value)) {
            return this;
        } else {
            return new Builder(this).add(value).build();
        }
    }

    public NumberResourceSet remove(NumberResourceBlock value) {
        if (!this.intersects(value)) {
            return this;
        }
        return new Builder(this).remove(value).build();
    }

    public NumberResourceSet union(NumberResourceSet that) {
        var left = this.resourcesByEndPoint;
        var right = that.resourcesByEndPoint;
        if (left.isEmpty()) {
            return that;
        } else if (right.isEmpty()) {
            return this;
        } else if (left.size() < right.size()) {
            return new Builder(that).addAll(left.values()).build();
        } else {
            return new Builder(this).addAll(right.values()).build();
        }
    }

    public NumberResourceSet intersection(NumberResourceSet that) {
        if (this.isEmpty()) {
            return this;
        } else if (that.isEmpty()) {
            return that;
        } else {
            var temp = Util.intersection(this.resourcesByEndPoint, that.resourcesByEndPoint);
            return temp.isEmpty() ? NumberResourceSet.empty() : new NumberResourceSet(temp);
        }
    }

    public NumberResourceSet difference(NumberResourceSet that) {
        if (that.isEmpty()) {
            return this;
        } else {
            return new Builder(this).removeAll(that).build();
        }
    }

    public NumberResourceSet complement() {
        return universal().difference(this);
    }

    @Override
    public @NotNull Iterator<NumberResourceBlock> iterator() {
        return Collections.unmodifiableMap(resourcesByEndPoint).values().iterator();
    }

    @Override
    public Spliterator<NumberResourceBlock> spliterator() {
        return Spliterators.spliterator(resourcesByEndPoint.values(),
            Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.IMMUTABLE);
    }

    public Stream<NumberResourceBlock> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public boolean isEmpty() {
        return resourcesByEndPoint.isEmpty();
    }

    public boolean contains(NumberResourceBlock resource) {
        return Util.contains(resourcesByEndPoint, resource);
    }

    public boolean contains(Iterable<? extends NumberResourceBlock> other) {
        for (NumberResourceBlock resource: other) {
            if (!contains(resource)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsType(IpResourceType type) {
        for (NumberResource resource: resourcesByEndPoint.keySet()) {
            if (type == resource.getType()) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(NumberResourceBlock resource) {
        var potentialMatch = resourcesByEndPoint.ceilingEntry(startPredecessor(resource));
        return potentialMatch != null && overlaps(resource, potentialMatch.getValue());
    }

    public boolean intersects(@NotNull NumberResourceSet that) {
        return Util.intersects(this.resourcesByEndPoint, that.resourcesByEndPoint);
    }

    public static NumberResourceSet parse(String s) {
        String[] resources = s.split(",");
        Builder builder = new Builder();
        for (String r : resources) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
                builder.add(NumberResourceBlock.parse(trimmed));
            }
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return resourcesByEndPoint.values().stream().map(Objects::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NumberResourceSet that && this.resourcesByEndPoint.equals(that.resourcesByEndPoint);
    }

    @Override
    public int hashCode() {
        return 'S' + resourcesByEndPoint.hashCode();
    }

    public static class Builder {
        private TreeMap<NumberResource, NumberResourceBlock> resourcesByEndPoint;

        public Builder() {
            this.resourcesByEndPoint = new TreeMap<>();
        }

        public Builder(NumberResourceSet resources) {
            this.resourcesByEndPoint = new TreeMap<>(resources.resourcesByEndPoint);
        }

        public Builder(Iterable<? extends NumberResourceBlock> resources) {
            if (resources instanceof NumberResourceSet set) {
                this.resourcesByEndPoint = new TreeMap<>(set.resourcesByEndPoint);
            } else {
                this.resourcesByEndPoint = new TreeMap<>();
                for (NumberResourceBlock resource : resources) {
                    add(resource);
                }
            }
        }

        public NumberResourceSet build() {
            assertNotAlreadyUsed();
            try {
                return resourcesByEndPoint.isEmpty() ? empty() : new NumberResourceSet(resourcesByEndPoint);
            } finally {
                resourcesByEndPoint = null;
            }
        }

        public Builder add(@NotNull NumberResourceBlock resource) {
            assertNotAlreadyUsed();
            Util.add(resourcesByEndPoint, resource);
            return this;
        }

        public Builder addAll(Iterable<? extends NumberResourceBlock> resources) {
            assertNotAlreadyUsed();
            Util.addAll(resourcesByEndPoint, resources);
            return this;
        }

        public Builder remove(@NotNull NumberResourceBlock resource) {
            assertNotAlreadyUsed();
            Util.remove(resourcesByEndPoint, resource);
            return this;
        }

        public Builder removeAll(Iterable<? extends NumberResourceBlock> resources) {
            assertNotAlreadyUsed();
            Util.removeAll(resourcesByEndPoint, resources);
            return this;
        }

        private void assertNotAlreadyUsed() {
            if (resourcesByEndPoint == null) {
                throw new IllegalStateException("builder can only be used once");
            }
        }
    }

    @NotNull
    private static NumberResource startPredecessor(NumberResourceBlock resource) {
        return switch (resource) {
            case AsnBlock asnBlock -> Asn.of(Math.max(0, asnBlock.lowerBound() - 1));
            case Ipv4Prefix ipv4Prefix -> Ipv4Address.of(Math.max(0L, ipv4Prefix.lowerBound() - 1));
            case Ipv4Range ipv4Range -> Ipv4Address.of(Math.max(0L, ipv4Range.lowerBound() - 1));
            case Ipv6Prefix ipv6Prefix -> ipv6Prefix.prefix().predecessorOrFirst();
            case Ipv6Range ipv6RangeImpl -> ipv6RangeImpl.start().predecessorOrFirst();
        };
    }
}
