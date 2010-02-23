package net.ripe.ipresource;

import java.math.BigInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Example: AS1-AS20
 */
public class IpResourceRange extends IpResource {

	private static final long serialVersionUID = 1L;

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
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("range must not be empty: " + start + "-" + end);
        }

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
        if(s.indexOf(',') >= 0) {
        	int slashIdx = s.indexOf('/');
        	String startAddress = s.substring(0, slashIdx);
        	
        	UniqueIpResource start = UniqueIpResource.parse(startAddress);
        	UniqueIpResource end = UniqueIpResource.parse(startAddress);
        	
        	String sizes = s.substring(slashIdx);
        	for(String sizeStr: sizes.split(",")) {
        		int size = Integer.parseInt(sizeStr.substring(1));
        		end = end.upperBoundForPrefix(size).successor();
        	}
        	
            return IpResourceRange.range(start, end.predecessor());
        }

        int idx = s.indexOf('/');
        if (idx >= 0) {
            IpAddress prefix = IpAddress.parse(s.substring(0, idx), true);
            int length = Integer.parseInt(s.substring(idx + 1));
            return IpRange.prefix(prefix, length);
        }

        idx = s.indexOf('-');
        if (idx >= 0) {
            UniqueIpResource start = UniqueIpResource.parse(s.substring(0, idx));
            UniqueIpResource end = UniqueIpResource.parse(s.substring(idx + 1));
            if (start.getType() != end.getType()) {
                throw new IllegalArgumentException("resource types in range do not match");
            }
            return IpResourceRange.range(start, end);
        }

        throw new IllegalArgumentException("illegal resource range: " + s);
    }
    
    public static IpResourceRange assemble(BigInteger start, BigInteger end, IpResourceType type) {
        if (type.equals(IpResourceType.ASN)) {
            return IpResourceRange.range(new Asn(start), new Asn(end));
        } else if (type.equals(IpResourceType.IPv4)) {
            return IpResourceRange.range(new Ipv4Address(start), new Ipv4Address(end));
        } else if (type.equals(IpResourceType.IPv6)) {
            return IpResourceRange.range(new Ipv6Address(start), new Ipv6Address(end));
        } else {
            throw new IllegalStateException("Unknown resource type: "+ type);
        }
    }

	public static IpResourceRange parseWithNetmask(String ipStr, String netmaskStr) {
		UniqueIpResource start = UniqueIpResource.parse(ipStr);
		UniqueIpResource netmask = UniqueIpResource.parse(netmaskStr);
		
		int size = netmask.value.bitCount();
        UniqueIpResource end = start.upperBoundForPrefix(size);

        return IpResourceRange.range(start, end);
	}
}
