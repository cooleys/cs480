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
		if (lex.tokenCategory() != Lexer.endOfInput)
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
		else if(lex.match("var") || lex.match("const") || lex.match("type"))
			nonFunctionDeclaration();
		else
			throw new ParseException(26);
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
			throw new ParseException(26);

		stop("nonFunctionDeclaration");
	}

	private void varDeclaration() throws ParseException {
		start("variableDeclaration");
		if(!lex.match("var"))
			throw new ParseException(15);
		lex.nextLex();
		if(!lex.isIdentifier())
			throw new ParseException(27);
		nameDeclaration();

		stop("variableDeclaration");
	}

	private void constDeclaration() throws ParseException {
		start("constantDeclaration");
		if(!lex.match("const"))
			throw new ParseException(6);

		lex.nextLex();
		if(!lex.isIdentifier())
			throw new ParseException(27);

		lex.nextLex();
		if(!lex.match("="))
			throw new ParseException(20);

		lex.nextLex();
		if(lex.tokenCategory() != Lexer.intToken ||
				lex.tokenCategory() != Lexer.realToken ||
				lex.tokenCategory() != Lexer.stringToken)
			throw new ParseException(31);

		lex.nextLex();
		stop("constantDeclaration");
	}

	private void typeDeclaration() throws ParseException {
		start("typeDeclaration");
		if(!lex.match("type"))
			throw new ParseException(14);
		lex.nextLex();
		if(!lex.isIdentifier())
			throw new ParseException(27);
		nameDeclaration();

		stop("typeDeclaration");
	}

	private void nameDeclaration() throws ParseException {
		start("nameDeclaration");
		if(!lex.isIdentifier())
			throw new ParseException(27);

		lex.nextLex();		
		if(!lex.match(":"))
			throw new ParseException(19);

		lex.nextLex();
		type();

		stop("nameDeclaration");
	}

	private void classDeclaration() throws ParseException {		
		start("classDeclaration");
		if(!lex.match("class"))
			throw new ParseException(5);

		lex.nextLex();
		if(!lex.isIdentifier())
			throw new ParseException(27);

		lex.nextLex();

		classBody();

		stop("classDeclaration");
	}

	private void classBody() throws ParseException {
		start("classBody");
		if(!lex.match("begin"))
			throw new ParseException(4);

		lex.nextLex();
		while (lex.match("function") || lex.match("var") || lex.match("const")
				|| lex.match("type")) {
			nonClassDeclaration();
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}

		if(!lex.match("end"))
			throw new ParseException();
		lex.nextLex();

		stop("classBody");
	}

	private void functionDeclaration() throws ParseException {
		start("functionDeclaration");
		if(!lex.match("function"))
			throw new ParseException(10);
		lex.nextLex();

		if(!lex.isIdentifier())
			throw new ParseException(27);
		lex.nextLex();

		arguments();
		returnType();
		functionBody();

		stop("functionDeclaration");
	}

	private void arguments() throws ParseException {		
		start("arguments");
		if(!lex.match("("))
			throw new ParseException(21);
		lex.nextLex();

		argumentList();

		if(!lex.match(")"))
			throw new ParseException(22);
		lex.nextLex();

		stop("arguments");
	}

	private void argumentList() throws ParseException {		
		start("argumentList");

		while (lex.isIdentifier()) {
			nameDeclaration();
			if (lex.match(","))
				lex.nextLex();
			else if(lex.match(")"));
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

		if(lex.match("[")){
			lex.nextLex();

			if(lex.tokenCategory() != Lexer.intToken)
				throw new ParseException(32);
			lex.nextLex();

			if(!lex.match(":"))
				throw new ParseException(19);
			lex.nextLex();

			if(lex.tokenCategory() != Lexer.intToken)
				throw new ParseException(32);
			lex.nextLex();

			if(!lex.match("]"))
				throw new ParseException(24);
			lex.nextLex();

			type();
		}
		else if(lex.match("^")){
			lex.nextLex();
			type();
		}
		else if(!lex.isIdentifier())
			throw new ParseException(30);
		else
			lex.nextLex();

		stop("type");
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
		stop("functionBody");
	}

	private void compoundStatement() throws ParseException {		
		start("compoundStatement");
		if(!lex.match("begin"))
			throw new ParseException(4);
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
			throw new ParseException(8);
		lex.nextLex();

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
			throw new ParseException(34);

		stop("statement");
	}

	private void returnStatement() throws ParseException {		
		start("returnStatement");
		if(!lex.match("return"))
			throw new ParseException(12);

		lex.nextLex();
		if(lex.match("(")){
			lex.nextLex();
			expression();
			if(!lex.match(")"))
				throw new ParseException(22);
			lex.nextLex();
		}

		stop("returnStatement");
	}

	private void ifStatement() throws ParseException {		
		start("ifStatement");
		if(!lex.match("if"))
			throw new ParseException(11);
		lex.nextLex();

		expression();

		if(!lex.match("then"))
			throw new ParseException(13);
		lex.nextLex();

		statement();

		if(lex.match("else")){
			lex.nextLex();
			statement();
		}

		stop("ifStatement");
	}

	private void whileStatement() throws ParseException {		
		start("whileStatement");
		if(!lex.match("while"))
			throw new ParseException(16);
		lex.nextLex();

		expression();

		if(!lex.match("do"))
			throw new ParseException(7);
		lex.nextLex();

		statement();

		stop("whileStatement");
	}

	private void assignOrFunction() throws ParseException {		
		start("assignOrFunction");
		reference();

		if(lex.match("=")){
			lex.nextLex();
			expression();
		}
		else if(lex.match("(")){
			lex.nextLex();
			parameterList();
			if(!lex.match(")"))
				throw new ParseException(22);
			lex.nextLex();
		}
		else
			throw new ParseException(33);

		stop("assignOrFunction");
	}

	private void parameterList() throws ParseException {		
		start("parameterList");
	
		while (!lex.match(")")) {
			expression();
			if (lex.match(","))
				lex.nextLex();
			else if(!lex.match(")"))
				throw new ParseException(33);
		}

		stop("parameterList");
	}

	private void expression() throws ParseException {		
		start("expression");
		relExpression();

		while (lex.match("and") || lex.match("or")) {
			lex.nextLex();
			relExpression();
		}

		stop("expression");
	}

	private void relExpression() throws ParseException {		
		start("relExpression");
		plusExpression();

		if(lex.match("<") || lex.match("<=") || lex.match("!=") ||
				lex.match("==") || lex.match(">=") || lex.match(">")) {
			lex.nextLex();
			plusExpression();
		}

		stop("relExpression");
	}

	private void plusExpression() throws ParseException {		
		start("plusExpression");
		timesExpression();

		while (lex.match("+") || lex.match("-") || lex.match("<<")) {
			lex.nextLex();
			timesExpression();
		}

		stop("plusExpression");
	}

	private void timesExpression() throws ParseException {		
		start("timesExpression");
		term();

		while (lex.match("*") || lex.match("/") || lex.match("%")) {
			lex.nextLex();
			term();
		}

		stop("timesExpression");
	}

	private void term() throws ParseException {		
		start("term");

		if(lex.match("(")){
			lex.nextLex();
			expression();
			if(!lex.match(")"))
				throw new ParseException(22);	
			lex.nextLex();
		}
		else if(lex.match("not")){
			lex.nextLex();
			term();
		}
		else if(lex.match("new")){
			lex.nextLex();
			type();
		}
		else if(lex.match("-")){
			lex.nextLex();
			term();
		}
		else if(lex.match("&")){
			lex.nextLex();
			reference();
		}
		else if(lex.isIdentifier()){
			reference();
			if(lex.match("(")){
				lex.nextLex();
				parameterList();
				if(!lex.match(")"))
					throw new ParseException(22);
				lex.nextLex();
			}
		}
		else if(lex.tokenCategory() == Lexer.intToken ||
				lex.tokenCategory() == Lexer.realToken ||
				lex.tokenCategory() == Lexer.stringToken){
			lex.nextLex();
		}
		else
			throw new ParseException(33);

		stop("term");
	}

	private void reference() throws ParseException {		
		start("reference");

		if(!lex.isIdentifier())
			throw new ParseException(27);
		lex.nextLex();

		while( lex.match("^") || lex.match(".") || lex.match("[") ) {
			if ( lex.match(".") ) {
				lex.nextLex();
				if( !lex.isIdentifier() ) {
					throw new ParseException(27);
				}
			}
			else if (lex.match("[") ) {
				lex.nextLex();
				expression();
				if( !lex.match("]") ) {
					throw new ParseException();
				}
			}
			lex.nextLex();
		}

		stop("reference");
	}
}
