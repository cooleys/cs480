/*
 *  Sarah Cooley
 *  CS480 - Winter2011
 *  Assignment 5
 *  Original Author: Tim Budd
 */


import java.util.Vector;

abstract class Ast {
	public Ast(Type t) { type = t; }

	public Type type;

	abstract public void genCode ();

	public Ast optimize () { return this; }

	protected boolean isInt(){
		return(this instanceof IntegerNode);
	}

	protected int intVal(){
		return(((IntegerNode)this).val);
	}

	public void branchIfTrue (Label lab) throws ParseException {
		genCode();
		System.out.println("Branch if True " + lab);
	}

	public void branchIfFalse (Label lab) throws ParseException { 
		genCode();
		System.out.println("Branch if False " + lab);
	}
}

class GlobalNode extends Ast {
	public GlobalNode (Type t, String n) { super(t); name = n;}

	public String name;

	public String toString() { return "global node " + name; }

	public void genCode() {
		System.out.println("Global " + name + " " + type);
	}
}

class IntegerNode extends Ast {
	public int val;

	public IntegerNode (int v) 
	{ super(PrimitiveType.IntegerType); val = v; }
	public IntegerNode (Integer v) 
	{ super(PrimitiveType.IntegerType); val = v.intValue(); }

	public String toString() { return "Integer " + val; }

	public void genCode() {
		System.out.println("Integer " + val);
	}
}

class RealNode extends Ast {
	private double val;

	public RealNode (double v) 
	{ super(PrimitiveType.RealType); val = v; }
	public RealNode (Double v) 
	{ super(PrimitiveType.RealType); val = v.doubleValue(); }


	public String toString() { return "real " + val; }

	public void genCode() {
		System.out.println("Real " + val);
	}
}

class StringNode extends Ast {
	private String val;

	public StringNode (String v) 
	{ super(new StringType(v)); val = v; }

	public String toString() { return "string " + val; }

	public void genCode() {
		System.out.println("String " + val); 
	}
}

class FramePointer extends Ast {
	public FramePointer () { super(PrimitiveType.VoidType); }

	public void genCode () {
		System.out.println("frame pointer");
	}

	public String toString() { return "frame pointer"; }
}

class UnaryNode extends Ast {
	static final int dereference = 1;
	static final int convertToReal = 2;
	static final int notOp = 3;
	static final int negation = 4;
	static final int newOp = 5;

	public int nodeType;
	public Ast child;

	public UnaryNode (int nt, Type t, Ast b) { 
		super(t); 
		nodeType = nt;
		child = b;
	}

	public Ast optimize () { 
		UnaryNode node = this;		
		node.child = node.child.optimize();

		if(node.nodeType == UnaryNode.negation) {
			if(node.child.type.equals(PrimitiveType.IntegerType)) {
				((IntegerNode)(node.child)).val =  ((node.child).intVal())*(-1);
				return new IntegerNode((node.child).intVal());
			}
		}
		return node;
	}

	public String toString() { return "Unary node " + nodeType +
		"(" + child + ")" + type; }

	public void genCode () {
		child.genCode();
		switch(nodeType) {
		case dereference:
			System.out.println("dereference " + type); break;
		case convertToReal:
			System.out.println("convert to real" + type); break;
		case notOp:
			System.out.println("not op " + type); break;
		case negation:
			System.out.println("numeric negation " + type); break;
		case newOp:
			System.out.println("new memory " + type); break;
		}
	}
}

class BinaryNode extends Ast {
	static final int plus = 1;
	static final int minus = 2;
	static final int times = 3;
	static final int divide = 4;
	static final int and = 5;
	static final int or = 6;
	static final int less = 7;
	static final int lessEqual = 8;
	static final int equal = 9;
	static final int notEqual = 10;
	static final int greater = 11;
	static final int greaterEqual = 12;
	static final int leftShift = 13;
	static final int remainder = 14;

	public BinaryNode (int nt, Type t, Ast l, Ast r) { 
		super(t); 
		NodeType = nt;
		LeftChild = l;
		RightChild = r;
	}

	public Ast optimize () { 
		BinaryNode node = this;		

		node.LeftChild = node.LeftChild.optimize();
		node.RightChild = node.RightChild.optimize();

		//c+c, t+0, c+t, (t+c)+t, (t+c)+t2, t+(t2+c)
		if(node.NodeType == BinaryNode.plus) {
			//c+t -> t+c
			if(node.LeftChild.isInt() && (!node.RightChild.isInt())) {
				Ast c = node.LeftChild;
				node.LeftChild = node.RightChild;
				node.RightChild = c;
			}

			//c+c -> c
			if(node.LeftChild.isInt() && node.RightChild.isInt()){
				return(new IntegerNode(node.LeftChild.intVal() + node.RightChild.intVal()));
			}

			//t+0 -> t
			if(node.RightChild.isInt() && node.RightChild.intVal() == 0) {
				node.LeftChild.type = node.type;
				return node.LeftChild;
			}

			//(t + c) + c -> (t + c2)
			if(node.LeftChild instanceof BinaryNode && ((BinaryNode)node.LeftChild).NodeType == BinaryNode.plus
					  && ((BinaryNode)node.LeftChild).RightChild.isInt() && node.RightChild.isInt()){
				IntegerNode i = new IntegerNode(((BinaryNode)node.LeftChild).RightChild.intVal() + node.RightChild.intVal());
				node.LeftChild = ((BinaryNode)node.LeftChild).LeftChild;
				node.RightChild = i;
			}
			
			//(t + c) + t2 -> (t + t2) + c
			if(node.LeftChild instanceof BinaryNode && ((BinaryNode)node.LeftChild).NodeType == BinaryNode.plus
					  && ((BinaryNode)node.LeftChild).RightChild.isInt() && (! node.RightChild.isInt())){
				Ast c = ((BinaryNode)node.LeftChild).RightChild;
				((BinaryNode)node.LeftChild).RightChild = node.RightChild;
				node.RightChild = c;
				node.LeftChild.type = node.type;
				return node.optimize();
			}
			
			//t+(t2+c) -> (t+t2)+c
			if(node.RightChild instanceof BinaryNode
					  && ((BinaryNode)node.RightChild).NodeType == BinaryNode.plus
					  && ((BinaryNode)node.RightChild).RightChild.isInt() ){
				Ast t = node.LeftChild;
				Ast t2 = ((BinaryNode)node.RightChild).LeftChild;
				Ast c = ((BinaryNode)node.RightChild).RightChild;
				node.LeftChild = node.RightChild;
				((BinaryNode)node.LeftChild).LeftChild = t;
				((BinaryNode)node.LeftChild).RightChild = t2;
				node.RightChild = c;
				node.LeftChild.type = node.type;
				return node.optimize();
			}
		}

		//t-c
		if(node.NodeType == BinaryNode.minus){
			//t-c -> t+(-c)
			if(node.RightChild.isInt()) {
				node.NodeType = BinaryNode.plus;
				((IntegerNode)node.RightChild).val = node.RightChild.intVal()*(-1);
				return node.optimize();
			}
		}

		//t*0, t*1, c*c, (t+c1)*c2
		if(node.NodeType == BinaryNode.times){
			//t*0 -> 0
			if(node.RightChild.isInt() && node.RightChild.intVal() == 0) {
				return new IntegerNode(0);
			}

			//t*1 -> t
			if(node.RightChild.isInt() && node.RightChild.intVal() == 1) {
				node.LeftChild.type = PrimitiveType.IntegerType;
				return node.LeftChild;
			}

			//c*c -> c
			if(node.RightChild.isInt() && node.LeftChild.isInt()){
				return(new IntegerNode(node.RightChild.intVal()*node.LeftChild.intVal()));
			}

			//(t+c1)*c2 -> t*c2+c1*c2
			if(node.RightChild.isInt() && (node.LeftChild instanceof BinaryNode)
					&& ((BinaryNode)node.LeftChild).NodeType == BinaryNode.plus){
				IntegerNode c = new IntegerNode(((BinaryNode)node.LeftChild).RightChild.intVal()*node.RightChild.intVal());
				((BinaryNode)node.LeftChild).NodeType = BinaryNode.times;
				((BinaryNode)node.LeftChild).RightChild = node.RightChild;
				node.NodeType = BinaryNode.plus;
				node.RightChild = c;
				return node.optimize();
			}
		}

		return node;
	}

	public String toString() { return "Binary Node " + NodeType +
		"(" + LeftChild + "," + RightChild + ")" + type; }

	public void genCode () {
		LeftChild.genCode();
		RightChild.genCode();
		switch (NodeType) {
		case plus: 
			System.out.println("do addition " + type); break;
		case minus: 
			System.out.println("do subtraction " + type); break;
		case leftShift: 
			System.out.println("do left shift " + type); break;
		case times: 
			System.out.println("do multiplication " + type); break;
		case divide: 
			System.out.println("do division " + type); break;
		case remainder:
			System.out.println("do remainder " + type); break;
		case and: 
			System.out.println("do and " + type); break;
		case or: 
			System.out.println("do or " + type); break;
		case less: 
			System.out.println("compare less " + type); break;
		case lessEqual: 
			System.out.println("compare less or equal" + type); break;
		case equal: 
			System.out.println("compare equal " + type); break;
		case notEqual: 
			System.out.println("compare notEqual " + type); break;
		case greater: 
			System.out.println("compare greater " + type); break;
		case greaterEqual: 
			System.out.println("compare greaterEqual " + type); break;
		}
	}

	public int NodeType;
	public Ast LeftChild;
	public Ast RightChild;
}

class FunctionCallNode extends Ast {
	private Ast fun;
	protected Vector args;

	public FunctionCallNode (Ast f, Vector a) {
		super (((FunctionType) f.type).returnType);
		fun = f;
		args = a;
	}

	public Ast optimize () { 
		FunctionCallNode node = this;
		for(int i = 0; i < node.args.size(); i++) {
			node.args.setElementAt(((Ast)node.args.elementAt(i)).optimize(), i);
		}		
		return node;
	}

	public String toString() { return "Function Call Node"; }

	public void genCode () {
		int i = args.size();
		while (--i >= 0) {
			Ast arg = (Ast) args.elementAt(i);
			arg.genCode();
			System.out.println("push argument " + arg.type);
		}

		fun.genCode();
		System.out.println("function call " + type);
	}
}
