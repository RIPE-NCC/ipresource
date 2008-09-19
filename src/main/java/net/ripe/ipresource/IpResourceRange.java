package net.ripe.ipresource;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example: AS1-AS20
 */
public class IpResourceRange extends IpResource {

	private static final long serialVersionUID = 1L;

	private static final Pattern PREFIX_PATTERN = Pattern.compile("([^/]+)/(\\d+)");
    private static final Pattern RANGE_PATTERN = Pattern.compile("([^-]+)-([^-]+)");

    private UniqueIpResource start;
    private UniqueIpResource end;

    public static IpResourceRange range(UniqueIpResource start, UniqueIpResource end) {
        if (start instanceof IpAddress && end instanceof IpAddress) {
            return IpRange.range((IpAddress) start, (IpAddress) end);
        } else {
            return new IpResourceRange(start, end);
        }
    }

    protected IpResourceRange(UniqueIpResource start, UniqueIpResource end) {
        // ASSUMPTION: All ranges are bounded
        // This is the invariant!
        Validate.notNull(start, "start is null");
        Validate.notNull(end, "end is null");
        Validate.isTrue(start.getType() == end.getType(), "resource types do not match");
        Validate.isTrue(start.compareTo(end) <= 0, "range must not be empty: " + start + "-" + end);

        this.start = start;
        this.end = end;
    }

    // ------------------------------------------------------------ Range
    // Interface

    public UniqueIpResource getStart() {
        return start;
    }

    public UniqueIpResource getEnd() {
        return end;
    }

    public IpResourceType getType() {
        return start.getType();
    }

    @Override
    public String toString() {
        return start.toString() + "-" + end.toString();
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof UniqueIpResource) {
            return -obj.compareTo(this);
        } else if (obj instanceof IpResourceRange) {
            IpResource that = (IpResource) obj;
            int rc = getStart().compareTo(that.getStart());
            if (rc != 0) {
                return rc;
            } else {
                return -getEnd().compareTo(that.getEnd());
            }
        } else {
            throw new IllegalArgumentException("unknown resource type: " + obj);
        }
    }

    @Override
    protected int doHashCode() {
        return isUnique() ? unique().hashCode() : new HashCodeBuilder().append(start).append(end).toHashCode();
    }

    @Override
    public UniqueIpResource unique() {
        Validate.isTrue(isUnique(), "resource not unique");
        return getStart();
    }

    public static IpResourceRange parse(String s) {
        Matcher m = RANGE_PATTERN.matcher(s);
        if (m.matches()) {
            UniqueIpResource start = UniqueIpResource.parse(m.group(1));
            UniqueIpResource end = UniqueIpResource.parse(m.group(2));
            if (start.getType() != end.getType()) {
                throw new IllegalArgumentException("resource types in range do not match");
            }
            return IpResourceRange.range(start, end);
        }

        m = PREFIX_PATTERN.matcher(s);
        if (m.matches()) {
            IpAddress prefix = IpAddress.parse(m.group(1), true);
            int length = Integer.parseInt(m.group(2));
            return IpRange.prefix(prefix, length);
        }

        throw new IllegalArgumentException("illegal resource range: " + s);
    }
}
