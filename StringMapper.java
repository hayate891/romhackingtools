import java.io.*;
import java.util.Arrays;

public class StringMapper {

	public static void printUsage() {
		System.out.println("This program is designed for mapping strings to numeric");
		System.out.println("values for quick conversion.");
		System.out.println("");
		System.out.println("Usage: ");
		System.out.println("   StringMapper [mapfile] [string]");
		System.out.println("   StringMapper [mapfile] [numeric-array]");
		System.out.println("");
		System.out.println("If the second argument is a string, it will be converted");
		System.out.println("to a numeric array. If it is a numeric array, it will be");
		System.out.println("converted to a string.");
		System.out.println("");
		System.out.println("Example: ");
		System.out.println("   StringMapper custom.map John");
		System.out.println("   StringMapper custom.map 1B 2F 3E");
		System.out.println("   StringMapper custom.map 12 66 443");
		System.out.println("");
		System.out.println("The map file should be formatted like such:");
		System.out.println("   NUMERIC_VALUE1 = STRING1");
		System.out.println("   NUMERIC_VALUE2 = STRING2");
		System.out.println("   NUMERIC_VALUE3 = STRING3");
		System.out.println("");
		System.out.println("Example:");
		System.out.println("  1B=C");
		System.out.println("  2F=a");
		System.out.println("  3E=t");
		System.out.println("");
		System.out.println("Note that the spaces around the equal sign are REQUIRED.");
		System.out.println("The numeric values can be of any base.");
		System.out.println("There is no limit to the length of the map file.");
	}

	public static void swap(String[] array, int pos1, int pos2) {
		String tmp = array[pos1];
		array[pos1] = array[pos2];
		array[pos2] = tmp;
	}
	public static void sortByLength(String[] array, String[] tagon) {
		int wall = 0;
		int largest = 0;
		
		while (wall < array.length) {
			for (int i=wall; i<array.length; i++) 
				if (array[i].length() > array[largest].length())
					largest = i;

			swap(array,largest,wall);
			swap(tagon,largest,wall);
			wall++;
		}
		
	}

	public static void main(String[] args) {
		
		String[] hexes = new String[0];
		String[] chars = new String[0];

		if (args.length < 2) {
			printUsage();
			return;
		}

		String fname = args[0];
		
		File myFile = new File(args[0]);

		try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.split(" = ").length == 1) 
					continue;
				hexes = Arrays.copyOf(hexes,hexes.length+1);
				hexes[hexes.length-1] = line.split(" = ")[0];
				chars = Arrays.copyOf(chars,chars.length+1);
				chars[chars.length-1] = line.split(" = ")[1];
			}
			sortByLength(chars,hexes);

			if (args.length == 2) {
				String out = "";
				for (int i=0; i<args[1].length(); i++) {
					for (int j=0; j<chars.length; j++) {

						if (i+chars[j].length() > args[1].length())
							continue;

						String cur = args[1].substring(i,i+chars[j].length());

						if (chars[j].equals(cur)) {
							out += hexes[j] + " ";
							i += chars[j].length()-1;
							break;
						}
					}
				}
				System.out.println(out);
			} else {
				String out = "";
				for (int i=1; i<args.length; i++) {
					for (int j=0; j<hexes.length; j++) {
						if (hexes[j].equals(args[i])) {
							out += chars[j];
							break;
						}
					}
				}
				System.out.println(out);
			}
 		} catch (IOException e) {
			printUsage();
		}
	}
}
