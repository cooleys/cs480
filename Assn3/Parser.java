/*
 *	Sarah Cooley
 *	CS480 - Winter2011
 *	Assignment 3
 *	Original Author: Tim Budd
 */

public class Parser {
	private Lexer lex;
	private boolean debug;

	public Parser (Lexer l, boolean d) { lex = l; debug = d; }

	public void parse () throws ParseException {
		SymbolTable sym = new GlobalSymbolTable();
		sym.enterType("int", PrimitiveType.IntegerType);
		sym.enterType("real", PrimitiveType.RealType);
		sym.enterFunction("printInt", new FunctionType(PrimitiveType.VoidType));
		sym.enterFunction("printReal", new FunctionType(PrimitiveType.VoidType));
		sym.enterFunction("printStr", new FunctionType(PrimitiveType.VoidType));
		lex.nextLex();
		program(sym);
		if (lex.tokenCategory() != Lexer.endOfInput)
			parseError(3); // expecting end of file
	}

	private final void start (String n) {
		if (debug) System.out.println("start " + n + 
				" token: " + lex.tokenText());
	}

	private final void stop (String n) {
		if (debug) System.out.println("recognized " + n + 
				" token: " + lex.tokenText());
	}

	private void parseError(int number) throws ParseException {
		throw new ParseException(number);
	}

	private void program (SymbolTable sym) throws ParseException {
		start("program");

		while (lex.tokenCategory() != Lexer.endOfInput) {
			declaration(sym);
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		stop("program");
	}

	private void declaration (SymbolTable sym) throws ParseException {
		start("declaration");
		if (lex.match("class"))
			classDeclaration(sym);
		else if (lex.match("function") || lex.match("const") 
				|| lex.match("var") || lex.match("type"))
			nonClassDeclaration(sym);
		else 
			parseError(26);
		stop("declaration");
	}

	private void nonClassDeclaration (SymbolTable sym) throws ParseException {
		start("nonClassDeclaration");
		if (lex.match("function"))
			functionDeclaration(sym);
		else if (lex.match("const") || lex.match("var") 
				|| lex.match("type"))
			nonFunctionDeclaration(sym);
		else
			parseError(26);
		stop("nonClassDeclaration");
	}

	private void nonFunctionDeclaration (SymbolTable sym) throws ParseException {
		start("nonFunctionDeclaration");
		if (lex.match("var"))
			variableDeclaration(sym);
		else if (lex.match("const"))
			constantDeclaration(sym);
		else if (lex.match("type"))
			typeDeclaration(sym);
		else 
			parseError(26);
		stop("nonFunctionDeclaration");
	}

	private void constantDeclaration (SymbolTable sym) throws ParseException {
		start("constantDeclaration");
		if (lex.match("const")) {
			lex.nextLex();
			if (!lex.isIdentifier())
				parseError(27);
			if(sym.nameDefined(lex.tokenText()))
				throw new ParseException(35, lex.tokenText());
			String id = lex.tokenText();
			lex.nextLex();
			if (!lex.match("="))
				parseError(20);
			lex.nextLex();
			if (lex.tokenCategory() == Lexer.intToken)
				sym.enterConstant(id, new IntegerNode(new Integer(lex.tokenText())));
			else if (lex.tokenCategory() == Lexer.realToken)
				sym.enterConstant(id, new RealNode(new Double(lex.tokenText())));
			else if (lex.tokenCategory() == Lexer.stringToken)
				sym.enterConstant(id, new StringNode(lex.tokenText()));
			else
				parseError(31);
			lex.nextLex();
		}
		else
			parseError(6);
		stop("constantDeclaration");
	}

	private void typeDeclaration (SymbolTable sym) throws ParseException {
		start("typeDeclaration");
		if (lex.match("type")) {
			lex.nextLex();
			if (!lex.isIdentifier()) 
				parseError(27);
			String id = lex.tokenText();
			lex.nextLex();
			if (!lex.match(":"))
				parseError(19);
			lex.nextLex();
			Type t = type(sym);
			if(sym.nameDefined(id))
				throw new ParseException(35, lex.tokenText());
			sym.enterType(id, t);
		} else
			parseError(14); 
		stop("typeDeclaration");
	}

	private void variableDeclaration (SymbolTable sym) throws ParseException {
		start("variableDeclaration");
		if (lex.match("var")) {
			lex.nextLex();
			nameDeclaration(sym);
		}
		else
			parseError(15);
		stop("variableDeclaration");
	}

	private void nameDeclaration (SymbolTable sym) throws ParseException {
		start("nameDeclaration");
		if (! lex.isIdentifier()) 
			parseError(27);
		String id = lex.tokenText();
		if(sym.nameDefined(id))
			throw new ParseException(35, lex.tokenText());
		lex.nextLex();
		if (! lex.match(":"))
			parseError(19);
		lex.nextLex();
		sym.enterVariable(id, type(sym));
		stop("nameDeclaration");
	}

	private void classDeclaration(SymbolTable sym) throws ParseException {
		start("classDeclaration");
		if (! lex.match("class"))
			parseError(5);
		lex.nextLex();
		if (! lex.isIdentifier())
			parseError(27);
		ClassSymbolTable symc = new ClassSymbolTable(sym);
		sym.enterType(lex.tokenText(), new ClassType(symc));
		lex.nextLex();
		classBody(symc);
		stop("classDeclaration");
	}

	private void classBody(SymbolTable sym) throws ParseException {
		start("classBody");
		if (! lex.match("begin"))
			parseError(4);
		lex.nextLex();
		while (! lex.match("end")) {
			nonClassDeclaration(sym);
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		lex.nextLex();
		stop("classBody");
	}

	private void functionDeclaration(SymbolTable sym) throws ParseException {
		start("functionDeclaration");
		if (!lex.match("function"))
			parseError(10);
		lex.nextLex();
		FunctionSymbolTable symf = new FunctionSymbolTable(sym);
		
		if (!lex.isIdentifier())
			parseError(27);
		String fid = lex.tokenText();
		if(sym.nameDefined(fid))
			throw new ParseException(35, lex.tokenText());
		lex.nextLex();
		
		symf.doingArguments = true;
		arguments(symf);
		symf.doingArguments = false;
		sym.enterFunction(fid, new FunctionType(returnType(symf)));
		functionBody(symf, fid);
		stop("functionDeclaration");
	}

	private void arguments (SymbolTable sym) throws ParseException {
		start("arguments");
		if (! lex.match("("))
			parseError(21);
		lex.nextLex();
		argumentList(sym);
		if (! lex.match(")"))
			parseError(22);
		lex.nextLex();
		stop("arguments");
	}

	private void argumentList (SymbolTable sym) throws ParseException {
		start("argumentList");
		if (lex.isIdentifier()) {
			nameDeclaration(sym);
			while (lex.match(",")) {
				lex.nextLex();
				nameDeclaration(sym);
			}
		}
		stop("argumentList");
	}

	private Type returnType (SymbolTable sym) throws ParseException {
		start("returnType");
		Type t = PrimitiveType.VoidType;
		if (lex.match(":")) {
			lex.nextLex();
			t = type(sym);
		}

		stop("returnType");
		return t;
	}

	private Type type (SymbolTable sym) throws ParseException {
		start("type");
		Type result = null;

		if(lex.isIdentifier()) {
			result = sym.lookupType(lex.tokenText());
			lex.nextLex();
		} else if(lex.match("^")) {
			lex.nextLex();
			result = new PointerType(type(sym));
		} else if(lex.match("[")) {
			lex.nextLex();
			if (lex.tokenCategory() != Lexer.intToken)
				parseError(32);
			int lower = Integer.parseInt((lex.tokenText()));
			lex.nextLex();
			if (! lex.match(":"))
				parseError(19);
			lex.nextLex();
			if (lex.tokenCategory() != Lexer.intToken)
				parseError(32);
			int upper = Integer.parseInt((lex.tokenText()));
			lex.nextLex();
			if (! lex.match("]"))
				parseError(24);
			lex.nextLex();
			type(sym);
			result = new ArrayType(lower, upper, result);
		} else parseError(30);
		stop("type");
		return result;
	}

	private void functionBody (SymbolTable sym, String fid) throws ParseException {
		start("functionBody");
		while (! lex.match("begin")) {
			nonFunctionDeclaration(sym);
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		CodeGen.genProlog(fid, sym.size());
		compoundStatement(sym);
		CodeGen.genEpilog(fid);
		stop("functionBody");
	}

	private void compoundStatement (SymbolTable sym) throws ParseException {
		start("compoundStatement");
		if (! lex.match("begin"))
			parseError(4);
		lex.nextLex();
		while (! lex.match("end")) {
			statement(sym);
			if (lex.match(";"))
				lex.nextLex();
			else
				throw new ParseException(18);
		}
		lex.nextLex();
		stop("compoundStatement");
	}

	private void statement (SymbolTable sym) throws ParseException {
		start("statement");
		if (lex.match("return"))
			returnStatement(sym);
		else if (lex.match("if"))
			ifStatement(sym);
		else if (lex.match("while"))
			whileStatement(sym);
		else if (lex.match("begin"))
			compoundStatement(sym);
		else if (lex.isIdentifier())
			assignOrFunction(sym);
		else
			parseError(34);
		stop("statement");
	}

	private boolean firstExpression() {
		if (lex.match("(") || lex.match("not") || lex.match("-") || lex.match("&"))
			return true;
		if (lex.isIdentifier())
			return true;
		if ((lex.tokenCategory() == Lexer.intToken) ||
				(lex.tokenCategory() == Lexer.realToken) ||
				(lex.tokenCategory() == Lexer.stringToken))
			return true;
		return false;
	}

	private void returnStatement (SymbolTable sym) throws ParseException {
		start("returnStatement");
		if (! lex.match("return"))
			parseError(12);
		lex.nextLex();
		if (lex.match("(")) {
			lex.nextLex();
			expression(sym);
			if (! lex.match(")"))
				parseError(22);
			lex.nextLex();
		}
		stop("returnStatement");
	}

	private void ifStatement (SymbolTable sym) throws ParseException {
		start("ifStatement");
		if (! lex.match("if"))
			parseError(11);
		lex.nextLex();
		expression(sym);
		if (! lex.match("then"))
			throw new ParseException(13);
		else
			lex.nextLex();
		statement(sym);
		if (lex.match("else")) {
			lex.nextLex();
			statement(sym);
		}
		stop("ifStatement");
	}

	private void whileStatement (SymbolTable sym) throws ParseException {
		start("whileStatement");
		if (! lex.match("while"))
			parseError(16);
		lex.nextLex();
		expression(sym);
		if (! lex.match("do"))
			throw new ParseException(7);
		else
			lex.nextLex();
		statement(sym);
		stop("whileStatement");
	}

	private void assignOrFunction (SymbolTable sym) throws ParseException {
		start("assignOrFunction");
		Ast val = reference(sym);
		val.genCode();
		reference(sym);
		if (lex.match("=")) {
			lex.nextLex();
			expression(sym);
		}
		else if (lex.match("(")) {
			lex.nextLex();
			parameterList(sym);
			if (! lex.match(")"))
				parseError(22);
			lex.nextLex();
		}
		else
			parseError(20);
		stop("assignOrFunction");
	}

	private void parameterList (SymbolTable sym) throws ParseException {
		start("parameterList");
		if (firstExpression()) {
			expression(sym);
			while (lex.match(",")) {
				lex.nextLex();
				expression(sym);
			}
		}
		stop("parameterList");
	}

	private void expression (SymbolTable sym) throws ParseException {
		start("expression");
		relExpression(sym);
		while (lex.match("and") || lex.match("or")) {
			lex.nextLex();
			relExpression(sym);
		}
		stop("expression");
	}

	private boolean relOp(SymbolTable sym) {
		if (lex.match("<") || lex.match("<=") ||
				lex.match("==") || lex.match("!=") ||
				lex.match(">") || lex.match(">="))
			return true;
		return false;
	}

	private void relExpression (SymbolTable sym) throws ParseException {
		start("relExpression");
		plusExpression(sym);
		if (relOp(sym)) {
			lex.nextLex();
			plusExpression(sym);
		}
		stop("relExpression");
	}

	private void plusExpression (SymbolTable sym) throws ParseException {
		start("plusExpression");
		timesExpression(sym);
		while (lex.match("+") || lex.match("-") || lex.match("<<")) {
			lex.nextLex();
			timesExpression(sym);
		}
		stop("plusExpression");
	}

	private void timesExpression (SymbolTable sym) throws ParseException {
		start("timesExpression");
		term(sym);
		while (lex.match("*") || lex.match("/") || lex.match("%")) {
			lex.nextLex();
			term(sym);
		}
		stop("timesExpression");
	}

	private void term (SymbolTable sym) throws ParseException {
		start("term");
		if (lex.match("(")) {
			lex.nextLex();
			expression(sym);
			if (! lex.match(")"))
				parseError(22);
			lex.nextLex();
		}
		else if (lex.match("not")) {
			lex.nextLex();
			term(sym);
		}
		else if (lex.match("new")) {
			lex.nextLex();
			type(sym);
		}
		else if (lex.match("-")) {
			lex.nextLex();
			term(sym);
		}
		else if (lex.match("&")) {
			lex.nextLex();
			reference(sym);
		}
		else if (lex.tokenCategory() == Lexer.intToken) {
			lex.nextLex();
		}
		else if (lex.tokenCategory() == Lexer.realToken) {
			lex.nextLex();
		}
		else if (lex.tokenCategory() == Lexer.stringToken) {
			lex.nextLex();
		}
		else if (lex.isIdentifier()) {
			Ast val = reference(sym);
			val.genCode();
			reference(sym);
			if (lex.match("(")) {
				lex.nextLex();
				parameterList(sym);
				if (! lex.match(")"))
					parseError(22);
				lex.nextLex();
			}
		}
		else
			parseError(33);
		stop("term");
	}

	private Ast reference (SymbolTable sym) throws ParseException {
		start("reference");
		Ast result = null;
		
		if (!lex.isIdentifier())
			parseError(27);
		else
			result = sym.lookupName(new FramePointer(), lex.tokenText());
		
		lex.nextLex();
		while (lex.match("^") || lex.match(".") || lex.match("[")) {
			if (lex.match("^")) {
				lex.nextLex();
				Type btype = addressBaseType(result.type);
				if ( !(btype instanceof PointerType) )
					parseError(38);
				PointerType pb = (PointerType) btype;
				result = new UnaryNode(UnaryNode.dereference,new AddressType(pb.baseType), result);
			}
			else if (lex.match(".")) {
				lex.nextLex();
				if (!lex.isIdentifier())
					parseError(27);
				Type ctype = addressBaseType(result.type);
				if ( !(ctype instanceof ClassType) )
					parseError(39);
				ClassType ct = (ClassType) ctype;
				if(ct.symbolTable.nameDefined(lex.tokenText()) == false)
					parseError(29);
				result = ct.symbolTable.lookupName(result, lex.tokenText());
			}
			else {
				lex.nextLex();
				expression(sym); // assume recursive call returns 42
				Ast indexExpression = new IntegerNode(42); // for now
				if ( !(addressBaseType(result.type) instanceof ArrayType) )
					parseError(40);
				ArrayType atype = (ArrayType) addressBaseType(result.type);
				
				if (! indexExpression.type.equals(PrimitiveType.IntegerType))
					parseError(41);
				indexExpression = new BinaryNode( BinaryNode.minus, PrimitiveType.IntegerType,
						indexExpression, new IntegerNode(atype.lowerBound));
				BinaryNode bnode = new BinaryNode(BinaryNode.times, atype.elementType, indexExpression, new IntegerNode(atype.elementType.size()));
				result = new BinaryNode(BinaryNode.plus, new AddressType(atype.elementType), result, bnode);
				if (! lex.match("]"))
					parseError(24);
				lex.nextLex();
			}
		}
		stop("reference");
		return result;
	}
	
	private Type addressBaseType(Type t) throws ParseException {
		if (! (t instanceof AddressType))
			parseError(37);
		AddressType at = (AddressType) t;
		return at.baseType;
	}

}
