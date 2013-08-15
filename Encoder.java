import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


public class Encoder {

	static int[] numList = new int[26];
	static int size = 0;
	static int total = 0;
	static int bitsPerSymbol = 0;
	
	//for huffman code
	static char[] alpha = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	static String huffStr = "";
	static int[] charFreqs = new int[256];
	static Map<String,String> huffCodes = new HashMap<String,String>(256);
	
	public static void main(String args[]) throws IOException{
		
		getDataFromFile(args[0]);
		double entropy = calcEntropy();
		buildHuffString();
		
		System.out.println("Language = " + huffStr);
		System.out.println("Entropy = " + entropy);
		
		// build tree
        HuffmanTree tree = HuffmanCode.buildTree(charFreqs);
 
        // print out results
        System.out.println("SYMBOL\tWEIGHT\tHUFFMAN CODE");
        HuffmanCode.printCodes(tree, new StringBuffer(),huffCodes);
        
        System.out.println("Generating " + Integer.parseInt(args[1]) + " character text using given language/frequency");
        createText(Integer.parseInt(args[1]));
        System.out.println("Encoding text");
        encode("testText.txt");
        System.out.println("Decoding text");
        decode("testText.enc1");
        
        System.out.println("Average bits per symbol of encoded text is " + bitsPerSymbol);
        System.out.println("Computed entropy of language is  " + entropy);
        System.out.println("Percentage differnce =  %" + bitsPerSymbol/entropy * 100);
        
		
	}
	
	private static double calcEntropy() {
		
		double ent = 0;
		for(int x = 0; x < size; x++){
			double num = numList[x] * 1.0;
			double freq = num / total;
			ent += freq * (Math.log(freq)/Math.log(2));
		}
		return ent*-1;
	}
	
	private static void buildHuffString(){
		for(int x = 0; x < size; x++){
			for(int y = 0; y < numList[x]; y++){
				huffStr += alpha[x];
			}
		}
		// we will assume that all our characters will have
        // code less than 256, for simplicity
        // read each character and record the frequencies
        for (char c : huffStr.toCharArray())
            charFreqs[c]++;
	}
	
	private static void createText(int len) throws IOException{
		PrintWriter out = new PrintWriter(new FileWriter("testText.txt"));

		for(int x = 0 ; x < size; x++ ){
			for(int y = 0; y < numList[x]/(1.0*total) * len; y++)
				out.print(alpha[x]);
		}
		
		out.close();
	}

	private static void encode(String file) throws IOException{
		// Open the file that is the first command line parameter
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		PrintWriter out = new PrintWriter(new FileWriter("testText.enc1"));
						 
		//Read File Line By Line
		String strVal;
		int totalSymbols = 0;
		int totalBits = 0;
		for(int x = 0; (strVal = br.readLine()) != null; x++){
			
			for(char c : strVal.toCharArray()){
				String val = huffCodes.get(String.valueOf(c));
				out.println(val);
				totalSymbols++;
				totalBits += val.length();
			}
		}
		
		bitsPerSymbol = totalBits/totalSymbols;
		out.close();
		br.close();
		fstream.close();
	}
	
	private static void decode(String file) throws IOException{
		// Open the file that is the first command line parameter
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		PrintWriter out = new PrintWriter(new FileWriter("testText.dec1"));
		
		//Read File Line By Line
		String strVal;
		while((strVal = br.readLine()) != null){
			out.print(huffCodes.get(strVal));
		}
				
		out.close();
		br.close();
		fstream.close();
	}
	private static void getDataFromFile(String file) throws IOException{
		// Open the file that is the first command line parameter
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				 
		//Read File Line By Line
		String strLine;
		for(int x = 0; (strLine = br.readLine()) != null; x++){
			assert x < 26;
			int num = Integer.parseInt(strLine);
			numList[x] = num;
			total+= num;
			size++;
		}
		
		fstream.close();
		br.close();
	}
}
