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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


public class CopyOfLLParser {
	Rule[] rules;
	Set<Category> terminals;
	Set<Category> categories;
	HashMap<Category,HashMap<Category,Integer>> table;

	public CopyOfLLParser(Grammar grammar) {
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
					terminals.add(c);
				}else{
					categories.add(c);
				}
			}
		}
		//create parse table
		buildTable();
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
			result.add(first);
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
	        
		System.out.print("");

	}

	public void parse(Iterable<String> tokens, Category seed){
		//parse the file
		//stack
		LinkedList<Category> stack = new LinkedList<Category>();
		stack.push(seed);
		Iterator<String> it = tokens.iterator();
		ParseTree current = new ParseTree(seed.getName());
		String token = null;
		boolean next = true;
		while(stack.size() > 0){
			if(next){
				if(it.hasNext()){
					token = it.next();
					next = false;
				}else{
					//error
					break;
				}
			}
			if(stack.getFirst().isTerminal()){
				//match
				if(stack.getFirst().getName().equals(token)){
					stack.pop();
					next = true;
					current = current.next();
				}else{
					//error
					break;
				}
			}else{
				//replace by right side of rule in the parse table
				Integer ri = table.get(new Category(token,true)).get(stack.getFirst());
				stack.pop();
				if(ri != null){
					Category[] right = rules[ri].getRight();
					ParseTree[] children = new ParseTree[right.length];
					for(int i = 0; i < right.length;i++){
						stack.push(right[right.length-1-i]);
						children[i] = new ParseTree(right[i].getName());
					}
					current = current.setChildren(children);
				}else{
					//error
					break;
				}
			}
		}
		if(!it.hasNext() && stack.size() == 0){
			System.out.println("ACCEPT");
		}else{
			System.out.println("REJECT");
		}
		//init: current node = S
		//if we apply a rule => add children for S + current node = first child
		//if we match a token, go up & current-node = next child (if no next child = go up)
		
	}
	
	class ParseTree {
		String name;
		ParseTree[] children;
		ParseTree parent;
		int i = 0;
		
		ParseTree(String root){
			this.name = root;
		}
		
		ParseTree setChildren(ParseTree[] children){
			this.children = children;
			for(int i = 0; i < children.length;i++){
				children[i].parent = this;
			}
			return this.children[0];
		}
		
		ParseTree next(){
			i++;
			if(children != null && i < children.length){
				return children[i];
			}else{
				if(parent !=  null){
					return parent.next();
				}else{
					return this;
				}
			}
		}
		
		
	}
	

}
