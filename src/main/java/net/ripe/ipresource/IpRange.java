package net.ripe.ipresource;

public abstract class IpRange extends ResourceRange {

    private static final long serialVersionUID = 1L;

    protected IpRange(IpAddress start, IpAddress end) {
        super(start, end);
    }

    public abstract boolean isValidNetmask();

    public static IpRange parse(String string) {
        if (string.indexOf(':') > -1) {
            return Ipv6Range.parse(string);
        }
        return Ipv4Range.parse(string);
    }

    protected abstract int getBitSize();

    //
    // protected IpRange(IpAddress start, IpAddress end) {
    // super(start, end);
    // }
    //
    // // Parsing.
    //
    // /**
    // * Parses an IP address range in either <em>prefix</em> or <em>range</em>
    // * notation.
    // *
    // * @param s
    // * the string to parse (non-null).
    // * @return an IP address range (non-null).
    // * @exception IllegalArgumentException
    // * the string to parse does not represent a valid IP address
    // * range or prefix.
    // */
    // public static IpRange parse(String s) {
    // ResourceRange result = IpResourceRange.parse(s);
    // Validate.isTrue(result instanceof IpRange, "range is not an IP address range: " + s);
    // return (IpRange) result;
    // }
    //
    // // ------------------------------------------------------------ Prefix
    // // behavior
    //

    //

    //
    //
    //
    //
    //
    //
    //
    //
    //
}
