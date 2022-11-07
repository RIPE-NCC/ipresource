RIPE NCC - IP Resource
====================================

License
-------
This library is distributed under the BSD License.
See: https://raw.github.com/RIPE-NCC/ipresource/master/LICENSE.txt

Description
-----------

This library contains a representation of IP number resources:

* IPv4 addresses
* IPv6 addresses
* Autonomous System Numbers (AS numbers).
* Ranges of the above (`AsnRange`, `IpRange` which both extend `IpResourceRange`)
* Sets of number resources (`ImmutableResourceSet` and `IpResourceSet`)

Finally, an optimised `IntervalMap` allows you to keep track of many number resources including support for querying
the (closest) enclosing resources, more specifics, etc.
