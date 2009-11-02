package net.ripe.ipresource;

import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class IpResource implements Serializable, Comparable<IpResource> {

    private static final long serialVersionUID = 1L;

    public abstract IpResourceType getType();

    public int compareTo(IpResource that) {
        int rc;

        rc = this.getType().compareTo(that.getType());
        if (rc != 0) {
            return rc;
        }

        return doCompareTo(that);
    }

    public boolean contains(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        return getStart().compareTo(other.getStart()) <= 0 && getEnd().compareTo(other.getEnd()) >= 0;
    }

    public boolean overlaps(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        return getStart().compareTo(other.getEnd()) <= 0 && getEnd().compareTo(other.getStart()) >= 0;
    }

    public boolean adjacent(IpResource other) {
        if (getType() != other.getType()) {
            return false;
        }
        if (overlaps(other)) {
            return false;
        }
        return getEnd().adjacent(other.getStart()) || getStart().adjacent(other.getEnd());
    }

    public boolean isMergeable(IpResource other) {
        return this.overlaps(other) || this.adjacent(other);
    }

    public IpResource merge(IpResource other) {
        Validate.isTrue(this.isMergeable(other));
        return getStart().min(other.getStart()).upTo(getEnd().max(other.getEnd()));
    }

    public boolean isUnique() {
        return getStart().equals(getEnd());
    }

    public abstract UniqueIpResource unique();

    public abstract UniqueIpResource getStart();

    public abstract UniqueIpResource getEnd();

    protected abstract int doCompareTo(IpResource that);

    protected abstract int doHashCode();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof IpResource)) {
            return false;
        }
        return compareTo((IpResource) other) == 0;
    }

    @Override
    public final int hashCode() {
        return doHashCode();
    }

    /**
     * Subtracts the range <code>value</code> from the current resource.
     * Depending on the operands this can result in 0, 1, or 2 new ranges being
     * returned.
     *
     * @param value the range to subtract.
     * @return non-null list (possibly empty) of resulting ranges.
     */
    public List<IpResourceRange> subtract(IpResource value) {
        if (!this.overlaps(value)) {
            return Collections.singletonList(getStart().upTo(getEnd()));
        } else if (value.contains(this)) {
            return Collections.emptyList();
        } else {
            List<IpResourceRange> result = new ArrayList<IpResourceRange>(2);
            if (getStart().compareTo(value.getStart()) < 0) {
                result.add(getStart().upTo(value.getStart().predecessor()));
            }
            if (value.getEnd().compareTo(getEnd()) < 0) {
                result.add(value.getEnd().successor().upTo(getEnd()));
            }
            return result;
        }
    }

    public IpResource intersect(IpResource value) {
        if (getType() != value.getType()) {
            return null;
        }
        
        UniqueIpResource start = getStart().max(value.getStart());
        UniqueIpResource end = getEnd().min(value.getEnd());
        if (start.compareTo(end) > 0) {
            return null;
        } else {
            return start.upTo(end);
        }
    }

    public static IpResource parse(String s) {
        try {
            return IpResourceRange.parse(s);
        } catch (IllegalArgumentException ex) {
            return UniqueIpResource.parse(s);
        }
    }
}
