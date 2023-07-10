package net.ripe.ipresource.scratch;

public sealed interface IpPrefix extends IpBlock permits Ipv4Prefix, Ipv6Prefix {
}
