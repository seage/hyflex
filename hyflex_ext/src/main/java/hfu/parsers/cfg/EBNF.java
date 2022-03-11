package hfu.parsers.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import hfu.parsers.cfg.pep.Category;
import hfu.parsers.cfg.pep.Grammar;
import hfu.parsers.cfg.pep.ParserOption;
import hfu.parsers.cfg.pep.Rule;

public class EBNF {
	
	Grammar grammar;
	
	public EBNF(){
		grammar = new Grammar("ebnf");
		//create rules to recognize java literals
		//int
		/*
		addRule("[§int§]","-[§intv§]");
		addRule("[§int§]","[§intv§]");
		addRule("[§intv§]","[§digit§]");
		addRule("[§intv§]","[§digit§][§intv§]");
		for(int i = 0; i < 10;i++){
			addRule("[§digit§]",""+i);
		}
		*/
	}

	public void addRule(String left, String right){

		/*
		boolean hasTerminal = false;
		for(int i = 0; i < right.length();i++){
			char c = right.charAt(i);
			if(Character.isWhitespace(c) && c != '\n'){
				right_cs.add(new Category("§whitespace§",true));
				hasTerminal = true;
				//ignore any further whitespace
				while(Character.isWhitespace(c)  && c != '\n' && i < right.length()){
					i++;
					c = right.charAt(i);
				}
				i--;
			}else if(c == '<'){
				//literal
				String lit = "";
				//obtain all text till '>'
				while(c != '>' && i < right.length()){
					i++; 
					lit += c;
					c = right.charAt(i);
				}
				String[] lit2 = lit.split(":");
				String name = lit2[0].substring(1);
				String type = lit2[1];
				right_cs.add(new Category("§"+type+"§")); //name is lost here (should be stored)
			}else if(c == '['){
				//category
				String cat = "";
				//obtain all text till '>'
				while(c != ']' && i < right.length()){
					i++; 
					cat += c;
					c = right.charAt(i);
				}
				right_cs.add(new Category(cat.substring(1)));
			}else if(c == '\\'){
				//ignore the backslash, but next is interpreted as a terminal
				i++;
				c = right.charAt(i);
				right_cs.add(new Category(""+c,true));
				hasTerminal = true;
			}else if(c == '\n'){
				right_cs.add(new Category("§newline§",true));
				hasTerminal = true;
			}else{
				//just a terminal character
				right_cs.add(new Category(""+c,true));
				hasTerminal = true;
			}
			
		}
		*/
		//create a category for left
		Category left_c = null;
		left = left.trim();
		if(left.startsWith("[") && left.endsWith("]")){
			left_c = new Category(left.substring(1, left.length()-1).trim());
		}else{
			System.out.println("ERROR: Invalid rule head: "+left);
		}
		//parse categories for the right
		
		Category[] right_cs;
		String[] right_split = right.split("[ \\t\\x0B\\f\\r]+");
		if(right.length() == 0){
			right_cs = new Category[]{new Category("",true)};
		}else{
			right_cs = new Category[right_split.length];
			for(int i = 0; i < right_split.length;i++){
				char c = right_split[i].charAt(0);
				if(c == '<'){
					right_cs[i] = new Category("§"+right_split[i].substring(1, right_split[i].length()-1)+"§",true);
				}else if(c == '['){
					right_cs[i] = new Category(right_split[i].substring(1,right_split[i].length()-1));
				}else if(c == '\n'){
					right_cs[i] = new Category("§newline§",true);
				}else if(c == '\\'){
					//ignore backslash, but consider what follows as terminal
					right_cs[i] = new Category(right_split[i].substring(1),true);
				}else{
					right_cs[i] = new Category(right_split[i],true);
				}
			}
		}
		/*
		//convert to non-preterminal rules (more efficient!)
		if(right_cs.length > 1 && hasTerminal){
			//preterminal rule
			for(int i = 0; i < right_cs.length;i++){
				if(right_cs[i].isTerminal()){
					//substitute by a dummy rule
					Category dummy = new Category("§§"+right_cs[i].getName()+"§§");
					grammar.addRule(new Rule(dummy,right_cs[i]));
					right_cs[i] = dummy;
				}
			}
		}
		*/
		/*
		//convert right recursive rules to left recursive rules
		if(right_cs[right_cs.length-1].equals(left_c)){
			for(int i = right_cs.length-2; i >= 0;i--){
				right_cs[i+1] = right_cs[i];
			}
			right_cs[0] = left_c;
		}
		*/
		
		grammar.addRule(new Rule(left_c,right_cs));
		
	}
	
	public Grammar getGrammar(){
		/*
		//return a copy of this grammar without empty rules
		Grammar proper_grammar = new Grammar("proper_ebnf");
		//split into proper and inproper rules
		Set<Rule> empty_rules = new HashSet<Rule>();
		for(Rule r : grammar.getAllRules()) {
			if(r.getRight()[0].getName().length() == 0){
				//r is an empty rule
				//as an exception keep empty root in
				if(!r.getLeft().getName().equals("S")){
					empty_rules.add(r);
				}
			}else{
				//r is a proper rule
				proper_grammar.addRule(r);
			}
		}
		while(empty_rules.size() > 0){
			Grammar proper_grammar2 = new Grammar("proper_ebnf");
			Set<Rule> new_empty_rules = new HashSet<Rule>();
			for(Rule er : empty_rules) {
				for(Rule r : proper_grammar.getAllRules()) {
					//now check whether we need to add reductions
					addReductions(er.getLeft(), r,0,new ArrayList<Category>(),proper_grammar2,new_empty_rules);
				}
			}
			empty_rules = new_empty_rules;
			proper_grammar = proper_grammar2;
		}
		*/
		return /*proper_grammar*/ grammar;
	}

	
	
	private void addReductions(Category left, Rule r, int index, ArrayList<Category> output, Grammar proper_grammar, Set<Rule> empty_rules){
		Category[] right = r.getRight();
		if(index < right.length){
			if(right[index].equals(left)){
				ArrayList<Category> output2 = new ArrayList<Category>(output);
				addReductions(left,r,index+1,output2,proper_grammar,empty_rules);
			}
			output.add(right[index]);
			addReductions(left,r,index+1,output,proper_grammar,empty_rules);
		}else{
			if (output.size() == 0){
				//empty rule
				if(!r.getLeft().getName().equals("S")){
					empty_rules.add(new Rule(r.getLeft(),new Category("",true)));
				}
			}else{
				proper_grammar.addRule(new Rule(r.getLeft(),(Category[]) output.toArray(new Category[0])));
			}
			
		}
	}

}
