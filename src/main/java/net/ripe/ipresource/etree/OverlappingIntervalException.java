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
package net.ripe.ipresource.etree;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown to indicate that an attempt was made to insert an interval that would
 * overlap with its siblings.
 */
public class OverlappingIntervalException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private final Object interval;

    private final List<?> overlaps;

    public OverlappingIntervalException(Object interval, List<?> overlaps) {
        super(String.format("%s overlaps with existing siblings %s", interval, overlaps));
        this.interval = interval;
        this.overlaps = new ArrayList<Object>(overlaps);
    }

    /**
     * @return the interval that overlaps with existing intervals.
     */
    public Object getInterval() {
        return interval;
    }

    /**
     * @return the existing intervals that overlap with the interval being
     *         added.
     */
    public List<?> getOverlaps() {
        return overlaps;
    }
}
