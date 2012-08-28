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

/**
 * A map with intervals as keys. Intervals are only allowed to overlap if they
 * are fully contained in the other interval (in other words, siblings are not
 * allowed to overlap, but nesting is ok).
 *
 * @param <K> the type of the interval (must implement {@link IntervalStrategy}).
 * @param <V> the type of the values to store.
 */
public interface IntervalMap<K, V> {

    /**
     * Associates the specified value with the specified key in this map If the
     * map previously contained a mapping for the key, the old value is replaced
     * by the specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws IllegalArgumentException     if the key or value is <code>null</code>
     * @throws OverlappingIntervalException if the key overlaps (but is not contained within) an existing
     *                                      key
     */
    void put(K key, V value);

    /**
     * Removes the mapping for a key from this map if it is present.
     * <p/>
     * <p/>
     * The map will not contain a mapping for the specified key once the call
     * returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @throws IllegalArgumentException if the specified key is null
     */
    void remove(K key);

    /**
     * Removes the mapping for a key and value from this map if both are present.
     * <p/>
     * <p/>
     * The map will not contain a mapping for the specified key with the specified value
     * once the call returns.
     *
     * @param key   key whose mapping is to be removed from the map
     * @param value value with the key to be removed from the map
     *
     * @throws IllegalArgumentException if the specified key or value is null
     */
    void remove(K key, V value);

    void clear();

    /**
     * Finds the value associated with closest interval that contains
     * <code>key</code> but is not equal to <code>key</code>.
     *
     * @param key the key to find the closest enclosing interval for
     * @return the value associated with the closest enclosing interval of
     *         <code>key</code>, or an empty list if no such mapping exists.
     */
    List<V> findFirstLessSpecific(K key);

    /**
     * Finds the value associated with <code>key</code>, if it exists.
     *
     * @param key the key to find the mapping for
     * @return the value associated with <code>key</code> or an empty list if no
     *         such value exists
     */
    List<V> findExact(K key);

    /**
     * Finds the value associated with <code>key</code>, or its closest
     * enclosing if <code>key</code> is not contained in this map, if it exists.
     *
     * @param key the key to find the mapping for
     * @return the value associated with <code>key</code> or its closest
     *         containing interval, or an empty list if no such value exists
     */
    List<V> findExactOrFirstLessSpecific(K key);

    /**
     * Finds all values that are associated to intervals that contain
     * <code>key</code> but are not equal to <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find all containing intervals for
     * @return the (possibly empty) list of values that are associated to
     *         intervals that contain <code>key</code> but is not equal to
     *         <code>key</code>.
     */
    List<V> findAllLessSpecific(K key);

    /**
     * Finds all values that are associated to intervals that contain
     * <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval. So if a mapping for <code>key</code> exists the last
     * element of the returned list will contain the value associated with
     * <code>key</code>.
     *
     * @param key the key to find all containing intervals for
     * @return the (possibly empty) list of values that are associated to
     *         intervals that contain <code>key</code>
     */
    List<V> findExactAndAllLessSpecific(K key);

    /**
     * Finds all values associated with intervals that are more specific
     * (contained in) <code>key</code>, but excluding the values that are nested
     * inside the matching intervals.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find the first level more specific values for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findFirstMoreSpecific(K key);

    /**
     * Finds all values associated with intervals that are contained within
     * (more specific than) <code>key</code>, but not equal to <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find all levels more specific values for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findAllMoreSpecific(K key);

    /**
     * Finds all values associated with intervals that are equal to
     * <code>key</code> or contained within (more specific than)
     * <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval. So if a mapping for <code>key</code> exists the first
     * element of the returned list will contain the value associated with
     * <code>key</code>.
     *
     * @param key the key to find the exact and all levels more specific values
     *            for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findExactAndAllMoreSpecific(K key);
}
