package hfu.parsers.cfg;

import hfu.parsers.cfg.MyTokenizer.MyTokenIterator.Token;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class MyTokenizer implements Iterable<String>{
	
	String file;
	
	public MyTokenizer(String file){
		this.file = file;
	}

	public Iterator<String> iterator() {
		return new MyIterator();
	}
	
	public MyTokenIterator getTokenIterator() {
		return new MyTokenIterator();
	}

	public class MyIterator implements Iterator<String>{
		BufferedReader reader;
		String[] line;
		int index;
		int line_count;
		boolean done;
		
		public MyIterator(){
			try {
				reader = new BufferedReader(new FileReader(file));
				line = new String[0];
				index = 1;
				line_count = 0;
				done = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		private void computeNext(){
			if(index > line.length){
				String raw_line = null;
				try {
					raw_line = reader.readLine();
					line_count++;
					index = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(raw_line != null){
					line = raw_line.trim().split("\\s+");
				}else{
					done = true;
				}
			}
		}

		public boolean hasNext() {
			// TODO Auto-generated method stub
			/*
			if(index <= line.length()){
				return true;
			}else{
				try {
					line = reader.readLine();
					index = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return line != null;
			*/
			computeNext();
			return !done;
		}


		public String next() {
			/*
			String token = "";
			if(index > line.length()){
				try {
					line = reader.readLine();
					index = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(index == line.length()){
				token = "§newline§";
			}else{
				char c = line.charAt(index);	
				if(Character.isWhitespace(c) && c != '\n'){
					token += "§whitespace§";
					//ignore any further whitespaces
					while(Character.isWhitespace(c) && c != '\n' && index < line.length()){
						index++;
						c = line.charAt(index);
					}
					index--;
				}else{
					token += c;
				}
			}
			index++;
			return token;
			*/
			computeNext();
			if(done){
				return null;
			}
			if(index == line.length){
				index++;
				return "§newline§";
			}else{
				return line[index++];
			}
		}

		public void remove() {
			//not supported
		}
		
	}
	
	public class MyTokenIterator implements Iterator<Token>{
		BufferedReader reader;
		String[] line;
		int index;
		int line_count;
		boolean done;
		
		public MyTokenIterator(){
			try {
				reader = new BufferedReader(new FileReader(file));
				line = new String[0];
				index = 1;
				line_count = 0;
				done = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		private void computeNext(){
			if(index > line.length){
				String raw_line = null;
				try {
					raw_line = reader.readLine();
					line_count++;
					index = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(raw_line != null){
					line = raw_line.trim().split("\\s+");
				}else{
					done = true;
				}
			}
		}

		public boolean hasNext() {
			// TODO Auto-generated method stub
			/*
			if(index <= line.length()){
				return true;
			}else{
				try {
					line = reader.readLine();
					index = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return line != null;
			*/
			computeNext();
			return !done;
		}

		public Token next() {
			computeNext();
			Token token = new Token();
			if(done){
				return null;
			}
			
			if(index == line.length){
				index++;
				token.value = "§newline§";
				token.line = line_count;
				token.pos--;
			}else{
				token.value = line[index++];
				token.line = line_count;
				token.pos--;
			}
			return token;
		}
		
		public Token getEOF(){
			Token t = new Token();
			t.value = "";
			t.line = line_count++;
			t.pos = 0;
			return t;
		}

		public void remove() {
			//not supported
		}
		
		public class Token{
			public String value;
			public int line;
			public int pos;
		}
		
	}

}
