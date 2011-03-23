package net.ripe.ipresource;

import java.io.Serializable;
import java.math.BigInteger;
import org.apache.commons.lang.Validate;

public abstract class ResourceRange implements Serializable, Comparable<ResourceRange> {

    private static final long serialVersionUID = 1L;

    protected final Resource start;
    protected final Resource end;

    protected ResourceRange(Resource start, Resource end) {
        Validate.isTrue(start.compareTo(end) <= 0);
        this.start = start;
        this.end = end;
    }

    public static ResourceRange range(Resource start, Resource end) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.isTrue(start.getClass().equals(end.getClass()));
        if (start instanceof Asn) {
            return new AsnRange((Asn) start, (Asn) end);
        }
        if (start instanceof Ipv4Address) {
            return new Ipv4Range((Ipv4Address) start, (Ipv4Address) end);
        }
        return new Ipv6Range((Ipv6Address) start, (Ipv6Address) end);
    }

    @Override
    public abstract int compareTo(ResourceRange o);

    public static ResourceRange parse(String string) {
        if (string.indexOf(':') > -1) {
            return Ipv6Range.parse(string);
        }
        if (string.indexOf('/') > -1 || string.indexOf('.') > -1) {
            return Ipv4Range.parse(string);
        }
        return AsnRange.parse(string);
    }

    public boolean contains(ResourceRange other) {
        if (!other.getClass().isInstance(this)) {
            return false;
        }
        return start.compareTo(other.start) <= 0 && end.compareTo(other.end) >= 0;
    }

    public boolean overlaps(ResourceRange other) {
        if (!other.getClass().isInstance(this)) {
            return false;
        }
        return start.compareTo(other.end) <= 0 && end.compareTo(other.start) >= 0;
    }

    public boolean adjacent(ResourceRange other) {
        if (!other.getClass().isInstance(this)) {
            return false;
        }
        return end.value.add(BigInteger.ONE).equals(other.start.value) || other.end.value.add(BigInteger.ONE).equals(start.value);
    }

    public boolean isMergeable(ResourceRange other) {
        return this.overlaps(other) || this.adjacent(other);
    }

    public boolean isUnique() {
        return start.equals(end);
    }
    
    public Resource unique() {
        return start;
    }

    //
    // public abstract UniqueIpResource unique();
    //
    // public abstract UniqueIpResource getStart();
    //
    // public abstract UniqueIpResource getEnd();
    //
    // public boolean isValidNetmask() {
    // return false;
    // }
    //
    // protected abstract int doCompareTo(ResourceRange that);
    //
    // protected abstract int doHashCode();
    //

    //

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof ResourceRange)) {
            return false;
        }
        return compareTo((ResourceRange) other) == 0;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", start, end);
    }
}
