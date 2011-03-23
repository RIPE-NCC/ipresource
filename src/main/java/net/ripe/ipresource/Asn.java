package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ripe.ipresource.Resource;
import org.apache.commons.lang.Validate;

public class Asn extends Resource {

    private static final long serialVersionUID = 2L;

    private static final long ASN_MIN_VALUE = 0L;
    private static final long ASN16_MAX_VALUE = (1L << 16) - 1L;
    private static final long ASN32_MAX_VALUE = (1L << 32) - 1L;

    private static final Pattern ASN_TEXT_PATTERN = Pattern.compile("(?:AS)?(\\d+)(\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    public Asn(BigInteger value) {
        super(value);
    }
    
    public Asn(int value) {
        super(BigInteger.valueOf(value));
    }

    public Asn(long value) {
        super(BigInteger.valueOf(value));
    }

    public static Asn parse(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = ASN_TEXT_PATTERN.matcher(text.trim());
        
        Validate.isTrue(matcher.matches(), "Not a legal ASN: " + text);

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

        return new Asn(BigInteger.valueOf((high << 16) | low));
    }

    private static void checkRange(long value, long max) {
        Validate.isTrue(value >= ASN_MIN_VALUE);
        Validate.isTrue(value <= max);
    }

    public long longValue() {
        return value.longValue();
    }

    @Override
    public String toString() {
        return "AS" + value;
    }

    @Override
    public int compareTo(Resource other) {
        if (other instanceof Asn) {
            return value.compareTo(other.value);
        }
        return -1;
    }
}
