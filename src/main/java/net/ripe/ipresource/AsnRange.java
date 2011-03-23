package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;

public class AsnRange extends ResourceRange {
    private static final long serialVersionUID = 1L;

    public AsnRange(Asn start, Asn end) {
        super(start, end);
    }
    
    public static AsnRange parse(String string) {
        if (string == null) {
            return null;
        }
        int separatorIndex = string.indexOf('-');
        
        Validate.isTrue(separatorIndex > -1, "Not a valid ASN range " + string);
        
        Asn start = Asn.parse(string.substring(0, separatorIndex));
        Asn end = Asn.parse(string.substring(separatorIndex + 1));
        
        return new AsnRange(start, end);
    }
    
    @Override
    public String toString() {
        return String.format("%s-%s", start, end);
    }

    @Override
    public int compareTo(ResourceRange o) {
        if (o instanceof AsnRange) {
            int startCompareResult = start.compareTo(o.start);
            if (startCompareResult == 0) {
                return -(end.compareTo(o.end));
            }
            return startCompareResult;
        }
        return -1;
    }
    
    public List<AsnRange> subtract(AsnRange other) {
        if (!this.overlaps(other)) {
            return Collections.singletonList(new AsnRange((Asn) start, (Asn) end));
        } else if (other.contains(this)) {
            return Collections.emptyList();
        } else {
            List<AsnRange> result = new ArrayList<AsnRange>(2);
            if (start.value.compareTo(other.start.value) < 0) {
                result.add(new AsnRange((Asn) start, new Asn(other.start.value.subtract(BigInteger.ONE))));
            }
            if (other.end.value.compareTo(end.value) < 0) {
                result.add(new AsnRange(new Asn(other.end.value.add(BigInteger.ONE)), (Asn)end));
            }
            return result;
        }
    }
    
    public AsnRange merge(AsnRange other) {
        Validate.isTrue(this.isMergeable(other));
        BigInteger lowestStart = start.value.min(other.start.value);
        BigInteger highestEnd = end.value.max(other.end.value);
        return new AsnRange(new Asn(lowestStart), new Asn(highestEnd));
    }
    
    public AsnRange intersect(AsnRange other) {
        BigInteger maxStart = start.value.max(other.start.value);
        BigInteger minEnd = end.value.min(other.end.value);
        if (start.compareTo(end) > 0) {
            return null;
        } else {
            return new AsnRange(new Asn(maxStart), new Asn(minEnd));
        }
    }
}
