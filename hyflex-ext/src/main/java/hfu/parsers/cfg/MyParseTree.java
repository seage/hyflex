package hfu.parsers.cfg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import hfu.parsers.cfg.pep.Category;

public class MyParseTree implements Iterable<MyParseTree>{

	String name;
	MyParseTree[] children;
	String value;
	
	MyParseTree(){
		
	}
	
	public MyParseTree(hfu.parsers.cfg.pep.ParseTree ptree, String[] tokens){
		name = ptree.getNode().getName();
		if(name.startsWith("§")){
			name = name.substring(1, name.length()-1);
			String[] split;
			if((split = name.split(":")).length == 2){
				name = split[0];
			}
		}
		if(ptree.getChildren() != null){
			ArrayList<MyParseTree> cs = new ArrayList<MyParseTree>();
			for(hfu.parsers.cfg.pep.ParseTree c: ptree.getChildren()){
				//only retain named components
				//<name:type>
				//[category]
				Category cc = c.getNode();
				if(cc.isTerminal()){
					if(cc.getName().startsWith("§")){
						cs.add(new MyParseTree(c,tokens));
					}else{
						//don't do anything (these are literals)
					}
				}else{
					if(cc.getName().startsWith("§§§")){
						//reduce §§§ rules
						if(!cc.getName().equals("§§§newline§§§")){
							cs.add(new MyParseTree(c.getChildren()[0],tokens));
						}
					}else if(cc.getName().startsWith("§§")){
						//don't do anything (these are literals)
					}else{
						cs.add(new MyParseTree(c,tokens));
					}
				}
			}
			children = cs.toArray(new MyParseTree[0]);
		}else{
			children = new MyParseTree[0];
			value = tokens[ptree.getOrigin()];
		}
	}
	
	public static MyParseTree produce(hfu.parsers.cfg.pep.LLParser.ParseTree ptree){
		//stack representing trees still to be converted
		LinkedList<Record> stack = new LinkedList<Record>();
		Record result = new Record(ptree);
		stack.push(result);
		while(!stack.isEmpty()){
			Record tos = stack.pop();
			MyParseTree mpt = tos.mptree;
			hfu.parsers.cfg.pep.LLParser.ParseTree pt = tos.ptree;
			if(pt.children != null){
				ArrayList<MyParseTree> cs = new ArrayList<MyParseTree>();
				for(hfu.parsers.cfg.pep.LLParser.ParseTree t: pt.children){
					if(!t.c.isTerminal() || t.value != null){
						//retain all categories && terminals with value
						Record child = new Record(t);
						cs.add(child.mptree);
						stack.push(child);
					}
				}
				mpt.children = cs.toArray(new MyParseTree[0]);
			}else{
				mpt.children = new MyParseTree[0];
				mpt.value = pt.value == null? "" : pt.value;
			}
		}
		return result.mptree;
	}
	
	static class Record{
		hfu.parsers.cfg.pep.LLParser.ParseTree ptree;
		MyParseTree mptree;
		
		Record(hfu.parsers.cfg.pep.LLParser.ParseTree ptree){
			this.ptree = ptree;
			mptree = new MyParseTree();
			mptree.name = ptree.c.getName();
		}
	}
	
	public MyParseTree(hfu.parsers.cfg.pep.LLParser.ParseTree ptree){
		name = ptree.c.getName();
		//filter children
		if(ptree.children != null){
			ArrayList<MyParseTree> cs = new ArrayList<MyParseTree>();
			for(hfu.parsers.cfg.pep.LLParser.ParseTree t: ptree.children){
				if(!t.c.isTerminal() || t.value != null){
					//retain all categories && terminals with value
					cs.add(new MyParseTree(t));
				}
			}
			children = cs.toArray(new MyParseTree[0]);
		}else{
			children = new MyParseTree[0];
			value = ptree.value == null? "" : ptree.value;
		}
	}
	
	public MyParseTree get(String name){
		MyParseTree child = null;
		for(MyParseTree c: children){
			if(c.name.equals(name)){
				child = c;
				break;
			}
		}
		return child;
	}
	
	public MyParseTree rNext(){
		return get(name);
	}
	
	public int rSize(){
		MyParseTree rtree = get(name);
		if(rtree == null){
			return 0;
		}else{
			return 1+rtree.rSize();
		}
	}
	
	public int asInteger(){
		return Integer.parseInt(value);
	}
	
	public String asString(){
		return value;
	}
	
	public double asFloat(){
		return Double.parseDouble(value);
	}
	
	public boolean isLeaf(){
		return children.length == 0;
	}

	@Override
	public Iterator<MyParseTree> iterator() {
		return new RecursiveIterator();
	}
	
	class RecursiveIterator implements Iterator<MyParseTree>{

		MyParseTree current;
		
		@Override
		public boolean hasNext() {
			return rNext() != null;
		}

		@Override
		public MyParseTree next() {
			MyParseTree result = current;
			current = rNext();
			return result;
		}

		@Override
		public void remove() {

		}
		
	}

}
