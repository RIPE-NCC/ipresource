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

import org.apache.commons.lang.Validate;

final class InternalNode<K extends Interval<K>, V> {

    private final K interval;
    private V value;
    private ChildNodeMap<K, V> children = ChildNodeTreeMap.empty();

    public InternalNode(K interval, V value) {
        Validate.notNull(interval, "interval");
        Validate.notNull(value, "value");
        this.interval = interval;
        this.value = value;
    }

    public InternalNode(InternalNode<K, V> source) {
        this.interval = source.interval;
        this.value = source.value;
        this.children = source.children == ChildNodeTreeMap.EMPTY ? ChildNodeTreeMap.<K, V>empty() : new ChildNodeTreeMap<K, V>(source.children);
    }

    public K getInterval() {
        return interval;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + interval.hashCode();
        result = prime * result + value.hashCode();
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
        InternalNode<?, ?> that = (InternalNode<?, ?>) obj;
        return this.interval.equals(that.interval) && this.value.equals(that.value) && this.children.equals(that.children);
    }

    @Override
    public String toString() {
        return "Node(" + interval + ", " + value + ", " + children + ")";
    }

    ChildNodeMap<K, V> getChildren() {
        return children;
    }

    void addChild(InternalNode<K, V> nodeToAdd) {
        if (interval.equals(nodeToAdd.getInterval())) {
            this.value = nodeToAdd.getValue();
        } else if (!interval.contains(nodeToAdd.getInterval())) {
            throw new IllegalArgumentException(nodeToAdd.getInterval() + " not properly contained in " + interval);
        } else {
            if (children == ChildNodeTreeMap.EMPTY) {
                children = new ChildNodeTreeMap<K, V>();
            }
            children.addChild(nodeToAdd);
        }
    }

    public void removeChild(K range) {
        if (!interval.contains(range) || interval.equals(range)) {
            throw new IllegalArgumentException(range + " not properly contained in " + interval);
        }
        if (children != ChildNodeTreeMap.EMPTY) {
            children.removeChild(range);
            if (children.isEmpty()) {
                children = ChildNodeTreeMap.empty();
            }
        }
    }
}
