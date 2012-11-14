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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Keeps a sorted map of child nodes ordered by the upper-bound of each child
 * interval. Intervals are not allowed to overlap (an
 * {@link OverlappingIntervalException} is thrown if an attempt is made to
 * insert siblings with overlapping intervals). This allows quick lookup of
 * matching intervals by comparing an interval's <em>lower-bound</em> with the
 * existing interval's <em>upper-bounds</em>.
 */
class ChildNodeTreeMap<K, V> extends TreeMap<K, InternalNode<K, V>> implements ChildNodeMap<K, V> {

    private static final long serialVersionUID = 1L;

    /*
     * Shared instance of empty child node map. This reduces memory usage, since
     * many nodes will not have any children.
     */
    @SuppressWarnings("rawtypes")
    static final ChildNodeMap EMPTY = new Empty();

    /**
     * @return an unmodifiable, empty {@link ChildNodeMap}.
     */
    @SuppressWarnings("unchecked")
    static <K, T> ChildNodeMap<K, T> empty() {
        return EMPTY;
    }

    ChildNodeTreeMap(IntervalStrategy<K> strategy) {
        super(strategy.upperBoundComparator());
    }

    public ChildNodeTreeMap(ChildNodeMap<K, V> source, IntervalStrategy<K> strategy) {
        this(strategy);
        for (InternalNode<K, V> node : source.values()) {
            this.put(node.getKey(), new InternalNode<K, V>(node, strategy));
        }
    }

    @Override
    public void addChild(InternalNode<K, V> nodeToAdd, IntervalStrategy<K> strategy) {
        K range = nodeToAdd.getKey();
        InternalNode<K, V> containingChild = getChildContaining(range, strategy);
        if (containingChild != null) {
            containingChild.addChild(nodeToAdd, strategy);
            return;
        }

        List<K> overlaps = getOverlappingChildren(range, strategy);
        if (!overlaps.isEmpty()) {
            throw new OverlappingIntervalException(range, overlaps);
        }

        transferChildNodes(nodeToAdd, strategy);

        this.put(range, nodeToAdd);
    }

    private void transferChildNodes(InternalNode<K, V> nodeToAdd, IntervalStrategy<K> strategy) {
        K range = nodeToAdd.getKey();
        for (Iterator<InternalNode<K, V>> it = this.tailMap(strategy.singletonIntervalAtLowerBound(range)).values().iterator(); it.hasNext(); ) {
            InternalNode<K, V> child = it.next();
            if (strategy.contains(range, child.getKey())) {
                nodeToAdd.addChild(child, strategy);
                it.remove();
            } else {
                break;
            }
        }
    }

    @Override
    public void removeChild(K interval, IntervalStrategy<K> strategy) {
        final InternalNode<K, V> containing = getChildContaining(interval, strategy);
        if (containing == null) {
            return;
        }

        if (interval.equals(containing.getKey())) {
            this.remove(interval);
            for (InternalNode<K, V> node : containing.getChildren().values()) {
                put(node.getKey(), node);
            }
        } else {
            containing.removeChild(interval, strategy);
        }
    }

    private List<K> getOverlappingChildren(K range, IntervalStrategy<K> strategy) {
        List<K> result = Collections.emptyList();
        K lowerCandidate = this.ceilingKey(strategy.singletonIntervalAtLowerBound(range));
        if (lowerCandidate != null && overlapsButNotContained(range, lowerCandidate, strategy)) {
            result = new ArrayList<K>(result);
            result.add(lowerCandidate);
        }
        K upperCandidate = this.ceilingKey(range);
        if (upperCandidate != null && overlapsButNotContained(range, upperCandidate, strategy)) {
            result = new ArrayList<K>(result);
            result.add(upperCandidate);
        }
        return result;
    }

    private boolean overlapsButNotContained(K left, K right, IntervalStrategy<K> strategy) {
        return strategy.overlaps(left, right) && !strategy.contains(left, right) && !strategy.contains(right, left);
    }

    private InternalNode<K, V> getChildContaining(K range, IntervalStrategy<K> strategy) {
        Entry<K, InternalNode<K, V>> entry = this.ceilingEntry(strategy.singletonIntervalAtLowerBound(range));
        if (entry != null && strategy.contains(entry.getKey(), range)) {
            return entry.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void findExactAndAllLessSpecific(List<InternalNode<K, V>> result, K range, IntervalStrategy<K> strategy) {
        InternalNode<K, V> node = getChildContaining(range, strategy);
        if (node != null) {
            result.add(node);
            node.getChildren().findExactAndAllLessSpecific(result, range, strategy);
        }
    }

    @Override
    public void findExactAndAllMoreSpecific(List<InternalNode<K, V>> result, K range, IntervalStrategy<K> strategy) {
        for (InternalNode<K, V> node : this.tailMap(strategy.singletonIntervalAtLowerBound(range)).values()) {
            if (strategy.contains(range, node.getKey())) {
                result.add(node);
                node.getChildren().addAllChildrenToList(result, strategy);
            } else if (strategy.overlaps(range, node.getKey())) {
                node.getChildren().findExactAndAllMoreSpecific(result, range, strategy);
            } else {
                break;
            }
        }
    }

    @Override
    public void findFirstMoreSpecific(List<InternalNode<K, V>> result, K range, IntervalStrategy<K> strategy) {
        for (InternalNode<K, V> node : this.tailMap(strategy.singletonIntervalAtLowerBound(range)).values()) {
            if (strategy.contains(range, node.getKey())) {
                result.add(node);
            } else if (strategy.overlaps(range, node.getKey())) {
                node.getChildren().findFirstMoreSpecific(result, range, strategy);
            } else {
                break;
            }
        }
    }

    @Override
    public void addAllChildrenToList(List<InternalNode<K, V>> list, IntervalStrategy<K> strategy) {
        for (InternalNode<K, V> node : this.values()) {
            list.add(node);
            node.getChildren().addAllChildrenToList(list, strategy);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class Empty extends TreeMap implements ChildNodeMap {
        private static final long serialVersionUID = 1L;

        @Override
        public void addChild(InternalNode childToAdd, IntervalStrategy strategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeChild(Object interval, IntervalStrategy strategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findExactAndAllLessSpecific(List list, Object range, IntervalStrategy strategy) {
        }

        @Override
        public void findExactAndAllMoreSpecific(List list, Object range, IntervalStrategy strategy) {
        }

        @Override
        public void findFirstMoreSpecific(List list, Object interval, IntervalStrategy strategy) {
        }

        @Override
        public void addAllChildrenToList(List list, IntervalStrategy strategy) {
        }
    }
}
