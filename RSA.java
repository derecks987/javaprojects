//Dereck Sanchez DAS2735
//Muhammad Ahmed Sadiq mas4769

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class RSA {


	public static void main(String[] args) throws Exception{
		if(args[0].equals("key")){
			int a = Integer.parseInt(args[1]);
			int b = Integer.parseInt(args[2]);
			generateKey(a,b);
		}
		else if(args[0].equals("encrypt")){
			encrypt(args[1],args[2],args[3]);
		}
		else if(args[0].equals("decrypt")){
			decrypt(args[1],args[2],args[3]);
		}
	}

	public static void generateKey(long p, long q) {
		long n = p*q;
		long phi = (p-1)*(q-1);
		Random r = new Random();
		// compute a random prime e where 1 < e < 256
		long e = BigInteger.probablePrime(r.nextInt(7)+2, r).longValue();
		long d = 0;
		// check if e is relatively prime to phi(n)
		for (long i = e; i > 1; i--) {
			if ((e%i == 0) && (phi%i == 0)){
				e = BigInteger.probablePrime(8, new Random()).longValue();
				i = e;
			}
		}
		d = gcd(phi,e);
		// make sure 1 <= d < n
		while(d>=n)
			d = d - phi;
		while(d < 0)
			d = d + phi;
			
		System.out.println(n+" "+e+" "+d);
	}
	// compute the GCD of two #
	public static long gcd(long phi, long e){
		long a = 1,b = 0,c = 0,d = 1,temp = 0,q = 0, u = phi, v = e;
		
		while(v != 1){
			q = u/v;
			temp = c; c = a-q*c; a=temp;
			temp = d; d = b-q*d; b=temp;
			temp = v; v = u-q*v; u=temp;
		}
		
		return (long)d;
	}
	
	// concat 3 bytes to long =  (((data[0] & 0xff) << 16)| ((data[1] & 0xff) <<  8) | ((data[2] & 0xff)))
	public static void encrypt(String inFile,String keyFile,String outFile) throws Exception {
		//create a file here to use the length() function on it later
		File f = new File(inFile);
		FileInputStream inStream =  new FileInputStream(f);
		FileOutputStream outStream =  new FileOutputStream(outFile);
		BufferedReader keyData = new BufferedReader(new FileReader(keyFile));
		// byte array to store bytes read from infile
		byte[] data = new byte[(int)f.length()];
		inStream.read(data);
		//checks for loop variables to see if  file doesnt end with 3 bytes evenly
		int check = data.length%3;
		int stop = data.length - check;
		long bits = 0;
		// read key data from keyfile
		String s = keyData.readLine();
		String[] sp = s.split(" ");
		long n = Long.valueOf(sp[0]);
		long e = Long.valueOf(sp[1]);
		long d = Long.valueOf(sp[2]);
		int i = 0;
		
		
		//apply rsa key and write out to file
		for(i=0; i < stop; i = i+3){
			// grabs 3 bytes and concats them into 1 long (0 d1 d2 d3)
			bits = (((data[i] & 0xff) << 16)| ((data[i+1] & 0xff) <<  8) | ((data[i+2] & 0xff)));
			// applys key to long
			bits = modExp(bits,e,n);
			
			//writes modded long out to file broken into 4 bits from left to right
			outStream.write((byte)((bits & 0xff000000) >>> 24));
			outStream.write((byte)((bits & 0xff0000) >>> 16));
			outStream.write((byte)((bits & 0xff00) >>> 8));
			outStream.write((byte)(bits & 0xff));
			
			
		}
		// special cases if file ends with just 2 or 1 bytes instead of 3
		if(check == 2){
			// grabs 2 bytes and concats them into 1 long (0 d1 d2 0)
			bits = (((data[i] & 0xff) << 16)| ((data[i+1] & 0xff) <<  8));
			// applys key to long
			bits = modExp(bits,e,n);
			
			//writes modded long out to file broken into 4 bits from left to right
			outStream.write((byte)((bits & 0xff000000) >>> 24));
			outStream.write((byte)((bits & 0xff0000) >>> 16));
			outStream.write((byte)((bits & 0xff00) >>> 8));
			outStream.write((byte)(bits & 0xff));
		}
		else if(check == 1){
			// grabs 1 byte and concats them into 1 long (0 d1 0 0)
			bits = (((data[i] & 0xff) << 16));
			// applys key to long
			bits = modExp(bits,e,n);
			
			//writes modded long out to file broken into 4 bits from left to right
			outStream.write((byte)((bits & 0xff000000) >>> 24));
			outStream.write((byte)((bits & 0xff0000) >>> 16));
			outStream.write((byte)((bits & 0xff00) >>> 8));
			outStream.write((byte)(bits & 0xff));
		}
		//close all files used
		inStream.close();
		keyData.close();
		outStream.close();
		
		
	}
	
	public static void decrypt(String inFile,String keyFile,String outFile_) throws Exception {
		
		try
				{
					BufferedReader keyData = new BufferedReader(new FileReader(keyFile));
					// read key data from keyfile
					String s = keyData.readLine();
					String[] sp = s.split(" ");
					long n = Long.valueOf(sp[0]);
					long e = Long.valueOf(sp[1]);
					long d = Long.valueOf(sp[2]);
					
					int blocksize;
					int writeblocks;
					long x;
					String outfilePath = "";
					
					
						blocksize = 4;
						writeblocks = 4;
						x = d; // use private key to decrypt
						outfilePath = "decrypted";
					
					File f = new File(inFile);
					long filesize = f.length();
					FileInputStream finf = new FileInputStream(inFile); 
					DataInputStream Mbig = new DataInputStream(finf); // input file
					
					FileOutputStream outfile = new FileOutputStream(outFile_);
					DataOutputStream Mout = new DataOutputStream(outfile); // output file
					
					while (Mbig.available() > 0) // infile still has unread bytes
					{
						byte[] bytes = new byte[blocksize];
						for(int i = 0; i < blocksize; i++) // reads next 3 or 4 bytes
						{
							try
							{
								bytes[i] = Mbig.readByte();
							}
							catch (EOFException ee)
							{
								bytes[i] = 0;
							}
						}
						long M = bytesToBlock(bytes); // concatenates bytes into one number (i.e. "block")
						long Mp = modExp(M,x,n); // computes M^x mod n
						byte[] outs = new byte[4];
						outs = blockToBytes(Mp); // breaks up new message into separate bytes to write to file
						if(filesize != 4){
							Mout.writeByte(outs[1]);
							Mout.writeByte(outs[2]);
							Mout.writeByte(outs[3]);
						}
						else{
							Mout.writeByte(outs[1]);
							if(outs[2] != 0)
								Mout.writeByte(outs[2]);
							if(outs[3] != 0)
								Mout.writeByte(outs[3]);
						}
						
						filesize = filesize -4;
					}
				}
				catch(FileNotFoundException fe)
				{
					System.out.println("FileNotFoundException: " + fe);
				}
				catch(IOException ioe)
				{
					System.out.println("IOException: " + ioe);
				}	
	}

	public static long bytesToBlock(byte[] bytes)
		{
			// concatenates an array of bytes by shifting each 
			// byte to the left a given amount such that the final string
			// can still be decomposed into the original bytes
			// by reading the string off in 8-bit segments, beginning
			// from the left
			
			int n = bytes.length; // number of input variables (bytes)
			long M = 0; // cumulative string of bytes
			int sh = 0; // amount to shift byte
			long shifted = 0; // shifted byte
			
			for (int i = 0; i < n; i ++)
			{
				int tmp = bytes[i];
				if (tmp < 0) // to correct for signed bytes
				{
					tmp += 256;
				}
				sh = 8*(n-i-1); // number of shifts to do on this byte
				shifted = tmp << sh; // shifted part
				M = M + shifted; // cumulative sum of bytes
			}
			return M; // concatenation of 8-bit chunks
		}
		
		public static byte[] blockToBytes(long M){

		// read chunks of 4bytes from the long concatenated string M
			
			byte[] outs = new byte[4];
			long tmp = 0;
			long start = 255; // 11111111
		
			for (int i = 3; i >= 0; i--)
			{
				tmp = (M & start); // isolates 8 bits of M
				outs[i] = (byte)(tmp >> (8*(3-i))); // shifts those 8 bits into one byte
				start = start << 8; // increment start (bit selector)
			}
			
			return outs;
		}
		
	// calculates modular exponentiation (M^e mod n)
	public static long modExp(long m, long e , long n){
		long c = 1;
		while(e!=0){
			if(e%2 != 0)
				c = c*m%n;
			e = e/2;
			m=m*m%n;
		}
		return c;
	}
}
