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

import java.util.List;

public final class SynchronizedIntervalMap<K extends Interval<K>, V> implements IntervalMap<K, V> {

    private final Object mutex;
    private final IntervalMap<K, V> wrapped;

    public static <K extends Interval<K>, V> IntervalMap<K, V> synchronizedMap(IntervalMap<K, V> toWrap) {
        return new SynchronizedIntervalMap<K, V>(toWrap);
    }

    public static <K extends Interval<K>, V> IntervalMap<K, V> synchronizedMap(IntervalMap<K, V> toWrap, final Object mutex) {
        return new SynchronizedIntervalMap<K, V>(toWrap, mutex);
    }

    private SynchronizedIntervalMap(final IntervalMap<K, V> wrapped) {
        this.wrapped = wrapped;
        this.mutex = this;
    }

    private SynchronizedIntervalMap(final IntervalMap<K, V> wrapped, final Object mutex) {
        this.wrapped = wrapped;
        this.mutex = mutex;
    }

    @Override
    public void put(K key, V value) {
        synchronized (mutex) {
            wrapped.put(key, value);
        }
    }

    @Override
    public void remove(K key) {
        synchronized (mutex) {
            wrapped.remove(key);
        }
    }

    @Override
    public void remove(K key, V value) {
        synchronized (mutex) {
            wrapped.remove(key, value);
        }
    }

    @Override
    public List<V> findFirstLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findFirstLessSpecific(key);
        }
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findAllLessSpecific(key);
        }
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactAndAllLessSpecific(key);
        }
    }

    @Override
    public List<V> findExact(K key) {
        synchronized (mutex) {
            return wrapped.findExact(key);
        }
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactOrFirstLessSpecific(key);
        }
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findFirstMoreSpecific(key);
        }
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findAllMoreSpecific(key);
        }
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactAndAllMoreSpecific(key);
        }
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            wrapped.clear();
        }
    }
}
