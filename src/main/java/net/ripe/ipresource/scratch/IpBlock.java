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
