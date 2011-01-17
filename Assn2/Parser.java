/*
*	Sarah Cooley
*	CS480 - Winter2011
*	Assignment 2
*	Original Author: Tim Budd
*/

public class Parser {
	private Lexer lex;
	private boolean debug;

	public Parser (Lexer l, boolean d) { lex = l; debug = d; }

	public void parse () throws ParseException {
		lex.nextLex();
		program();
		if (lex.tokenCategory() != lex.endOfInput)
			parseError(3); // expecting end of file
	}

	private final void start (String n) {
		if (debug) System.out.println("start " + n + " token: " + lex.tokenText());
	}

	private final void stop (String n) {
		if (debug) System.out.println("recognized " + n + " token: " + lex.tokenText());
	}

	private void parseError(int number) throws ParseException {
		throw new ParseException(number);
	}

	private void program () throws ParseException {
		start("program");

		while (lex.tokenCategory() != Lexer.endOfInput) {
			declaration();
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}        
		stop("program");
	}

	private void declaration() throws ParseException {
		start("declaration");
		if(classDeclairation());
		else if(nonClassDeclairation());
	}
	
	private boolean nonClassDeclairation() throws ParseException {
		if(!functionDeclairation() && !nonFunctionDeclairation())
			return false;
		start("nonClassDeclairation");
		return true;
	}
	
	private boolean nonFunctionDeclairation() throws ParseException {
		if(!(varDeclairation() || constDeclairation() || typeDeclairation()))
			return false;
		start("nonFunctionDeclairation");
		return true;
	}
	
	private boolean varDeclairation() throws ParseException {
		if(!lex.match("var"))
			return false;
		start("varDeclairation");
		
		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		return true;
	}
	
	private boolean constDeclairation() throws ParseException {
		if(!lex.match("const"))
			return false;
		start("constDeclairation");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		return true;
	}
	
	private boolean typeDeclairation() throws ParseException {
		if(!lex.match("type"))
			return false;
		start("typeDeclairation");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		return true;
	}
	
	private boolean classDeclairation() throws ParseException {		
		if(!lex.match("class"))
			return false;
		start("classDeclairation");

		if(lex.tokenCategory() != 1)
			throw new ParseException();
		
		lex.nextLex();
		if(!classBody())
			throw new ParseException();
		
		return true;
	}
	
	private boolean classBody() throws ParseException {
		if(!lex.match("begin"))
			return false;
		start("classBody");

		lex.nextLex();
		while (lex.tokenCategory() != Lexer.endOfInput && !lex.match("end")) {
			if(!nonClassDeclairation());
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		
		if(!lex.match("end"))
			throw new ParseException();
		
		return true;
	}
	
	private boolean functionDeclairation() throws ParseException {
		if(!lex.match("function"))
			return false;
		start("functionDeclairation");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException();
		
		lex.nextLex();
		if(!arguments())
			throw new ParseException();
		
		lex.nextLex();
		if(!returnType())
			throw new ParseException();
		
		lex.nextLex();
		if(!functionBody())
			throw new ParseException();
		
		return true;
	}
	
	private boolean arguments() throws ParseException {		
		if(lex.tokenCategory() != 6 && !lex.match("("))
			return false;
		start("arguments");

		lex.nextLex();
		if(argumentList())
			throw new ParseException();
		
		lex.nextLex();
		if(lex.tokenCategory() != 6 && !lex.match("("))
			throw new ParseException();
		
		stop("arguments");
		return true;
	}
	
	private boolean argumentList() throws ParseException {		
		start("argumentList");

		while (lex.tokenCategory() != Lexer.endOfInput) {
			if (lex.match(","))
				lex.nextLex();
			else
				throw new ParseException(18);
			nameDeclairation();
		}        
		stop("program");
	}
	
	private boolean returnType() throws ParseException {		
		if(!lex.match("class"))
			return false;
		start("returnType");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException();
		
		lex.nextLex();
		if(!classBody())
			throw new ParseException();
		
		return true;
	}
	
	private boolean functionBody() throws ParseException {		
		if(!lex.match("class"))
			return false;
		start("functionBody");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException();
		
		lex.nextLex();
		if(!classBody())
			throw new ParseException();
		
		return true;
	}
}
