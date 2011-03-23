package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.Validate;

public class Ipv4Range extends IpRange {

    private static final long serialVersionUID = 1L;

    public Ipv4Range(Ipv4Address start, Ipv4Address end) {
        super(start, end);
    }

    private Ipv4Range(Ipv4Address networkNumber, int prefixLength) {
        this(networkNumber, networkNumber.upperBoundForPrefix(prefixLength));
    }

    public static Ipv4Range prefix(Ipv4Address networkNumber, int prefixLength) {
        Validate.notNull(networkNumber, "network number can't be null");
        Validate.isTrue(prefixLength >= 0);
        Validate.isTrue(prefixLength <= networkNumber.getBitSize());
        return new Ipv4Range(networkNumber, prefixLength);
    }

    public boolean isLegalPrefix() {
        int n = getPrefixLength();
        return start.equals(((Ipv4Address) start).lowerBoundForPrefix(n)) && end.equals(((Ipv4Address) end).upperBoundForPrefix(n));
    }

    public int getPrefixLength() {
        return ((Ipv4Address) start).getCommonPrefixLength(((Ipv4Address) end));
    }
    
    public static Ipv4Range parse(String string) {
        if (string == null) {
            return null;
        }

        if (string.indexOf(',') >= 0) {
            int slashIdx = string.indexOf('/');
            String startAddress = string.substring(0, slashIdx);

            Ipv4Address start = Ipv4Address.parse(startAddress);
            Ipv4Address end = Ipv4Address.parse(startAddress);

            String sizes = string.substring(slashIdx);
            for (String sizeStr : sizes.split(",")) {
                int size = Integer.parseInt(sizeStr.substring(1));
                end = end.upperBoundForPrefix(size).successor();
            }

            return new Ipv4Range(start, end.predecessor());
        }

        int idx = string.indexOf('/');
        if (idx >= 0) {
            Ipv4Address prefix = Ipv4Address.parse(string.substring(0, idx), true);
            int length = Integer.parseInt(string.substring(idx + 1));
            return Ipv4Range.prefix(prefix, length);
        }

        idx = string.indexOf('-');
        if (idx >= 0) {
            Ipv4Address start = Ipv4Address.parse(string.substring(0, idx));
            Ipv4Address end = Ipv4Address.parse(string.substring(idx + 1));
            return new Ipv4Range(start, end);
        }

        throw new IllegalArgumentException("Illegal IPv4 range: " + string);
    }

    public List<Ipv4Range> subtract(Ipv4Range value) {
        if (!this.overlaps(value)) {
            return Collections.singletonList(new Ipv4Range((Ipv4Address) start, (Ipv4Address) end));
        } else if (value.contains(this)) {
            return Collections.emptyList();
        } else {
            List<Ipv4Range> result = new ArrayList<Ipv4Range>(2);
            if (start.compareTo(value.start) < 0) {
                result.add(new Ipv4Range((Ipv4Address) start, ((Ipv4Address) value.start).predecessor()));
            }
            if (value.end.compareTo(end) < 0) {
                result.add(new Ipv4Range(((Ipv4Address) value.end).successor(), (Ipv4Address) end));
            }
            return result;
        }
    }
    
    public static Ipv4Range parseWithNetmask(String ipStr, String netmaskStr) {
        Ipv4Address start = Ipv4Address.parse(ipStr);
        Ipv4Address netmask = Ipv4Address.parse(netmaskStr);
        if (!netmask.isValidNetmask()) {
            throw new IllegalArgumentException("netmask '" + netmaskStr + "' is not a valid netmask");
        }

        int size = netmask.value.bitCount();
        Ipv4Address end = start.upperBoundForPrefix(size);

        return new Ipv4Range(start, end);
    }


    @Override
    public boolean isValidNetmask() {
        if (!isUnique()) {
            return false;
        }
        return ((Ipv4Address) start).isValidNetmask();
    }

    @Override
    protected int getBitSize() {
        return 32;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean defaultMissingOctets) {
        if (isLegalPrefix()) {
            return ((Ipv4Address) start).toString(defaultMissingOctets) + "/" + getPrefixLength();
        } else {
            return super.toString();
        }
    }
    
    public List<Ipv4Range> splitToPrefixes() {
        BigInteger rangeEnd = end.value;
        BigInteger currentRangeStart = start.value;
        int startingPrefixLength = getBitSize();
        List<Ipv4Range> prefixes = new LinkedList<Ipv4Range>();

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

            Ipv4Range prefix = new Ipv4Range(new Ipv4Address(currentRangeStart), new Ipv4Address(currentRangeEnd));

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
    public int compareTo(ResourceRange o) {
        if (o instanceof Ipv4Range) {
            int startCompareResult = start.compareTo(o.start);
            if (startCompareResult == 0) {
                return -(end.compareTo(o.end));
            }
            return startCompareResult;
        }
        if (o instanceof AsnRange) {
            return 1;
        }
        return -1;
    }

}
