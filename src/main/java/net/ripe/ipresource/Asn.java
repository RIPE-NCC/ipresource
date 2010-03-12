package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Immutable value object for Autonomous System Numbers.
 */
public class Asn extends UniqueIpResource {

	private static final long serialVersionUID = 2L;

	private static final Pattern ASN_TEXT_PATTERN = Pattern.compile("AS(\\d+)(\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    private static long ASN_MIN_VALUE = 0L;
    private static long ASN16_MAX_VALUE = (1L << 16) - 1L;
    private static long ASN32_MAX_VALUE = (1L << 32) - 1L;

    // Use long to easily represent 32-bit unsigned integers.
    private final long value;

    @Deprecated
    public Asn(BigInteger value) {
        this(value.longValue());
    }
    
    public Asn(long value) {
        super(IpResourceType.ASN);
        checkRange(value, ASN32_MAX_VALUE);
        this.value = value;
    }

    public static Asn parse(String text) {
        if (text == null) {
            return null;
        }

        text = text.trim();

        Matcher matcher = ASN_TEXT_PATTERN.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a legal ASN: " + text);
        }

        long high = 0L;
        long low;

        if (matcher.group(3) != null) {
            low = Long.valueOf(matcher.group(3));
            high = Long.valueOf(matcher.group(1));

            checkRange(high, ASN16_MAX_VALUE);
            checkRange(low, ASN16_MAX_VALUE);
        } else {
            low = Long.valueOf(matcher.group(1));

            checkRange(low, ASN32_MAX_VALUE);
        }

        return new Asn((high << 16) | low);
    }

    private static void checkRange(long value, long max) {
        Validate.isTrue(value >= ASN_MIN_VALUE);
        Validate.isTrue(value <= max);
    }
    
    public long longValue() {
        return value;
    }

    @Override
    protected int doHashCode() {
        return new HashCodeBuilder().append(value).toHashCode();
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof Asn) {
            long otherValue = ((Asn) obj).value;
            if (value < otherValue) {
                return -1;
            } else if (value > otherValue) {
                return +1;
            } else {
                return 0;
            }
        } else {
            return super.doCompareTo(obj);
        }
    }
    
    @Override
    public String toString() {
        return "AS" + value;
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress lowerBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    public final BigInteger getValue() {
        return BigInteger.valueOf(value);
    }

}
