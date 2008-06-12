package net.ripe.ipresource;

import org.apache.commons.lang.Validate;

@SuppressWarnings("serial")
/**
 * Example IP Range: 192.168.0.1-192.168.1.10
 */
public class IpRange extends IpResourceRange {

    // Construction.

    public static IpRange range(IpAddress start, IpAddress end) {
        return new IpRange(start, end);
    }

    public static IpRange prefix(IpAddress networkNumber, int prefixLength) {
        Validate.notNull(networkNumber, "network number not null");
        Validate.isTrue(prefixLength >= 0);
        Validate.isTrue(prefixLength <= networkNumber.getType().getBitSize());
        return new IpRange(networkNumber, prefixLength);
    }

    protected IpRange(IpAddress start, IpAddress end) {
        super(start, end);
    }

    // Parsing.

    /**
     * Parses an IP address range in either <em>prefix</em> or <em>range</em>
     * notation.
     *
     * @param s
     *            the string to parse (non-null).
     * @return an IP address range (non-null).
     * @exception IllegalArgumentException
     *                the string to parse does not represent a valid IP address
     *                range or prefix.
     */
    public static IpRange parse(String s) {
        IpResource result = IpResourceRange.parse(s);
        Validate.isTrue(result instanceof IpRange, "range is not an IP address range: " + s);
        return (IpRange) result;
    }

    // ------------------------------------------------------------ Prefix
    // behavior

    protected IpRange(IpAddress networkNumber, int prefixLength) {
        this(networkNumber, networkNumber.upperBoundForPrefix(prefixLength));
        Validate.isTrue(networkNumber.equals(networkNumber.lowerBoundForPrefix(prefixLength)),
                "not a valid prefix: " + networkNumber + "/" + prefixLength);
    }

    public boolean isLegalPrefix() {
        int n = getPrefixLength();
        return getStart().equals(getStart().lowerBoundForPrefix(n)) && getEnd().equals(getEnd().upperBoundForPrefix(n));
    }

    public int getPrefixLength() {
        return getStart().getCommonPrefixLength(getEnd());
    }

    @Override
    public String toString() {
        if (isLegalPrefix()) {
            return ((IpAddress) getStart()).toString(true) + "/" + getPrefixLength();
        } else {
            return super.toString();
        }
    }

}
