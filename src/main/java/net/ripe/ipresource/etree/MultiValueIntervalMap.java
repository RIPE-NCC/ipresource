/**
 * The BSD License
 *
 * Copyright (c) 2010, 2011 RIPE NCC
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

import static net.ripe.ipresource.etree.NestedIntervalMap.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public final class MultiValueIntervalMap<K, V> implements IntervalMap<K, V> {
    private final IntervalMap<K, SortedSet<V>> wrapped;

    public MultiValueIntervalMap(IntervalStrategy<K> strategy) {
        this.wrapped = new NestedIntervalMap<K, SortedSet<V>>(strategy);
    }

    @Override
    public void put(K key, V value) {
        SortedSet<V> set = uniqueResult(wrapped.findExact(key));
        if (set == null) {
            set = new TreeSet<V>();
            wrapped.put(key, set);
        }

        set.add(value);
    }

    @Override
    public void remove(K key) {
        wrapped.remove(key);
    }

    @Override
    public void remove(K key, V value) {
        SortedSet<V> set = uniqueResult(wrapped.findExact(key));
        if (set == null) {
            return;
        }

        set.remove(value);

        if (set.isEmpty()) {
            wrapped.remove(key);
        }
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    private static <V> List<V> unroll(final List<SortedSet<V>> sets) {
        int size = 0;
        for (final SortedSet<V> set : sets) {
            size += set.size();
        }

        final List<V> result = new ArrayList<V>(size);
        for (final SortedSet<V> set : sets) {
            result.addAll(set);
        }

        return result;
    }

    @Override
    public List<V> findFirstLessSpecific(K key) {
        return unroll(wrapped.findFirstLessSpecific(key));
    }

    @Override
    public List<V> findExact(K key) {
        return unroll(wrapped.findExact(key));
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(K key) {
        return unroll(wrapped.findExactOrFirstLessSpecific(key));
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        return unroll(wrapped.findAllLessSpecific(key));
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        return unroll(wrapped.findExactAndAllLessSpecific(key));
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        return unroll(wrapped.findFirstMoreSpecific(key));
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        return unroll(wrapped.findAllMoreSpecific(key));
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        return unroll(wrapped.findExactAndAllMoreSpecific(key));
    }
}
