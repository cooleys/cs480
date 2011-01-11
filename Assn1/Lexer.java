/*
*	Sarah Cooley
*	CS480 - Winter 2011
*	Assignment1
*	Starter code by: Tim Budd
*/

import java.io.*;

public class Lexer {
	private PushbackReader input;
	private String token;
	private int tokenType;

	public Lexer(Reader in) {
		input = new PushbackReader(in);
	}

	private void skipWhiteSpace() throws ParseException, IOException {
		char cc = (char)input.read();
		String tempTok="";
		
		//key words and identifiers
		if(Character.isLetter(cc)){
			while(Character.isLetterOrDigit(cc)){
				tempTok += cc;
				cc = (char)input.read();
			}
		}
		
		input.unread(cc);
	}

	public void nextLex() throws ParseException {
		try {
			skipWhiteSpace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isValid(char c){
		Character.isLetterOrDigit(c);
		return true;
	}

	static final int identifierToken = 1;
	static final int keywordToken = 2;
	static final int intToken = 3;
	static final int realToken = 4;
	static final int stringToken = 5;
	static final int otherToken = 6;
	static final int endOfInput = 7;

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
