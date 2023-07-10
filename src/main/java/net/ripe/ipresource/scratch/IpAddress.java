package net.ripe.ipresource.scratch;

public sealed interface IpAddress extends NumberResource permits Ipv4Address, Ipv6Address {
}
