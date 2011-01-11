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
	
	static final Pattern[] numbers = {Pattern.compile("[0-9]*"), Pattern.compile("[0-9]*")};

	public Lexer(Reader in) {
		input = new PushbackReader(in);
		token = "";
		tokenType = -1;
	}

	private void skipWhiteSpace() throws ParseException, IOException {
		char cc = (char)input.read();
		
		//key words and identifiers
		if(Character.isLetter(cc)){
			while(Character.isLetterOrDigit(cc)){
				token += cc;
				cc = (char)input.read();
			}
			tokenType = 1;
			input.unread(cc);
		}
		
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
			while(cc != '}'){
				if(!Character.isDefined(cc))
					throw new ParseException(1);
				token += cc;
				cc = (char)input.read();
			}
			nextLex();
		}
		
		//ints and floats
		else if(Character.isDigit(cc)){
			while(Character.isDigit(cc)){
				token += cc;
				cc = (char)input.read();
			}
			tokenType = 4;
			input.unread(cc);
		}
		
		//end of input
		else if(!Character.isDefined(cc)){
			token = "-1";
			tokenType = 7;
		}
		
		//end of input
		else if(Character.isWhitespace(cc)){
			nextLex();
		}
		
		//other symbols
		else{
			token = "" + cc;
			tokenType = 6;
		}		
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
