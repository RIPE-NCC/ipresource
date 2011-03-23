package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.Validate;


public class Ipv6Range extends IpRange {
    private static final long serialVersionUID = 1L;
    
    public Ipv6Range(Ipv6Address start, Ipv6Address end) {
        super(start, end);
    }
    
    private Ipv6Range(Ipv6Address networkNumber, int prefixLength) {
        this(networkNumber, networkNumber.upperBoundForPrefix(prefixLength));
    }

    public static Ipv6Range prefix(Ipv6Address networkNumber, int prefixLength) {
        Validate.notNull(networkNumber, "network number can't be null");
        Validate.isTrue(prefixLength >= 0);
        Validate.isTrue(prefixLength <= networkNumber.getBitSize());
        return new Ipv6Range(networkNumber, prefixLength);
    }
    
    public List<Ipv6Range> subtract(Ipv6Range value) {
        if (!this.overlaps(value)) {
            return Collections.singletonList(new Ipv6Range((Ipv6Address) start, (Ipv6Address) end));
        } else if (value.contains(this)) {
            return Collections.emptyList();
        } else {
            List<Ipv6Range> result = new ArrayList<Ipv6Range>(2);
            if (start.compareTo(value.start) < 0) {
                result.add(new Ipv6Range((Ipv6Address) start, ((Ipv6Address) value.start).predecessor()));
            }
            if (value.end.compareTo(end) < 0) {
                result.add(new Ipv6Range(((Ipv6Address) value.end).successor(), (Ipv6Address) end));
            }
            return result;
        }
    }
    

    public static Ipv6Range parse(String string) {
        if (string == null) {
            return null;
        }

        if (string.indexOf(',') >= 0) {
            int slashIdx = string.indexOf('/');
            String startAddress = string.substring(0, slashIdx);

            Ipv6Address start = Ipv6Address.parse(startAddress);
            Ipv6Address end = Ipv6Address.parse(startAddress);

            String sizes = string.substring(slashIdx);
            for (String sizeStr : sizes.split(",")) {
                int size = Integer.parseInt(sizeStr.substring(1));
                end = end.upperBoundForPrefix(size).successor();
            }

            return new Ipv6Range(start, end.predecessor());
        }

        int idx = string.indexOf('/');
        if (idx >= 0) {
            Ipv6Address prefix = Ipv6Address.parse(string.substring(0, idx));
            int length = Integer.parseInt(string.substring(idx + 1));
            return Ipv6Range.prefix(prefix, length);
        }

        idx = string.indexOf('-');
        if (idx >= 0) {
            Ipv6Address start = Ipv6Address.parse(string.substring(0, idx));
            Ipv6Address end = Ipv6Address.parse(string.substring(idx + 1));
            return new Ipv6Range(start, end);
        }

        throw new IllegalArgumentException("Illegal IPv4 range: " + string);
    }
    
    public static Ipv6Range parseWithNetmask(String ipStr, String netmaskStr) {
        Ipv6Address start = Ipv6Address.parse(ipStr);
        Ipv6Address netmask = Ipv6Address.parse(netmaskStr);
        if (!netmask.isValidNetmask()) {
            throw new IllegalArgumentException("netmask '" + netmaskStr + "' is not a valid netmask");
        }

        int size = netmask.value.bitCount();
        Ipv6Address end = start.upperBoundForPrefix(size);

        return new Ipv6Range(start, end);
    }

    @Override
    public boolean isValidNetmask() {
        if (!isUnique()) {
            return false;
        }
        return ((Ipv6Address) start).isValidNetmask();
    }
    
    
    public List<Ipv6Range> splitToPrefixes() {
        BigInteger rangeEnd = end.value;
        BigInteger currentRangeStart = start.value;
        int startingPrefixLength = getBitSize();
        List<Ipv6Range> prefixes = new LinkedList<Ipv6Range>();

        while (currentRangeStart.compareTo(rangeEnd) <= 0) {
            int maximumPrefixLength = getMaximumLengthOfPrefixStartingAtIpAddressValue(currentRangeStart, startingPrefixLength);
            BigInteger maximumSizeOfPrefix = rangeEnd.subtract(currentRangeStart).add(BigInteger.ONE);
            BigInteger currentSizeOfPrefix = BigInteger.valueOf(2).pow(maximumPrefixLength);

            while ((currentSizeOfPrefix.compareTo(maximumSizeOfPrefix) > 0) && (maximumPrefixLength > 0)) {
                maximumPrefixLength--;

                currentSizeOfPrefix = BigInteger.valueOf(2).pow(maximumPrefixLength);
            }
            BigInteger currentRangeEnd = currentRangeStart
                    .add(BigInteger.valueOf(2).pow(maximumPrefixLength).subtract(BigInteger.ONE));

            Ipv6Range prefix = new Ipv6Range(new Ipv6Address(currentRangeStart), new Ipv6Address(currentRangeEnd));

            prefixes.add(prefix);

            currentRangeStart = currentRangeEnd.add(BigInteger.ONE);
        }

        return prefixes;
    }
    
    private static int getMaximumLengthOfPrefixStartingAtIpAddressValue(BigInteger ipAddressValue, int startingPrefixLength) {
        int prefixLength = startingPrefixLength;

        while ((prefixLength >= 0) && !canBeDividedByThePowerOfTwo(ipAddressValue, prefixLength)) {
            prefixLength--;
        }

        return prefixLength;
    }
    
    private static boolean canBeDividedByThePowerOfTwo(BigInteger number, int power) {
        return number.remainder(BigInteger.valueOf(2).pow(power)).equals(BigInteger.ZERO);
    }

    @Override
    protected int getBitSize() {
        return 128;
    }
    
    @Override
    public int compareTo(ResourceRange o) {
        if (o instanceof Ipv6Range) {
            int startCompareResult = start.compareTo(o.start);
            if (startCompareResult == 0) {
                return -(end.compareTo(o.end));
            }
            return startCompareResult;
        }
        return 1;
    }
    
    @Override
    public String toString() {
        return toString(false);
    }
    
    public boolean isLegalPrefix() {
        int n = getPrefixLength();
        return start.equals(((Ipv6Address) start).lowerBoundForPrefix(n)) && end.equals(((Ipv6Address) end).upperBoundForPrefix(n));
    }

    public int getPrefixLength() {
        return ((Ipv6Address) start).getCommonPrefixLength(((Ipv6Address) end));
    }

    public String toString(boolean defaultMissingOctets) {
        if (isLegalPrefix()) {
            return ((IpAddress) start).toString(defaultMissingOctets) + "/" + getPrefixLength();
        } else {
            return super.toString();
        }
    }
}
