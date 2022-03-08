package hfu.parsers.cfg;

import hfu.parsers.cfg.pep.Category;
import hfu.parsers.cfg.pep.LLParser;
import hfu.parsers.cfg.pep.ParseTree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class MyParseTree implements Iterable<MyParseTree> {
  String name;
  
  MyParseTree[] children;
  
  String value;
  
  MyParseTree() {}
  
  public MyParseTree(ParseTree ptree, String[] tokens) {
    this.name = ptree.getNode().getName();
    this.name = this.name.substring(1, this.name.length() - 1);
    String[] split;
    if (this.name.startsWith("§") && (split = this.name.split(":")).length == 2)
      this.name = split[0]; 
    if (ptree.getChildren() != null) {
      ArrayList<MyParseTree> cs = new ArrayList<>();
      byte b;
      int i;
      ParseTree[] arrayOfParseTree;
      for (i = (arrayOfParseTree = ptree.getChildren()).length, b = 0; b < i; ) {
        ParseTree c = arrayOfParseTree[b];
        Category cc = c.getNode();
        if (cc.isTerminal()) {
          if (cc.getName().startsWith("§"))
            cs.add(new MyParseTree(c, tokens)); 
        } else if (cc.getName().startsWith("§§§")) {
          if (!cc.getName().equals("§§§newline§§§"))
            cs.add(new MyParseTree(c.getChildren()[0], tokens)); 
        } else if (!cc.getName().startsWith("§§")) {
          cs.add(new MyParseTree(c, tokens));
        } 
        b++;
      } 
      this.children = cs.<MyParseTree>toArray(new MyParseTree[0]);
    } else {
      this.children = new MyParseTree[0];
      this.value = tokens[ptree.getOrigin()];
    } 
  }
  
  public static MyParseTree produce(LLParser.ParseTree ptree) {
    LinkedList<Record> stack = new LinkedList<>();
    Record result = new Record(ptree);
    stack.push(result);
    while (!stack.isEmpty()) {
      Record tos = stack.pop();
      MyParseTree mpt = tos.mptree;
      LLParser.ParseTree pt = tos.ptree;
      if (pt.children != null) {
        ArrayList<MyParseTree> cs = new ArrayList<>();
        byte b;
        int i;
        LLParser.ParseTree[] arrayOfParseTree;
        for (i = (arrayOfParseTree = pt.children).length, b = 0; b < i; ) {
          LLParser.ParseTree t = arrayOfParseTree[b];
          if (!t.c.isTerminal() || t.value != null) {
            Record child = new Record(t);
            cs.add(child.mptree);
            stack.push(child);
          } 
          b++;
        } 
        mpt.children = cs.<MyParseTree>toArray(new MyParseTree[0]);
        continue;
      } 
      mpt.children = new MyParseTree[0];
      mpt.value = (pt.value == null) ? "" : pt.value;
    } 
    return result.mptree;
  }
  
  static class Record {
    LLParser.ParseTree ptree;
    
    MyParseTree mptree;
    
    Record(LLParser.ParseTree ptree) {
      this.ptree = ptree;
      this.mptree = new MyParseTree();
      this.mptree.name = ptree.c.getName();
    }
  }
  
  public MyParseTree(LLParser.ParseTree ptree) {
    this.name = ptree.c.getName();
    if (ptree.children != null) {
      ArrayList<MyParseTree> cs = new ArrayList<>();
      byte b;
      int i;
      LLParser.ParseTree[] arrayOfParseTree;
      for (i = (arrayOfParseTree = ptree.children).length, b = 0; b < i; ) {
        LLParser.ParseTree t = arrayOfParseTree[b];
        if (!t.c.isTerminal() || t.value != null)
          cs.add(new MyParseTree(t)); 
        b++;
      } 
      this.children = cs.<MyParseTree>toArray(new MyParseTree[0]);
    } else {
      this.children = new MyParseTree[0];
      this.value = (ptree.value == null) ? "" : ptree.value;
    } 
  }
  
  public MyParseTree get(String name) {
    MyParseTree child = null;
    byte b;
    int i;
    MyParseTree[] arrayOfMyParseTree;
    for (i = (arrayOfMyParseTree = this.children).length, b = 0; b < i; ) {
      MyParseTree c = arrayOfMyParseTree[b];
      if (c.name.equals(name)) {
        child = c;
        break;
      } 
      b++;
    } 
    return child;
  }
  
  public MyParseTree rNext() {
    return get(this.name);
  }
  
  public int rSize() {
    MyParseTree rtree = get(this.name);
    if (rtree == null)
      return 0; 
    return 1 + rtree.rSize();
  }
  
  public int asInteger() {
    return Integer.parseInt(this.value);
  }
  
  public String asString() {
    return this.value;
  }
  
  public double asFloat() {
    return Double.parseDouble(this.value);
  }
  
  public boolean isLeaf() {
    return (this.children.length == 0);
  }
  
  public Iterator<MyParseTree> iterator() {
    return new RecursiveIterator();
  }
  
  class RecursiveIterator implements Iterator<MyParseTree> {
    MyParseTree current;
    
    public boolean hasNext() {
      return (MyParseTree.this.rNext() != null);
    }
    
    public MyParseTree next() {
      MyParseTree result = this.current;
      this.current = MyParseTree.this.rNext();
      return result;
    }
    
    public void remove() {}
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\MyParseTree.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */