import java.io.*;
import java.util.Arrays;

public class RelativeSearch {
	private static boolean[] wildCards;

	/*private static void addWildCard(int nc) {
		wildCards = Arrays.copyOf(wildCards,wildCards.length+1);
		wildCards[wildCards.length-1] = nc;
	}*/

	public static String decToHex(int dec) {
		int places = (int)Math.floor(Math.log(dec) / Math.log(16))+1;
		String hexString = "";
		for (int i=0; i<places; i++) {
			int place = dec % 16;
			dec = (dec - place) / 16;
			char newChar = place < 10 ? (char)(48+place) : (char)(55+place);
			hexString = newChar + hexString;
		}
		return hexString;
	}

	public static int[] relativeSearchSigned16(byte[] buffer, byte[] rel) {
		int relPos = 0;
		int[] ret = new int[0];

		for (int i=0; i<buffer.length-2; i+=2) {
			int change = (buffer[i+3]+buffer[i+2])-
					(buffer[i+1]+buffer[i]);
			change = handleWildcards(change,rel,relPos);
			if (change == rel[relPos]) 
				relPos++;
			else
				relPos = 0;

			if (relPos == rel.length) {
				ret = Arrays.copyOf(ret,ret.length+1);
				ret[ret.length-1] = i-(rel.length+1);
				relPos = 0;
			}
		}
		return ret;
	}

	public static int[] relativeSearchUnsigned16(byte[] buffer, byte[] rel) {
		int relPos = 0;
		int[] ret = new int[0];

		for (int i=0; i<buffer.length-2; i+=2) {
			int change = (unsign(buffer[i+3])+unsign(buffer[i+2]))-
					(unsign(buffer[i+1])+unsign(buffer[i]));
			change = handleWildcards(change,rel,relPos);
			if (change == rel[relPos]) 
				relPos++;
			else
				relPos = 0;

			if (relPos == rel.length) {
				ret = Arrays.copyOf(ret,ret.length+1);
				ret[ret.length-1] = i-(rel.length+1);
				relPos = 0;
			}
		}
		return ret;
	}

	public static int[] relativeSearchSigned8(byte[] buffer, byte[] rel) {
		int relPos = 0;
		int[] ret = new int[0];

		for (int i=0; i<buffer.length-1; i++) {
			int change = buffer[i+1]-buffer[i];
			change = handleWildcards(change,rel,relPos);
			if (change == rel[relPos]) 
				relPos++;
			else
				relPos = 0;

			if (relPos == rel.length) {
				ret = Arrays.copyOf(ret,ret.length+1);
				ret[ret.length-1] = i-rel.length+1;
				relPos = 0;
			}
		}
		return ret;
	}


	public static int[] relativeSearchUnsigned8(byte[] buffer, byte[] rel) {
		int relPos = 0;
		int[] ret = new int[0];

		for (int i=0; i<buffer.length-1; i++) {
			int change = unsign(buffer[i+1])-unsign(buffer[i]);
			change = handleWildcards(change,rel,relPos);

			if (change == rel[relPos]) 
				relPos++;
			else
				relPos = 0;

			if (relPos == rel.length) {
				ret = Arrays.copyOf(ret,ret.length+1);
				ret[ret.length-1] = i-rel.length+1;
				relPos = 0;
			}
		}
		return ret;
	}

	private static int handleWildcards(int change, byte[] rel, int relPos) {

		if (!wildCards[relPos]) return change;
		else if (rel[relPos] == 1) return 1;

		else if (rel[relPos] == 2 && change > 0) return 2;
		else if (rel[relPos] == 2 && change <= 0) return -1;

		else if (rel[relPos] == 3 && change < 0) return 3;
		else if (rel[relPos] == 3 && change >= 0) return -1;

		else if (rel[relPos] == 4 && change != 0) return 4;
		else if (rel[relPos] == 4 && change == 0) return -1;

		return change;	
	}

	public static int unsign(int val) {
		byte a = (byte)val;
		int b = a & 0xFF; 
		return b;
	}
	public static void printUsage() {
		System.out.println("This program searches binary files for a relative");
		System.out.println("sequence bytes.");
		System.out.println("");
		System.out.println("Usage: ");
		System.out.println("   RelativeSearch [filename] [bits] [signed] [relative sequence]");
		System.out.println("");
		System.out.println("Example: ");
		System.out.println("   RelativeSearch file.img 8 true 4 9 -2 7");
		System.out.println("   RelativeSearch file.img 16 false JOHN");
		System.out.println("");
		System.out.println("If the relative sequence is numeric, wildcards");
		System.out.println("may be used. Allowed wildcards are:");
		System.out.println("   ?    Any 8 bit or 16 bit value.");
		System.out.println("   +    An increasing value.");
		System.out.println("   -    A decreasing value.");
		System.out.println("   x    A change in value.");
		System.out.println("");
		System.out.println("   The ? may be used in a character relative sequence.");
		System.out.println("");
		System.out.println("This will return the location in which this");
		System.out.println("relative sequence first occurs and the bytes that");
		System.out.println("make up the sequence.");
	}

	public static void main(String[] args) {

		if (args.length < 4) {
			printUsage();
			return;
		}
		
		byte[] rel = new byte[0];

		try {
			wildCards = new boolean[args.length-3];
			rel = new byte[args.length-3];
			for (int i=3; i<args.length; i++) {
				if (args[i].equals("?")) {
					wildCards[i-3] = true;
					rel[i-3] = (byte)1;
				} else if (args[i].equals("+")) {
					wildCards[i-3] = true;
					rel[i-3] = (byte)2;
				} else if (args[i].equals("-")) {
					wildCards[i-3] = true;
					rel[i-3] = (byte)3;
				} else if (args[i].equals("x")) {
					wildCards[i-3] = true;
					rel[i-3] = (byte)4;
				} else {
					rel[i-3] = (byte)Integer.parseInt(args[i]);
				}
			}
		} catch (Exception e) {
			wildCards = new boolean[args[3].length()-1];
			rel = new byte[args[3].length()-1];
			for (int i=0; i<args[3].length()-1; i++) {
				int diff = args[3].charAt(i+1)-args[3].charAt(i);
				rel[i] = (byte)diff;

				if (args[3].charAt(i+1) == '?' || args[3].charAt(i) == '?') {
					wildCards[i] = true;
					rel[i] = 1;
				}/* else if (args[3].charAt(i+1) == '+' || args[3].charAt(i) == '+') {
					wildCards[i] = true;
					rel[i] = 2;
				} else if (args[3].charAt(i+1) == '-' || args[3].charAt(i) == '-') {
					wildCards[i] = true;
					rel[i] = 3;
				} else if (args[3].charAt(i+1) == '%' || args[3].charAt(i) == '%') {
					wildCards[i] = true;
					rel[i] = 4;
				}*/

			}
		}

		String fname = args[0];
		
		File myFile = new File(args[0]);
		byte[] buffer = new byte[(int)myFile.length()];

		try { 
			FileInputStream ifstream = new FileInputStream(args[0]);
			int read = 0;
			try { 
				while ((read = ifstream.read(buffer)) != -1) {}
				ifstream.close();
			} catch (IOException e) {}

		} catch (FileNotFoundException e) {
			System.out.println("The specified file was not found.");
			return;
		}

		int[] rets = new int[0];
		if (args[1].equals("8") && args[2].toLowerCase().equals("true"))
			rets = relativeSearchSigned8(buffer,rel);
		else if (args[1].equals("8") && args[2].toLowerCase().equals("false"))
			rets = relativeSearchUnsigned8(buffer,rel);
		else if (args[1].equals("16") && args[2].toLowerCase().equals("true"))
			rets = relativeSearchSigned16(buffer,rel);
		else if (args[1].equals("16") && args[2].toLowerCase().equals("false"))
			rets = relativeSearchUnsigned16(buffer,rel);
		else {
			printUsage();
			return;
		}

		if (rets.length != 0) {
			for (int i=0; i<rets.length; i++) {

				String hexPos = decToHex(rets[i]);
				while (hexPos.length() < 8)
					hexPos = "0"+hexPos;
	
				System.out.print(hexPos + ": ");
				if (args[1].equals("16")) {
					for (int j=0; j<2*(rel.length+1); j++) {
						String hexVal = decToHex(unsign(buffer[rets[i]+j]));
						while (hexVal.length() < 2)
							hexVal = "0"+hexVal;
						System.out.print(hexVal+" ");
					}
				} else {
					for (int j=0; j<rel.length+1; j++) {
						String hexVal = decToHex(unsign(buffer[rets[i]+j]));
						while (hexVal.length() < 2)
							hexVal = "0"+hexVal;
						System.out.print(hexVal+" ");
					}
				}
				System.out.println();	
			}
		} else {
			System.out.println("Search returned nothing.");
		}

	}
}
