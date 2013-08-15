import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.*;


public class FSM {
	
	private final static HashMap<String,String> shortcuts = new HashMap<String,String>();
	static
	{
		shortcuts.put("|u", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		shortcuts.put("|l", "abcdefghijklmnopqrstuvwxyz");
		shortcuts.put("|a", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
		shortcuts.put("|d", "0123456789");
		shortcuts.put("|n", "123456789");
		shortcuts.put("|s", ".,~!@$#%^&+-{}");
	}
	private final static HashMap<String,String> shortcuts_trans = new HashMap<String,String>();
	static
	{
		shortcuts_trans.put("|u", "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z");
		shortcuts_trans.put("|l", "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z");
		shortcuts_trans.put("|a", "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z");
		shortcuts_trans.put("|d", "0,1,2,3,4,5,6,7,8,9");
		shortcuts_trans.put("|n", "1,2,3,4,5,6,7,8,9");
		shortcuts_trans.put("|s", ".,,,~,!,@,$,#,%,^,&,+,-,{,}");
	}
	private static String input_alph = "";
	private static String output_alph = "";
	private static String machine_name = "";
	private static String machine_type = "";
	private static String starting_state = "";
	private static ArrayList<String> states = new ArrayList<String>();
	private static LinkedList<LinkedList<String>> transitions = new LinkedList<LinkedList<String>>();
	private static ArrayList<State> stateList = new ArrayList<State>();
	private static FSM fsminst = new FSM();
	
	private class State{
		private boolean isMoore;//false for Moore machines, true for Mealy
		private boolean isAccepting=false;
		private boolean isFinal=false;
		private String name;
		private List<String> transitionsS;
		private State(boolean type, String stateName ,List<String> trans){
			isMoore = type;
			name=stateName;
			transitionsS = trans;
			if(isMoore){
				if(name.contains("$")){
					isAccepting=true;
					name=name.replace("$", "");
				}
				if(name.contains("!")){
					isFinal=true;
					name=name.replace("!", "");
				}
			}
		}
		
		public String getName(){return name;}
		
		public String toString(){
			String val = name;
				if(isAccepting)val+="$";
				if(isFinal)val+="!";
				return val;
			}
		
		public boolean hasNextState(String in){
			if(isFinal)return true;
			for(String transition:transitionsS){
				if(transition.equals(in))return true;
			}
			return false;
		}
		public String getNextState(String in){
			if(isFinal)return name;
//			System.out.print(this + " " + in);
//			System.out.println(transitionsS);
			for(String transition:transitionsS){//input[1] is the inputs of the transitions
				String[] input = transition.split(":");
//				System.out.println(input[0] + " " + input[1].contains(in));
				if(isMoore)
					if(input[1].contains(in))return input[0];
					else{}//input[0] is the next state's name
				else {
					String[] ts = input[1].substring(0,input[1].length()-1).split(",");
					for(String t:ts){
						if(t.split("[/]")[0].equals(in))return input[0];
					}
				}
			}
			return "Not found";
		}
		public String output(String in){
			for(String transition:transitionsS){
				String[] input = transition.split(":");
				String[] outputs = input[1].substring(1,input[1].length()-1).split(",");
				for(String out:outputs){
					String[] pair = out.split("[/]");
					if(pair[0].equals(in))return pair[1];
				}
			}
			return null;
		}
	}
	
	
	public static void main(String args[]) throws Exception{
		ArrayList<String> fsms = new ArrayList<String>();
		ArrayList<String> inputs = new ArrayList<String>();
		boolean fflag,iflag,vflag,wflag,tflag;
		fflag=iflag=vflag=wflag=tflag=false;
		
		for(String elem:args){
			if(elem.equals("--fsm")){
					fflag = true;
					iflag=false;
			}
			else if(elem.equals("--input")){
					iflag = true;
					fflag=false;
			}
			else if(elem.equals("--verbose")){
				vflag = true;
				fflag = false;
				iflag = false;
			}
			else if(elem.equals("--warnings")){
				wflag = true;
				fflag = false;
				iflag = false;
			}
			else if(elem.equals("--unspecified-transitions-trap")){
				tflag = true;
				fflag = false;
				iflag = false;
			}
			
			if(fflag && !elem.contains("--"))
				fsms.add(elem);
			else if(iflag && !elem.contains("--"))
				inputs.add(elem);
		}
		
		for(String fsm:fsms){
			states.clear();
			transitions.clear();
			stateList.clear();
			parse(fsm);
			for(String file:inputs)runMachine(machine_type.equals("MOORE"),file,1);
		}
		
	}
	
	private static void runMachine(boolean isMoore, String inputFile, int inputLength) throws Exception {
		// TODO Auto-generated method stub
		State current=null;
		State start = null;
		for(State s:stateList)if(s.getName().equals(starting_state)){
			start=s;
			break;
		}
		BufferedReader inFile = new BufferedReader(new FileReader(inputFile));
		while(inFile.ready()){
			current = start;
			String mealyOutput = "";
			Pattern p = Pattern.compile("\\s*([\\w.,~@$!#%ˆ&\\-+{}()]*)\\s*:\\s*([\\w.,~@$!#%ˆ&\\-+{}()]*)");
			//Pattern p = Pattern.compile("\\s*(\\w*)\\s*:\\s*(\\w*)");
			String line = inFile.readLine();
			Matcher m = p.matcher(line);
			m.find();
			String inputName = m.group(1);
			String input = m.group(2);
			//System.out.println(inputName+" "+input);
			//ArrayList<String> trace = new ArrayList<String>();
			for(int i=0;i<input.length();i+=inputLength){
				//System.out.println(current);
				String in = input.substring(i,i+inputLength);
				if(!isMoore)mealyOutput+=current.output(in);
				String next = current.getNextState(in);
				for(State s:stateList){
	//				System.out.println("compare: " + s.getName() + " " + next);
					if(s.getName().equals(next)){
					current=s;
					break;
					}
				}
			}
			if(isMoore)
				if(current.isAccepting)System.out.println(machine_name+" : ACCEPTS "+input);
				else System.out.println(machine_name+" : REJECTS "+input);
			else System.out.println(machine_name+" : "+input+" / "+mealyOutput);
		}
	}

	public static void parse(String inFile) throws Exception{
		String check = "";
		BufferedReader fsmFile = new BufferedReader(new FileReader(inFile));
		
		machine_name = fsmFile.readLine();
		
		while(fsmFile.ready()){
			String line = fsmFile.readLine();
			
			/**********************************************************************/
			Pattern p = Pattern.compile("([a-zA-Z]*_[a-zA-Z]*)\\s*:\\s*(([|][uUlLaAdDnNsS])*\\w*\\W*)");
			Matcher m = p.matcher(line);
			while(m.find()){
				check = m.group(1);
				if(check.equalsIgnoreCase("INPUT_ALPHABET")){
					input_alph = m.group(2);
					for(String pipe:shortcuts.keySet()){
						input_alph = input_alph.replace(pipe, shortcuts.get(pipe));
					}
				}
				else if(check.equalsIgnoreCase("OUTPUT_ALPHABET")){
					output_alph = m.group(2);
					for(String pipe:shortcuts.keySet()){
						output_alph = output_alph.replace(pipe, shortcuts.get(pipe));
					}

				}
				else if(check.equalsIgnoreCase("MACHINE_TYPE")){
					machine_type = m.group(2);
				}
				else if(check.equalsIgnoreCase("STARTING_STATE")){
					starting_state = m.group(2);
				}
					
			}
			/*******************************************************************/
/*			 String charOrShortcut = "([\\w.,~@$!#%ˆ&\\-+{}]|([|][uUlLaAdDnNsS]))";  // this is just for shorthand in the next statement:

			 Pattern stateTransitionPattern = Pattern.compile("[{]\\s*((" + charOrShortcut + "\\s*[,]\\s*)*" + charOrShortcut + ")\\s*[}]\\s*");
*/			
			if(machine_type.equals("MOORE")){
				Pattern p2 = Pattern.compile("([\\w.,~@#%ˆ&\\-+]+[$!]?[!$]?)\\s*:\\s*(([\\w.,~@#%ˆ&\\-+]+:[{](([|][uUlLaAdDnNsS])*[\\w().,~@$!#%ˆ&\\-+{}]*[,]?)*[}]\\s*)*)");
				Matcher m2 = p2.matcher(line);
				while(m2.find()){
					if(!m2.group(1).equalsIgnoreCase("INPUT_ALPHABET")&&!m2.group(1).equalsIgnoreCase("OUTPUT_ALPHABET")&&!m2.group(1).equalsIgnoreCase("MACHINE_TYPE")&&!m2.group(1).equalsIgnoreCase("STARTING_STATE")){
						transitions.add(new LinkedList<String>());
						states.add(m2.group(1));
						Pattern p3 = Pattern.compile("[\\w.,~@#%ˆ&\\-+]+:[{](([|][uUlLaAdDnNsS])*[\\w().,~@$!#%ˆ&\\-+{}]*[,]?)*[}]");
						Matcher m3 = p3.matcher(m2.group(2));
						while(m3.find()){
							String temp = m3.group();
							for(String pipe:shortcuts_trans.keySet()){
								temp = temp.replace(pipe, shortcuts_trans.get(pipe));
							}
							transitions.getLast().add(temp);
						}
					}
				}
			}
			else if(machine_type.equals("MEALY")){
				Pattern p2 = Pattern.compile("([\\w.,~@#%ˆ&\\-+]+)\\s*:\\s*(([\\w.,~@#%ˆ&\\-+]+:[{](([|][uUlLaAdDnNsS])*[\\w().,~@$!#%ˆ&\\-+{}]*[,]?[/]?)*[}]\\s*)*)");
				Matcher m2 = p2.matcher(line);
				while(m2.find()){
					if(!m2.group(1).equalsIgnoreCase("INPUT_ALPHABET")&&!m2.group(1).equalsIgnoreCase("OUTPUT_ALPHABET")&&!m2.group(1).equalsIgnoreCase("MACHINE_TYPE")&&!m2.group(1).equalsIgnoreCase("STARTING_STATE")){
						transitions.add(new LinkedList<String>());
						states.add(m2.group(1));
						//Pattern p3 = Pattern.compile("[\\w.,~@#%ˆ&\\-+]+:[{](([|][uUlLaAdDnNsS])*[\\w().,~@$!#%ˆ&\\-+{}]*[,]?[/]?)*[}]");
						Pattern p3 = Pattern.compile("[\\w.,~@#%ˆ&\\-+]+:[{]\\s*(([|][uUlLaAdDnNsS]|[\\w().,~@$!#%ˆ&\\-+{}])[/]([\\w().~@$!#%ˆ&\\-+{}]*)\\s*[,]\\s*)*([|][uUlLaAdDnNsS]|[\\w().~@$!#%ˆ&\\-+{}])[/]([\\w().~@$!#%ˆ&\\-+{}]*)\\s*[}]");
						Matcher m3 = p3.matcher(m2.group(2));
						while(m3.find()){
							String temp = m3.group();
				//			System.out.println(temp);
							Pattern p4 = Pattern.compile("\\s*([|][uUlLaAdDnNsS]|[\\w().,~@$!#%ˆ&\\-+{}])[/]([\\w().~@$!#%ˆ&\\-+]*)\\s*");
							Matcher m4 = p4.matcher(temp);
							while(m4.find()){
				//				System.out.println(m4.group(1) + m4.group(2));
								if(shortcuts_trans.containsKey(m4.group(1)))
								temp = temp.replace(m4.group(1), shortcuts_trans.get(m4.group(1)).replace(",", "/"+m4.group(2)+","));//.replace(m4.group(1), m4.group(1)+"/"+m4.group(2)));
							}
							transitions.getLast().add(temp);
						}
					}
				}
			}
		}
		boolean m_type = machine_type.equals("MOORE");
		Iterator<LinkedList<String>> t = transitions.iterator();
		//System.out.println(transitions);
		for(int i=0;i<states.size();i++){
			State newSt = fsminst.new State(m_type,states.get(i),transitions.get(i));
			stateList.add(newSt);
		}
		System.out.println(machine_name);
		System.out.println(stateList);
		System.out.println(input_alph);
		System.out.println(output_alph);
		System.out.println(machine_type);
		System.out.println(starting_state);
		System.out.println(states);
		System.out.println(transitions);
		
		
	}
}
