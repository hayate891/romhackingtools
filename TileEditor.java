import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class TileEditor {

	static char[] charset = new char[4];

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

	public static int unsign(int val) {
		byte a = (byte)val;
		int b = a & 0xFF; 
		return b;
	}

	public static void printUsage() {
		System.out.println("This program is designed for exporting and importing tile");
		System.out.println("graphics from Nintendo Entertainment System ROM files.");
		System.out.println("");
		System.out.println("Usage: ");
		System.out.println("   TileEditor [filename] [-e] [tile-id]");
		System.out.println("   TileEditor [filename] [-i] [tile-id] [tile-data]");
		System.out.println("   TileEditor [filename] [-d] [columns]");
		System.out.println("");
		System.out.println("Example: ");
		System.out.println("   TileEditor mario.nes -e 2104");
		System.out.println("   TileEditor mario.nes -e 2104 > newtile.txt");
		System.out.println("   TileEditor mario.nes -i 2104 < newtile.txt");
		System.out.println("   TileEditor mario.nes -d 5");
		System.out.println("");
		System.out.println("The -e switch is used to export tiles through STDOUT.");
		System.out.println("The -i switch is used to import tiles through STDIN.");
		System.out.println("The -d switch dumps all tile data through STDOUT.");
	}

	public static int[] tileToTileData(int[] tile) {
		int[] tileData = new int[16];

		for (int i=0; i<tile.length; i+=8) {
			int val1 = 0;
			int val2 = 0;
			int val3 = 0;
			int val4 = 0;

			for (int j=0; j<4; j++) {
				int inc = (int)Math.pow(2,j);
				if (tile[i+(3-j)] == 1 || tile[i+(3-j)] == 3)
					val1 += inc;
				if (tile[i+(3-j)+4] == 1 || tile[i+(3-j)+4] == 3)
					val2 += inc;	

				if (tile[i+(3-j)] == 2 || tile[i+(3-j)] == 3)
					val3 += inc;
				if (tile[i+(3-j)+4] == 2 || tile[i+(3-j)+4] == 3)
					val4 += inc;	
			}

			tileData[i/8] = (val1*16)+val2;
			tileData[i/8+8] = (val3*16)+val4;
		}
		return tileData;
	}

	public static void getHalfTile(int start,int[] halfTile1,byte[] buffer) {

		for (int i=start; i<start+8; i++) {
			int val = unsign(buffer[i]);
			int val1 = val % 16;
			int val2 = ((val-val1)/16)%16;

			int[] pixels = new int[8];
			for (int j=3; j>=0; j--) {
				int pos = (int)Math.pow(2,j);
				if (val1 >= pos) {
					pixels[(3-j)+4] = 1;
					val1 -= pos;
				}
				if (val2 >= pos) {
					pixels[3-j] = 1;
					val2 -= pos;
				}
			}
			for (int j=0; j<pixels.length; j++) 
				halfTile1[j+8*(i-start)] = pixels[j];
		}

	}

	public static int[] combineHalfTiles(int[] halfTile1, int[] halfTile2) {

		int[] outputTile = new int[Math.min(halfTile1.length,halfTile2.length)];
		for (int i=0; i<outputTile.length; i++) {
			if (halfTile1[i] == 1 && halfTile2[i] == 1) 
				outputTile[i] = 3;
			else if (halfTile1[i] == 1) 
				outputTile[i] = 1;
			else if (halfTile2[i] == 1) 
				outputTile[i] = 2;
		}
		return outputTile;
	}

	public static void printTile(int[] tile) {
		for (int i=0; i<tile.length; i++) {
			System.out.print(charset[tile[i]]);
			System.out.print(charset[tile[i]]);
			if ((i+1) % 8 == 0) System.out.println("");
		}
	}

	public static void outputTile(int tilenum,byte[] buffer) {
		int start = tilenum*16;//34704+16; 
		String hexstart = decToHex(start);
		//while (hexstart.length() < 8)
		//	hexstart = "0" + hexstart;
		//System.out.println(hexstart + ":");

		int[] halfTile1 = new int[64];
		int[] halfTile2 = new int[64];
		int[] tile = new int[64];
		getHalfTile(start,halfTile1,buffer);
		getHalfTile(start+8,halfTile2,buffer);
		tile = combineHalfTiles(halfTile1,halfTile2);

		printTile(tile);

	}

	public static void tileDump(int cols,byte[] buffer) {
		for (int i=0; i<buffer.length; i+=16+16*cols) {
			while (buffer.length - i < cols*16)
				cols--;

			String ob = createOutputBuffer(cols);
			for (int j=0; j<cols; j++) {
				/*String hexpos = decToHex(j*16+i);
				while (hexpos.length() < 8)
					hexpos = "0" + hexpos;*/

				String output = "#"+(i/16 + j)+":";
				while (output.length() < 16)
					output += " ";
				System.out.print(output);
				if (i != cols-1)
					System.out.print(" ");
			}
			System.out.println("");

			for (int j=0; j<cols; j++) {
				int[] halfTile1 = new int[64];
				int[] halfTile2 = new int[64];
				int[] tile = new int[64];
				getHalfTile(j*16+i,halfTile1,buffer);
				getHalfTile(j*16+i+8,halfTile2,buffer);
				tile = combineHalfTiles(halfTile1,halfTile2);
				
				ob = copyTileToOutputBuffer(ob,tile,j);
			}
			System.out.println(ob);
		}
	}

	public static String copyTileToOutputBuffer(String buff, int[] tile, int tilenum) {
		int x=0;
		int y=0;
		for (int i=0; i<tile.length; i++) {
			buff = editOutputBuffer(buff,tilenum,x,y,tile[i]);
			x++;
			if (x == 8) {
				x=0;
				y++;
			}
		}
		return buff;
	}

	public static String editOutputBuffer(String buff, int tilenum, int x, int y, int num) {
		int width = 0;
		for (width=0; width<buff.length(); width++)
			if (buff.charAt(width) == '\n')
				break;
		width++;
		
		int pos = x*2 + y*width + tilenum*17;
		String buffhalf1 = buff.substring(0,pos);
		String buffhalf2 = buff.substring(pos+2,buff.length());
		
		return buffhalf1 + charset[num] + charset[num] + buffhalf2;
	}

	public static String createOutputBuffer(int cols) {
		String buff = "";
		int height = 8;
		int spaces = cols-1;
		int width = cols*16 + spaces;
		int size = width * height + height;

		for (int i=0; i<size; i++) {
			if ((i+1) % (width+1) == 0)
				buff += "\n";	
			else if ((i+1) % 17 == 0)
				buff += " ";		
			else
				buff += "x";
		}
		return buff.substring(0,buff.length()-1);
	}

	public static int[] inputBufferToTile(String ib) {
		int[] outputTile = new int[64];
		int x = 0;
		int y = 0;
		int width = 17;
		for (int i=0; i<outputTile.length; i++) {
			int pos = x*2+y*width;
			int ch = ib.charAt(pos);
			if (ch == charset[0])
				outputTile[i] = 0;
			else if (ch == charset[1])
				outputTile[i] = 1;
			else if (ch == charset[2])
				outputTile[i] = 2;
			else
				outputTile[i] = 3;
			x++;
			if (x >= 8) {
				x=0;
				y++;
			}
		}
		return outputTile;
	}

	public static void importTile(int[] tile, int tilenum, byte[] buffer) {
		int start = tilenum*16;
		for (int i=start; i<start+16; i++) 
			buffer[i] = (byte)tile[i-start];
	}

	public static void writeFile(byte[] buffer, String fname) throws IOException {
		FileOutputStream ofstream = new FileOutputStream(fname);
		ofstream.write(buffer);
		ofstream.close();
	}

	public static void main(String[] args) {
	
		charset[0] = ' ';
		charset[1] = '▓';
		charset[2] = '░';
		charset[3] = '█';

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
		try {
			if (args[1].equals("-d")) 
				tileDump(Integer.parseInt(args[2]),buffer);
			else if (args[1].equals("-e"))
				outputTile(Integer.parseInt(args[2]),buffer);
			else if (args[1].equals("-i")) {
				int tileid = Integer.parseInt(args[2]);
				Scanner kb = new Scanner(System.in);
				String line = "x";
				String inputBuffer = "";
				for (int i=0; i<8; i++) {
					inputBuffer += kb.nextLine();
					if (i != 7)
						inputBuffer += "\n";
				}
				
				kb.close();
				int[] tile = inputBufferToTile(inputBuffer);
				int[] tileData = tileToTileData(tile);
				importTile(tileData,tileid,buffer);
				writeFile(buffer,args[0]);
			}
		} catch (Exception e) {
			printUsage();
		}
	}
}
