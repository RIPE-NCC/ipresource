package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * This class has parts copied over from Ipv4Address for quick use by other team members. Stuff like
 * the ADDRESS_SIZE and the overrides need some pulling up and refactoring.
 * 
 * @author beaumose
 *
 */
public class Ipv6Address extends IpAddress {

	public static final int ADDRESS_SIZE = IpResourceType.IPv6.getBitSize();
    private static final BigInteger ADDRESS_MASK = bitMask(0);

    private static BigInteger bitMask(int prefixLength) {
        return BigInteger.valueOf((1L << (ADDRESS_SIZE - prefixLength)) - 1);
    }
    
	/**
	 * Mask for 16 bits, which is the length of one part of an IPv6 address.
	 */
	private BigInteger PART_MASK = BigInteger.valueOf(65535);
	
	protected Ipv6Address(IpResourceType type, BigInteger value) {
		super(type, value);
	}

    public Ipv6Address(BigInteger value) {
		super(IpResourceType.IPv6, value);
	}

	public static Ipv6Address parse(String s) {
        Validate.isTrue(Pattern.matches("[0-9a-fA-F]{0,4}\\:([0-9a-fA-F]{0,4}\\:){1,6}[0-9a-fA-F]{0,4}", s), "Invalid IPv6 address: " + s);
        
        // Count number of colons: must be between 2 and 7
        Pattern colonPattern = Pattern.compile(":");
        Matcher colonMatcher = colonPattern.matcher(s);
        int colonCount = 0;
        while (colonMatcher.find()) { colonCount++ ; };
        
        // Count number of double colons: should be either 0 (with 7 colons) or 1 (with less)
        Pattern doubleColonPattern = Pattern.compile("::");
        Matcher doubleColonMatcher = doubleColonPattern.matcher(s);
        int doubleColonCount = 0;
        while (doubleColonMatcher.find()) { doubleColonCount++ ; };
      
        // The number of double colons must be exactly one if there's a missing colon.
        // The double colon will be the place that gets filled out to complete the address for easy parsing.
        if (colonCount < 7) {
        	Validate.isTrue(doubleColonCount == 1, "May only be one double colon in an IPv6 address");

        	// Add extra colons
        	String filledDoubleColons = ":::::::".substring(0, 7 - colonCount + 2);
        	s = s.replace("::", filledDoubleColons);
        }
        
        // By now we have an IPv6 address that's guaranteed to have 7 colons.
        
        Pattern p = Pattern.compile("([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4})");
        Matcher m = p.matcher(s);
        m.find();
        
        String ipv6Number = "";
        for (int i = 1; i <= m.groupCount(); i++) {
        	String part = m.group(i);
        	String padding = "0000".substring(0, 4 - part.length());
			ipv6Number = ipv6Number + padding + part;
		}

        return new Ipv6Address(IpResourceType.IPv6, new BigInteger(ipv6Number, 16));
    }	
	
	@Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        BigInteger temp = this.getValue().xor(other.getValue());
        return ADDRESS_SIZE - temp.bitLength();
    }

	@Override
    public Ipv6Address lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = ADDRESS_MASK.xor(bitMask(prefixLength));
        return new Ipv6Address(this.getValue().and(mask));
    }

	@Override
    public Ipv6Address upperBoundForPrefix(int prefixLength) {
        return new Ipv6Address(this.getValue().or(bitMask(prefixLength)));
    }

    @Override
    public String toString() {
    	String[] parts = new String[8];
    	
    	for (int i = 0; i < parts.length; i++) {
    		BigInteger part = getValue().shiftRight(i*16).and(PART_MASK);
    		if (BigInteger.ZERO.equals(part)) {
    			parts[i] = "";
    		} else {
    			parts[i] = part.toString(16);	
    		}
		}

    	String result = String.format("%s:%s:%s:%s:%s:%s:%s:%s",
    			parts[7],
    			parts[6],
    			parts[5],
    			parts[4],
    			parts[3],
    			parts[2],
    			parts[1],
    			parts[0]);
    	
    	result = result.replaceAll(":{3,7}", "::");
    	
    	return result;
    }		
}
