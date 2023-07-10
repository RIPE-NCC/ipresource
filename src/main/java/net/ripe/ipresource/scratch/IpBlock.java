package net.ripe.ipresource.scratch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface IpBlock extends NumberResourceRange permits IpPrefix, Ipv4Block, Ipv6Block {
    @NotNull IpAddress start();
    @NotNull IpAddress end();

    static @NotNull IpBlock range(IpAddress start, IpAddress end) {
        return switch (start) {
            case Ipv4Address x -> Ipv4Block.of(x, (Ipv4Address) end);
            case Ipv6Address x -> Ipv6Block.of(x, (Ipv6Address) end);
        };
    }

    static @Nullable IpBlock merge(@Nullable IpBlock a, @Nullable IpBlock b) {
        return switch (a) {
            case null -> null;
            case Ipv4Prefix x -> b instanceof Ipv4Block y ? Ipv4Block.merge(x, y) : null;
            case Ipv4Range x -> b instanceof Ipv4Block y ? Ipv4Block.merge(x, y) : null;
            case Ipv6Prefix x -> b instanceof Ipv6Block y ? Ipv6Block.merge(x, y) : null;
            case Ipv6Range x -> b instanceof Ipv6Block y ? Ipv6Block.merge(x, y) : null;
        };
    }
}
