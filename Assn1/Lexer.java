/*
*	Sarah Cooley
*	CS480 - Winter 2011
*	Assignment1
*	Starter code by: Tim Budd
*/

import java.io.*;
import java.util.regex.Pattern;

public class Lexer {
	private PushbackReader input;
	private String token;
	private int tokenType;
	
	static final int identifierToken = 1;
	static final int keywordToken = 2;
	static final int intToken = 3;
	static final int realToken = 4;
	static final int stringToken = 5;
	static final int otherToken = 6;
	static final int endOfInput = 7;
	
	static final String[] keywords = {"const", "type", "var", "class", "begin", 
		"end", "function", "return", "if", "then", "while", "do", "not", "new"};
	
	public Lexer(Reader in) {
		input = new PushbackReader(in);
	}

	private void skipWhiteSpace() throws ParseException, IOException {
		char cc = (char)input.read();
		
		//key words and identifiers
		if(Character.isLetter(cc))
			words(cc);
		
		//ints and floats
		else if(Character.isDigit(cc))
			num(cc);

		//end of input
		else if(!Character.isDefined(cc)){
			token = "-1";
			tokenType = 7;
		}
		
		//end of input
		else if(Character.isWhitespace(cc))
			nextLex();
		
		//literal strings
		else if(cc == '"'){
			cc = (char)input.read();
			while(cc != '"'){
				if(!Character.isDefined(cc))
					throw new ParseException(2);
				token += cc;
				cc = (char)input.read();
			}
			tokenType = 5;
		}
		
		//comments
		else if(cc == '{'){
			do{
				cc = (char)input.read();
				if(!Character.isDefined(cc) || cc == '{')
					throw new ParseException(1);
			}while(cc != '}');
			nextLex();
		}
		
		//other symbols
		else
			symbols(cc);
	}
	
	private void words(char cc) throws IOException{
		do{
			token += cc;
			cc = (char)input.read();
		}while(Character.isLetterOrDigit(cc));
		
		tokenType = 1;
		input.unread(cc);
		
		for(String t: keywords)
			if(t.equals(token))
				tokenType=2;
	}
	
	private void num(char cc) throws IOException{
		do{
			token += cc;
			cc = (char)input.read();
		}while(Character.isDigit(cc) || (cc == '.' && token.indexOf('.') == -1));
		
		if(token.indexOf('.') == -1)
			tokenType = 3;
		else
			tokenType = 4;
		
		input.unread(cc);
	}
	
	private void symbols(char cc) throws IOException{
		token = "" + cc;
		if(cc == '<'){
			cc = (char)input.read();
			if(cc == '=' || cc == '<')
				token += cc;
			else
				input.unread(cc);
		}
		else if(cc == '>'){
			cc = (char)input.read();
			if(cc == '=' || cc == '>')
				token += cc;
			else
				input.unread(cc);
		}
		else if(cc == '=' || (cc == '!')){
			cc = (char)input.read();
			if(cc == '=')
				token += cc;
			else
				input.unread(cc);
		}
			
		tokenType = 6;
	}

	public void nextLex() throws ParseException {
		token = "";
		tokenType = -1;
		try {
			skipWhiteSpace();
		} catch (IOException e) {
			throw new ParseException(0);
		}
	}

	public String tokenText() {
		return token;
	}

	public int tokenCategory() {
		return tokenType;
	}

	public boolean isIdentifier() {
		return tokenType == identifierToken;
	}

	public boolean match (String test) {
		return test.equals(token);
	}
}
