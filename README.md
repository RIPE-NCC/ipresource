RIPE NCC - IP Resource
======================

License
-------
This library is distributed under the BSD License.  See:
https://raw.github.com/RIPE-NCC/ipresource/master/LICENSE.txt

Description
-----------

This library contains a representation of IP number resources:

* IPv4 addresses
* IPv6 addresses
* Autonomous System Numbers (AS numbers).
* Ranges of the above (`AsnRange`, `IpRange` which both extend `IpResourceRange`)
* Sets of number resources (`ImmutableResourceSet` and `IpResourceSet`)

Finally, an optimised `IntervalMap` allows you to keep track of many
number resources including support for querying the (closest)
enclosing resources, more specifics, etc.

Releasing
---------

To release ipresource to the central maven repositories you should:

1. Update the version in `pom.xml` (e.g. 1.50) and commit this on
   `main`.
2. Create an annotated tag `ipresource-<version>`
   (e.g. `ipresource-1.50`).
3. Push to github (`git push`) and then push tags to github (`git push
   --tags`).
4. The RIPE NCC gitlab CI/CD environment will pick this up and publish
   to the central maven repositories.
5. Once this succeeds, update the `pom.xml` version to the new
   snapshot version, e.g. `1.51-SNAPSHOT`.
