//Dereck Sanchez
//eid: das2735
//Sam Wolfshohl
//eid: smw985

import java.util.*;
public class LowerBound{

private static ArrayList<Double> solve(char[] input) {
	ArrayList<Double> result = new ArrayList<Double>();
	
	char[] items = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'X', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			' ', ','};
	
	double countLetter = 0.0;
	int x = 0;
	while(x < items.length){
		for(int y = 0; y < input.length; y++){
			if(input[y] == items[x]){
				countLetter++;
			}
		}
		result.add((double)(countLetter/(input.length-2))); 
		countLetter = 0;
		x++;
	}
	return result;
}

public static void main (String [] args) throws Exception{
	IO.Compressor c = new IO.Compressor(args[0]);
	char[] input = c.getCharacters();
	ArrayList <Double> prob = new ArrayList<Double>();
	
	prob = solve(input);   
	
	double entropy = 0.0;
	int i = 0;
	while(i < prob.size()){
		if(prob.get(i) > 0){
			entropy = entropy + (Math.log(prob.get(i))/Math.log(2))
					  * (-prob.get(i));		 
		}
		i++;
	}
	double lowerBound = (input.length-2) * entropy;
	
	System.out.printf("The lower bound is %.3f", lowerBound);
	System.out.printf("; the entropy is %.3f\n", entropy);
}

}	