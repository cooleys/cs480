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
		if(lex.match("class"))
			classDeclaration();
		else if(lex.match("function") || lex.match("var") || lex.match("const")
				|| lex.match("type"))
			 nonClassDeclaration();
		else
			throw new ParseException(26);
		
		stop("declaration");
	}
	
	private void nonClassDeclaration() throws ParseException {
		start("nonClassDeclaration");
		if(lex.match("function"))
			functionDeclaration();
		if(lex.match("var") || lex.match("const") || lex.match("type"))
			nonFunctionDeclaration();
		stop("nonClassDeclaration");
	}
	
	private void nonFunctionDeclaration() throws ParseException {
		start("nonFunctionDeclaration");
		if(lex.match("var"))
			varDeclaration();
		else if(lex.match("const"))
			constDeclaration();
		else if(lex.match("type"))
			typeDeclaration();
		else
			throw new ParseException();
		
		stop("nonFunctionDeclaration");
	}
	
	private void varDeclaration() throws ParseException {
		start("varDeclaration");
		
		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		stop("varDeclaration");
	}
	
	private void constDeclaration() throws ParseException {
		start("constDeclaration");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		stop("constDeclaration");
	}
	
	private void typeDeclaration() throws ParseException {
		start("typeDeclaration");

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException(27);
		
		stop("typeDeclaration");
	}
	
	private void nameDeclaration() throws ParseException {
		start("nameDeclaration");
		if(!lex.isIdentifier())
			throw new ParseException();

		lex.nextLex();
		if(!lex.match(":"))
			throw new ParseException(19);
	
		lex.nextLex();
		if(lex.match("type"))
			type();
		else
			throw new ParseException(19);
		
		stop("nameDeclaration");
	}
	
	private void classDeclaration() throws ParseException {		
		start("classDeclaration");
		if(!lex.match("class"))
			throw new ParseException();
		
		lex.nextLex();
		if(!lex.isIdentifier())
			throw new ParseException();
		
		lex.nextLex();
		classBody();
		
		stop("classDeclaration");
	}
	
	private void classBody() throws ParseException {
		start("classBody");
		if(!lex.match("begin"))
			throw new ParseException();
		
		lex.nextLex();
		while (lex.tokenCategory() != Lexer.endOfInput && !lex.match("end")) {
			nonClassDeclaration();
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		
		if(!lex.match("end"))
			throw new ParseException();
	}
	
	private void functionDeclaration() throws ParseException {
		start("functionDeclaration");
		if(!lex.match("function"))
			throw new ParseException();
		
		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException();
		
		lex.nextLex();
		arguments();
		
		returnType();
		
		functionBody();
		
		stop("functionDeclaration");
	}
	
	private void arguments() throws ParseException {		
		start("arguments");
		if(!lex.match("("))
			throw new ParseException();

		lex.nextLex();
		argumentList();
			
		if(lex.tokenCategory() != 6 && !lex.match(")"))
			throw new ParseException();
		
		lex.nextLex();
		stop("arguments");
	}
	
	private void argumentList() throws ParseException {		
		start("argumentList");
		
		while (lex.isIdentifier()) {
			nameDeclaration();
			if (lex.match(","))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		
		stop("argumentList");
	}
	
	private void returnType() throws ParseException {		
		start("returnType");
		
		if(lex.match(":")){
			lex.nextLex();
			type();
		}
		
		stop("returnType");
	}
	
	private void type() throws ParseException {		
		start("type");
		if(!lex.match("class"))

		lex.nextLex();
		if(lex.tokenCategory() != 1)
			throw new ParseException();	
	}
	
	private void functionBody() throws ParseException {		
		start("functionBody");

		while (lex.match("function") || lex.match("var") || lex.match("const")
				|| lex.match("type")) {
			nonClassDeclaration();
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		
		compoundStatement();
	}
	
	private void compoundStatement() throws ParseException {		
		start("Compound Statement");
		if(!lex.match("begin"))
			throw new ParseException();
		
		lex.nextLex();
		while (lex.match("begin") || lex.match("if") || lex.match("while") ||
				lex.match("return") || lex.isIdentifier()) {
			statement();
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		
		if(!lex.match("end"))
			throw new ParseException();
		
		stop("compoundStatement");
	}
	
	private void statement() throws ParseException {		
		start("statement");
		if(lex.match("begin"))
			compoundStatement();
		else if(lex.match("if"))
			ifStatement();
		else if(lex.match("while"))
			whileStatement();
		else if(lex.match("return"))
			returnStatement();
		else if(lex.isIdentifier())
			assignOrFunction();
		else
			throw new ParseException();
		
		lex.nextLex();
		stop("statement");
	}
	
	private void returnStatement() throws ParseException {		
		start("returnStatement");
		if(!lex.match("return"))
			throw new ParseException();
		
		lex.nextLex();
		if(lex.match("("));
			//expression();
		
		stop("returnStatement");
	}
	
	private void ifStatement() throws ParseException {		
		start("ifStatement");
		if(!lex.match("if"))
			throw new ParseException();
		
		lex.nextLex();
		//expression();
		
		lex.nextLex();
		if(!lex.match("then"))
			throw new ParseException();
		
		lex.nextLex();
		statement();
		
		lex.nextLex();
		if(lex.match("else")){
			lex.nextLex();
			statement();
		}
		
		stop("ifStatement");
	}
	
	private void whileStatement() throws ParseException {		
		start("whileStatement");
		if(!lex.match("while"))
			throw new ParseException();
		
		lex.nextLex();
		//expression();
		
		lex.nextLex();
		if(!lex.match("do"))
			throw new ParseException();
		
		lex.nextLex();
		statement();
		
		stop("whileStatement");
	}
	
	private void assignOrFunction() throws ParseException {		
		start("assignOrFunction");
		if(!lex.isIdentifier())
			throw new ParseException();
		
		lex.nextLex();
		if(lex.match("="));
			//expression();
		else if(lex.match("("));
			//parameterList();
		
		statement();
		
		stop("assignOrFunction");
	}
}
