/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * A map with intervals as keys. Intervals are only allowed to overlap if they
 * are fully contained in the other interval (in other words, siblings are not
 * allowed to overlap, but nesting is ok).
 * <p/>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally. (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not a
 * structural modification.) This is typically accomplished by synchronizing on
 * some object that naturally encapsulates the map.
 *
 * @param <K> the type of the interval (must implement {@link IntervalStrategy}).
 * @param <V> the type of the values to store.
 */
public final class NestedIntervalMap<K, V> implements IntervalMap<K, V> {
    private final ChildNodeMap<K, V> children;

    private final IntervalStrategy<K> strategy;

    /**
     * Construct an empty {@link NestedIntervalMap}.
     */
    public NestedIntervalMap(IntervalStrategy<K> strategy) {
        this.strategy = strategy;
        this.children = new ChildNodeTreeMap<K, V>(strategy);
    }

    /**
     * Construct a new {@link NestedIntervalMap} with (key, values) of
     * <code>source</code> copied.
     *
     * @param source the source to copy.
     */
    public NestedIntervalMap(NestedIntervalMap<K, V> source, IntervalStrategy<K> strategy) {
        this.strategy = strategy;
        this.children = new ChildNodeTreeMap<K, V>(source.children, strategy);
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public void put(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);
        children.addChild(new InternalNode<K, V>(key, value), strategy);
    }

    @Override
    public void remove(K key) {
        Validate.notNull(key);
        children.removeChild(key, strategy);
    }

    @Override
    public void remove(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);

        if (value.equals(findExact(key))) {
            remove(key);
        }
    }

    @Override
    public V findFirstLessSpecific(K key) {
        Validate.notNull(key);
        InternalNode<K, V> node = internalFindFirstLessSpecific(key);
        return mapToValue(node);
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindAllLessSpecific(key));
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindExactAndAllLessSpecific(key));
    }

    @Override
    public V findExact(K key) {
        Validate.notNull(key);
        InternalNode<K, V> node = internalFindExact(key);
        return node == null ? null : node.getValue();
    }

    @Override
    public V findExactOrFirstLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValue(internalFindExactOrFirstLessSpecific(key));
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindFirstMoreSpecific(key));
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindAllMoreSpecific(key));
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindExactAndAllMoreSpecific(key));
    }

    /**
     * Clears all values from the map.
     */
    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + children.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NestedIntervalMap<?, ?> that = (NestedIntervalMap<?, ?>) obj;
        return this.children.equals(that.children);
    }

    @Override
    public String toString() {
        return children.toString();
    }

    private V mapToValue(InternalNode<K, V> node) {
        return node == null ? null : node.getValue();
    }

    private List<V> mapToValues(Collection<InternalNode<K, V>> nodes) {
        List<V> result = new ArrayList<V>(nodes.size());
        for (InternalNode<K, V> node : nodes) {
            result.add(node.getValue());
        }
        return result;
    }

    private InternalNode<K, V> internalFindExactOrFirstLessSpecific(K range) {
        List<InternalNode<K, V>> list = internalFindExactAndAllLessSpecific(range);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private InternalNode<K, V> internalFindFirstLessSpecific(K range) {
        List<InternalNode<K, V>> list = internalFindAllLessSpecific(range);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    private List<InternalNode<K, V>> internalFindAllLessSpecific(K range) {
        List<InternalNode<K, V>> result = internalFindExactAndAllLessSpecific(range);
        if (result.isEmpty()) {
            return result;
        }
        InternalNode<K, V> last = result.get(result.size() - 1);
        if (last.getKey().equals(range)) {
            return result.subList(0, result.size() - 1);
        } else {
            return result;
        }
    }

    private List<InternalNode<K, V>> internalFindExactAndAllLessSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        children.findExactAndAllLessSpecific(result, range, strategy);
        return result;
    }

    private InternalNode<K, V> internalFindExact(K range) {
        List<InternalNode<K, V>> exactAndAllLessSpecific = internalFindExactAndAllLessSpecific(range);
        if (exactAndAllLessSpecific.isEmpty()) {
            return null;
        }
        InternalNode<K, V> last = exactAndAllLessSpecific.get(exactAndAllLessSpecific.size() - 1);
        if (last.getKey().equals(range)) {
            return last;
        }
        return null;
    }

    private List<InternalNode<K, V>> internalFindFirstMoreSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        InternalNode<K, V> container = internalFindExactOrFirstLessSpecific(range);
        if (container == null) {
            children.findFirstMoreSpecific(result, range, strategy);
        } else {
            container.getChildren().findFirstMoreSpecific(result, range, strategy);
        }
        return result;
    }

    private List<InternalNode<K, V>> internalFindAllMoreSpecific(K range) {
        List<InternalNode<K, V>> result = internalFindExactAndAllMoreSpecific(range);
        if (!result.isEmpty() && result.get(0).getKey().equals(range)) {
            return result.subList(1, result.size());
        } else {
            return result;
        }
    }

    private List<InternalNode<K, V>> internalFindExactAndAllMoreSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        InternalNode<K, V> containing = internalFindExactOrFirstLessSpecific(range);
        if (containing == null) {
            children.findExactAndAllMoreSpecific(result, range, strategy);
        } else {
            if (containing.getKey().equals(range)) {
                result.add(containing);
            }
            containing.getChildren().findExactAndAllMoreSpecific(result, range, strategy);
        }
        return result;
    }

    public abstract static class Key<K extends IntervalStrategy<K>> {
        private final K key;

        public Key(K key) {
            Validate.notNull(key);
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key<?> that = (Key<?>) obj;
            return this.key.equals(that.key);
        }

        @Override
        public String toString() {
            return "IpResource(" + key + ")";
        }
    }
}
