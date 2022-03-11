/*
 * $Id: Grammar.java 1781 2010-01-19 04:21:54Z scott $ 
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
import java.util.Map;
import java.util.Set;


/**
 * Represents a context-free grammar (set of production rules).
 * <p>
 * Grammars maintain their rules indexed by
 * {@link Rule#getLeft() left side category}. The rule sets contained for
 * any given {@link Category left category} are not guaranteed to be 
 * maintained in the order of insertion.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1781 $
 */
public class Grammar {
	String name;
	Set<Category> nullable;
	Map<Category, Set<Rule>> rules;
	
	
	public boolean isNullable(Category c){
		if(nullable == null){
			computeNullable();
		}
		return nullable.contains(c);
	}
	
	private void computeNullable(){
		nullable = new HashSet<Category>();
		boolean modified = true;
		while(modified){
			modified = false;
			//find for each category appearing on the left, whether it is nullable:
			//rightside contains nothing but nullable categories
			for(Category c : rules.keySet()) {
				if(!nullable.contains(c)){
					boolean isNullable = true;
					for(Rule r : rules.get(c)){
						isNullable = true;	
						if(!r.getRight()[0].getName().equals("")){
							for(Category rc : r.getRight()){
								isNullable = isNullable && nullable.contains(rc);
							}
						}
						if(isNullable){
							break;
						}
					}
					if(isNullable){
						nullable.add(c);
						modified = true;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Creates a grammar with the given name, initializes its internal data
	 * structure.
	 * @param name The mnemonic name for this grammar.
	 */
	public Grammar(String name) {
		this.name = name;
		rules = new HashMap<Category, Set<Rule>>();
	}
	
	/**
	 * Gets the name of this grammar.
	 * @return The value specified when this grammar was created.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Adds a production rule. If no rules are contained for the 
	 * {@link Rule#getLeft() left side} of the specified rule, a new rule
	 * set is created.
	 * @param rule The rule to add.
	 * @return <code>true</code> iff this grammar did not already contain
	 * the specified rule.
	 * @throws NullPointerException If <code>rule</code> is <code>null</code>.
	 */
	public boolean addRule(Rule rule) {
		nullable = null;
		if(rule == null) {
			throw new NullPointerException("null rule");
		}
		
		Set<Rule> r;
		if(!rules.containsKey(rule.left)) { // already rules for rule.left?
			r = new HashSet<Rule>();
			rules.put(rule.left, r); // create, add rule set at rule.left
		}
		else {
			r = rules.get(rule.left); // get the existing rule set
		}
		
		return r.add(rule);
	}
	
	/**
	 * Tests whether this grammar contains rules for the specified left side
	 * category.
	 * @param left The left category of the rules to test for.
	 * @return <code>true</code> iff this grammar contains rules with the
	 * specified category as their {@link Rule#getLeft() left side}.
	 */
	public boolean containsRules(Category left) {
		return rules.containsKey(left);
	}
	
	/**
	 * Gets the set of rules contained by this grammar with the given left
	 * side category.
	 * @param left The {@link Rule#getLeft() left side} of the rules to find.
	 * @return A set containing the rules in this grammar whose 
	 * {@link Rule#getLeft() left side} is
	 * {@link Category#equals(Object) the same} as <code>left</code>, or
	 * <code>null</code> if no such rules are contained in this grammar. The
	 * rule set returned by this method is <em>not</em> guaranteed to contain
	 * the rules in the order in which they were {@link #addRule(Rule) added}.
	 */
	public Set<Rule> getRules(Category left) {
		return rules.get(left);
	}
	
	/**
	 * Gets every rule in this grammar.
	 */
	public Set<Rule> getAllRules() {
		Set<Rule> allRules = new HashSet<Rule>();
		
		for(Set<Rule> s : rules.values()) {
			allRules.addAll(s);
		}
		
		return allRules;
	}
	
	/**
	 * Gets a singleton preterminal rule with the specified left category,
	 * producing the given string token.
	 * @param left The left side of the preterminal rule.
	 * @param token The right side of the preterminal rule.
	 * @param ignoreCase Whether the comparison of right side rules in this
	 * grammar with the specified <code>token</code> should be done on a
	 * case-insensitive basis.
	 * @return A preterminal rule of the form <code>left -> token</code> if
	 * any exists within this grammar, or <code>null</code> if none exists.
	 * @see Rule#isSingletonPreterminal()
	 */
	Rule getSingletonPreterminal(Category left, String token,
			boolean ignoreCase) {
		if(rules.containsKey(left)) {
			for(Rule r : rules.get(left)) {
				if(r.isSingletonPreterminal() && extended_equals(r.right[0].name,token,ignoreCase)) {
					return r;
				}
			}
		}

	    return null;
	}
	
	private boolean extended_equals(String terminal, String token, boolean ignoreCase){
		String[]  split;
		if(terminal.length() > 0 && terminal.charAt(0) == '§' && (split = terminal.substring(1, terminal.length()-1).split(":")).length == 2){
			if(split[1].equals("int")){
				//integer
				try{
					Integer.parseInt(token);
					return true;
				}catch(Exception e){
					return false;
				}
			}else if(split[1].equals("float")){
				//double
				try{
					Double.parseDouble(token);
					return true;
				}catch(Exception e){
					return false;
				}
			}else if(split[1].equals("string")){
				//string
				return !token.equals("§newline§");
			}else{
				System.out.println("ERROR: Unknown type: "+split[1]);
				return false;
			}
		}else{
			return terminal.equals(token) || (ignoreCase && terminal.equalsIgnoreCase(token));
		}
	}
	
	/**
	 * Gets a string representation of this grammar.
	 * @return A string listing all of the rules contained by this grammar.
	 * @see Rule#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append(getClass().getSimpleName());
		sb.append(' ');
		sb.append(name);
		sb.append(": {");
		
		Iterator<Set<Rule>> si = rules.values().iterator();
		while(si.hasNext()) {
			Iterator<Rule> ri = si.next().iterator();
			while(ri.hasNext()) {
				sb.append(ri.next().toString());
				if(ri.hasNext()) {
					sb.append(", ");
				}
			}
			
			if(si.hasNext()) {
				sb.append(", ");
			}
		}
		
		sb.append("}]");
		
		return sb.toString();
	}
}
