package net.ripe.ipresource.scratch;

import net.ripe.ipresource.IpResourceType;
import net.ripe.ipresource.UniqueIpResource;
import org.jetbrains.annotations.NotNull;

public sealed interface NumberResource extends Comparable<NumberResource> permits IpAddress, Asn {
    IpResourceType getType();

    static NumberResource parse(@NotNull String s) {
        return switch (UniqueIpResource.parse(s)) {
            case net.ripe.ipresource.Asn asn -> Asn.of(asn.longValue());
            case net.ripe.ipresource.Ipv4Address ipv4 -> Ipv4Address.of(ipv4.longValue());
            default -> throw new IllegalArgumentException("unknown type");
        };
    }

    @NotNull NumberResource successorOrLast();
    @NotNull NumberResource predecessorOrFirst();

    default @NotNull NumberResourceRange upTo(@NotNull NumberResource that) {
        return  NumberResourceRange.range(this, that);
    }
}
