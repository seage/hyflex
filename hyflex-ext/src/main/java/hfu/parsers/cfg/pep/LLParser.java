/*
 * $Id: EarleyParser.java 1807 2010-02-05 22:20:02Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package hfu.parsers.cfg.pep;

import hfu.parsers.cfg.MyTokenizer;
import hfu.parsers.cfg.MyTokenizer.MyTokenIterator;
import hfu.parsers.cfg.MyTokenizer.MyTokenIterator.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


public class LLParser {
	Rule[] rules;
	Set<Category> terminals;
	Set<Category> categories;
	HashMap<Category,HashMap<Category,Integer>> table;

	public LLParser(Grammar grammar) {
		Set<Rule> rule_set = grammar.getAllRules();
		rules = new Rule[rule_set.size()];
		terminals = new HashSet<Category>();
		categories = new HashSet<Category>();
		int k = 0;
		for(Rule r: rule_set){
			rules[k] = r;
			k++;
			categories.add(r.getLeft());
			Category[] right = r.getRight();
			for(Category c : right){
				if(c.isTerminal()){
					terminals.add(getTerminal(c));
				}else{
					categories.add(c);
				}
			}
		}
		//create parse table
		buildTable();
	}
	
	private Category getTerminal(Category c){
		//get a terminal, in case of a typed, §a:type§, this function returns §type§
		String name = c.getName();
		String[] split;
		if(name.length() > 0 && name.charAt(0) == '§' && (split = name.substring(1, name.length()-1).split(":")).length == 2){
			c = new Category("§"+split[1]+"§",true);
		}
		return c;
	}
	
	private String[] hasType(Category c){
		String name = c.getName();
		String[] split;
		if(name.length() > 0 && name.charAt(0) == '§' && (split = name.substring(1, name.length()-1).split(":")).length == 2){
			return split;
		}
		return null;
	}
	
	private Set<Category> getFi(int pos, Category[] right, HashMap<Category,Set<Category>> leftFI){
		//Fi is defined as follows
		Set<Category> result = new HashSet<Category>();
		if(pos == right.length){
			//Fi(e) = { e }
			result.add(new Category("",true));
		}else{
			Category first = right[pos];
			if(first.isTerminal()){
				//Fi(a w' ) = { a } for every terminal a
				//Fi(e) = { e }
				result.add(getTerminal(first));
			}else{
				result.addAll(leftFI.get(first));
				if(result.remove(new Category("",true))){
					//Fi(A w' ) = Fi(A) \ { e } U Fi(w' ) for every nonterminal A with e in Fi(A)
					result.addAll(getFi(pos+1,right,leftFI));
				}
			}
		}
		return result;
	}
	
	private void buildTable(){
		int n = rules.length;
		//determine the first-set of each Ai & wi
		Set<Category>[] rightFi = new Set[n];
		HashMap<Category,Set<Category>> leftFi = new HashMap<Category,Set<Category>>();
		//1) initialize every Fi(wi) and Fi(Ai) with the empty set
		for(int i = 0; i < n;i++){
			leftFi.put(rules[i].getLeft(),new HashSet<Category>());
			rightFi[i] = new HashSet<Category>();
		}
		boolean modified = true;
		while(modified){
			modified = false;
			//2,3) add Fi(wi) to Fi(A) for every rule Ai -> wi
			for(int i = 0; i < n;i++){
				Rule r = rules[i];
				Set<Category> FiAi = leftFi.get(r.getLeft());
				rightFi[i] = getFi(0,r.getRight(),leftFi);
				modified = FiAi.addAll(rightFi[i]) || modified;
			}
		}//do steps 2 and 3 until all Fi sets stay the same.
		
		//determine follow set of A

	    //initialize every Fo(Ai) with the empty set
		HashMap<Category,Set<Category>> leftFo = new HashMap<Category,Set<Category>>();
		for(int i = 0; i < n;i++){
			leftFo.put(rules[i].getLeft(),new HashSet<Category>());
		}
	    modified = true;
	    while(modified){
	    	modified = false;
			for(int i = 0; i < n;i++){
				Rule r = rules[i];
				Category[] right = r.getRight();
				for(int j = 0; j < right.length;j++){
					//if there is a rule of the form Aj -> wAiw' , then
					if(!right[j].isTerminal()){
						Set<Category> Fiwp = getFi(j+1,right,leftFi);
						for(Category c: Fiwp){
							if(c.equals(new Category("",true))){
								//if e is in Fi(w' ), then add Fo(Aj) to Fo(Ai)
								//if w' has length 0, then add Fo(Aj) to Fo(Ai)
								modified = leftFo.get(right[j]).addAll(leftFo.get(r.getLeft())) || modified;
							}else if(c.isTerminal()){
								//if the terminal a is in Fi(w' ), then add a to Fo(Ai)
								modified = leftFo.get(right[j]).add(c) || modified;
							}
						}
					}
				}
			}
	    }
		//compute the parsing table
		table = new HashMap<Category,HashMap<Category,Integer>>();
		
	    
		for(Category t : terminals){
			table.put(t, new HashMap<Category,Integer>());
			for(Category c : categories){
				table.get(t).put(c, null);
				//T[A,a] contains the rule A -> w if and only if
				for(int i = 0; i < rules.length;i++){
					//a is in Fi(w) or
			        //e is in Fi(w) and a is in Fo(A).
					if(rules[i].getLeft().equals(c) && (rightFi[i].contains(t) || (rightFi[i].contains(new Category("",true)) && leftFo.get(rules[i].getLeft()).contains(t)))){
						if(table.get(t).get(c) == null){
							table.get(t).put(c, i);
						}else{
							System.out.println("This grammar is not LL(1)");
						}
					}
				}
			}
		}
	}

	public ParseTree parse(MyTokenizer tokens, Category seed){
		//parse the file
		MyTokenIterator it = tokens.getTokenIterator();
		ParseTree current = new ParseTree(seed);
		ParseTree result = current;
		Token token = null;
		boolean next = true;
		int k = 0;
		while(current != null){
			if(next){
				if(it.hasNext()){
					token = it.next();
					k++;
					/*
					if(k == 11480){
						System.out.println(k);
					}
					*/
					next = false;
				}else{
					token = it.getEOF();
				}
			}
			Category tos = current.getNode();
			if(tos.isTerminal()){
				//match
				String[] r;
				if((r = hasType(tos)) != null){
					current.value = token.value;
					current.c = new Category(r[0],true);
					try{
						if(r[1].equals("int")){
							Integer.parseInt(token.value);
						}else if(r[1].equals("float")){
							Double.parseDouble(token.value);
						}else if(r[1].equals("string")){
							if(token.value.equals("§newline§")){
								System.out.println("ERROR @"+token.line+"("+token.pos+"): Token "+token.value+" is not of type "+r[1]);
								break; //newline is the only character that can't be interpreted as a String
							}
						}else{
							//error
							System.out.println("ERROR @"+token.line+"("+token.pos+"): Unknown type "+r[1]);
							break;
						}
					}catch(Exception e){
						//error
						System.out.println("ERROR @"+token.line+"("+token.pos+"): Token "+(token.value.equals("")? "EOF" : token.value)+" is not of type "+r[1]);
						break;
					}
					next = true;
					current = pop(current);
				}else if(tos.getName().equals(token.value)){
					//plain text matching
					next = true;
					current = pop(current);
				}else{
					//error
					System.out.println("ERROR @"+token.line+"("+token.pos+"): Got "+(token.value.equals("")? "EOF" : token.value)+", expected "+tos);
					break;
				}
			}else{
				//replace by right side of rule in the parse table
				Integer ri = null;
				HashMap<Category,Integer> t;
				if((t = table.get(new Category(token.value,true))) != null && (ri = t.get(tos)) != null){
					//literal hit
				}
				if(ri == null && !token.value.equals("§newline§") && (t = table.get(new Category("§string§",true))) != null && (ri = t.get(tos)) != null){
					//string
				}
				if(ri == null && (t = table.get(new Category("§float§",true))) != null && (ri = t.get(tos)) != null){
					//float
					try{
						Double.parseDouble(token.value);
					}catch(Exception e){
						ri = null;
					}
				}
				if(ri == null && (t = table.get(new Category("§int§",true))) != null && (ri = t.get(tos)) != null){
					//integer
					try{
						Integer.parseInt(token.value);
					}catch(Exception e){
						ri = null;
					}
				}
				
				if(ri != null){
					Category[] right = rules[ri].getRight();
					ParseTree[] children = new ParseTree[right.length];
					if(!right[0].getName().equals("")){
						for(int i = 0; i < right.length;i++){
							children[i] = new ParseTree(right[i]);
						}
						current = current.setChildren(children);
					}else{
						current = pop(current);
					}
				}else{
					System.out.println("ERROR @"+token.line+"("+token.pos+"): Category "+ tos + ", can't start with "+(token.value.equals("")? "EOF" : token.value));
					//error
					break;
				}
			}
		}
		if(!it.hasNext() && current == null){
			//System.out.println("ACCEPT");
		}else{
			System.out.println("REJECT");
		}
		//init: current node = S
		//if we apply a rule => add children for S + current node = first child
		//if we match a token, go up & current-node = next child (if no next child = go up)
		return result;
	}
	
	private ParseTree pop(ParseTree stack){
		ParseTree new_stack = stack.next();
		while(new_stack == null){
			if(stack.parent == null){
				break;
			}
			stack = stack.parent;
			new_stack = stack.next();
		}
		return new_stack;
	}
	
	public class ParseTree {
		public Category c;
		public ParseTree[] children;
		public ParseTree parent;
		public int i = 0;
		public String value;
		
		ParseTree(Category c){
			this.c = c;
		}
		
		ParseTree setChildren(ParseTree[] children){
			this.children = children;
			for(int i = 0; i < children.length;i++){
				children[i].parent = this;
			}
			return this.children[0];
		}
		
		Category getNode(){
			return c;
		}
		
		ParseTree next(){
			i++;
			if(children != null && i < children.length){
				return children[i];
			}else{
				return null;
			}
		}
		
		public String toString(){
			String result = c.getName();
			if(children != null && children.length > 0){
				result += "[";
				for(int i = 0; i < children.length-1;i++){
					result += children[i].toString();
					result += ",";
				}
				result += children[children.length-1].toString();
				result += "]";
			}else if(value != null){
				result += "="+value;
			}
			return result;
		}
		
		
	}
	

}
