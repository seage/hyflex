package hfu.parsers.cfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class MyTokenizer implements Iterable<String> {
  InputStream in;
  
  public MyTokenizer(InputStream in) {
    this.in = in;
  }
  
  public Iterator<String> iterator() {
    return new MyIterator();
  }
  
  public MyTokenIterator getTokenIterator() {
    return new MyTokenIterator();
  }
  
  public class MyIterator implements Iterator<String> {
    BufferedReader reader;
    
    String[] line;
    
    int index;
    
    int line_count;
    
    boolean done;
    
    public MyIterator() {
      try {
        this.reader = new BufferedReader(new InputStreamReader(MyTokenizer.this.in, "UTF-8"));
        this.line = new String[0];
        this.index = 1;
        this.line_count = 0;
        this.done = false;
      } catch (IOException e) {
        e.printStackTrace();
      } 
    }
    
    private void computeNext() {
      if (this.index > this.line.length) {
        String raw_line = null;
        try {
          raw_line = this.reader.readLine();
          this.line_count++;
          this.index = 0;
        } catch (IOException e) {
          e.printStackTrace();
        } 
        if (raw_line != null) {
          this.line = raw_line.trim().split("\\s+");
        } else {
          this.done = true;
        } 
      } 
    }
    
    public boolean hasNext() {
      computeNext();
      return !this.done;
    }
    
    public String next() {
      computeNext();
      if (this.done)
        return null; 
      if (this.index == this.line.length) {
        this.index++;
        return "§newline§";
      } 
      return this.line[this.index++];
    }
    
    public void remove() {}
  }
  
  public class MyTokenIterator implements Iterator<MyTokenIterator.Token> {
    BufferedReader reader;
    
    String[] line;
    
    int index;
    
    int line_count;
    
    boolean done;
    
    public MyTokenIterator() {
      try {
        this.reader = new BufferedReader(new InputStreamReader(MyTokenizer.this.in, "UTF-8"));
        this.line = new String[0];
        this.index = 1;
        this.line_count = 0;
        this.done = false;
      } catch (IOException e) {
        e.printStackTrace();
      } 
    }
    
    private void computeNext() {
      if (this.index > this.line.length) {
        String raw_line = null;
        try {
          raw_line = this.reader.readLine();
          this.line_count++;
          this.index = 0;
        } catch (IOException e) {
          e.printStackTrace();
        } 
        if (raw_line != null) {
          this.line = raw_line.trim().split("\\s+");
        } else {
          this.done = true;
        } 
      } 
    }
    
    public boolean hasNext() {
      computeNext();
      return !this.done;
    }
    
    public Token next() {
      computeNext();
      Token token = new Token();
      if (this.done)
        return null; 
      if (this.index == this.line.length) {
        this.index++;
        token.value = "§newline§";
        token.line = this.line_count;
        token.pos--;
      } else {
        token.value = this.line[this.index++];
        token.line = this.line_count;
        token.pos--;
      } 
      return token;
    }
    
    public Token getEOF() {
      Token t = new Token();
      t.value = "";
      t.line = this.line_count++;
      t.pos = 0;
      return t;
    }
    
    public void remove() {}
    
    public class Token {
      public String value;
      
      public int line;
      
      public int pos;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\MyTokenizer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */