//Dereck Sanchez
//eid: das2735
//Sam Wolfshohl
//eid: smw985
import java.util.*;
public class LZcoding {
	
	public static void main(String[] args) throws Exception{
		if(args[0].equals("c")){
			compress(args[1]);
		}
		else if(args[0].equals("d")){
			decompress(args[1]);
		}
	}
	
	public static void compress(String fileName) throws Exception {
		IO.Compressor compressor = new IO.Compressor(fileName);
		char[] chars = compressor.getCharacters();
		LinkedList<IO.Pair> transmission = new LinkedList<IO.Pair>();
		LinkedList<String> words = new LinkedList<String>();
		words.add("");
		transmission.add(new IO.Pair(0,'\0'));
		compressor.encode(0,'\0');
		

		int index = 0;
		while(index < chars.length-3){
			String check = chars[index] + "";
			boolean flag = true;
			int last = 0;
			while(flag){
				if(words.contains(check)){
					last = words.indexOf(check);
					index++;
					check = check + chars[index];
				}
				else{
					words.add(check);
					transmission.add(new IO.Pair(last,chars[index]));
					compressor.encode(last,chars[index]);
					last = 0;
					flag = false;
					index++;
				}
				
			}
			
		}
		
		compressor.finalize();
	}
	
	public static void decompress(String fileName)throws Exception{
		IO.Decompressor io = new IO.Decompressor(fileName);
		boolean flag = true;
		LinkedList<IO.Pair> dictionary = new LinkedList<IO.Pair>();
		String result = "";
		
		while(flag){
			IO.Pair next = io.decode();
			dictionary.add(next);
			flag = next.isValid();
		}
		while(!dictionary.isEmpty()){
			IO.Pair last = dictionary.getLast();
			result = result + last.getCharacter();
			while(last.getIndex() != 0 && last.getCharacter() != '\0'){
				last = dictionary.get(last.getIndex());
				result = result + last.getCharacter();
			}
		
		dictionary.removeLast();
			
		}
		for(int i=result.length()-1; i > 0; i--){
			io.append(result.charAt(i-1)+"");
		}
		io.finalize();
	}

}
