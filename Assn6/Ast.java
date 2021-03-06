/*
 *  Sarah Cooley
 *  CS480 - Winter2011
 *  Assignment 6
 *  Original Author: Tim Budd
 */

import java.util.Vector;

abstract class Ast {
	public Ast(Type t) { type = t; }

	public Type type;

    protected boolean isConstant() {
        Ast tree = this;
        if((tree.type.equals(PrimitiveType.IntegerType)) || (tree.type.equals(PrimitiveType.IntegerType)))
                return true;
        else
                return false;   
    }
	
    protected int getIntValue() {
        Ast tree = this;
        try {
                if( ! (tree.isConstant()))
                        throw new ParseException(32);
        } catch (ParseException e) {
                e.printStackTrace();
        }                       
        return ((IntegerNode)tree).val;
    }
    
    protected double getRealValue() {
        Ast tree = this;
        try {
                if( ! (tree.isConstant()))
                        throw new ParseException(32);
        } catch (ParseException e) {
                e.printStackTrace();
        }                       
        return ((RealNode)tree).val;
    }

    abstract public void genCode ();

	public Ast optimize () { return this; }

	public void branchIfTrue (Label lab) throws ParseException {
		if( !(this instanceof BinaryNode) || 
		  ((((BinaryNode)this).NodeType != BinaryNode.and) &&
		  (((BinaryNode)this).NodeType != BinaryNode.or)))
			genCode();
		if(this instanceof UnaryNode) {
			if(((UnaryNode)this).nodeType == UnaryNode.notOp) {
				((UnaryNode)this).child.branchIfFalse(lab);
			}
		}
		else if(this instanceof BinaryNode) {
			switch(((BinaryNode)this).NodeType) {
				case BinaryNode.and:
					Label l2 = new Label();
					((BinaryNode)this).LeftChild.branchIfFalse(l2);
					((BinaryNode)this).RightChild.branchIfTrue(lab);
					l2.genCode();
					break;
				case BinaryNode.or:
					Label l3 = new Label();
					((BinaryNode)this).LeftChild.branchIfTrue(lab);
					((BinaryNode)this).RightChild.branchIfFalse(l3);
					l3.genCode();
					break;
				case BinaryNode.less:
					BinaryNode.genComp("jl", lab);
					break;
				case BinaryNode.lessEqual:
					BinaryNode.genComp("jle", lab);
					break;
				case BinaryNode.equal:
					BinaryNode.genComp("je", lab);
					break;
				case BinaryNode.notEqual:
					BinaryNode.genComp("jne", lab);
					break;
				case BinaryNode.greater:
					BinaryNode.genComp("jg", lab);
					break;
				case BinaryNode.greaterEqual:
					BinaryNode.genComp("jge", lab);
					break;
			}
		}
	}

	public void branchIfFalse (Label lab) throws ParseException { 
		if( !(this instanceof BinaryNode) || 
		  ((((BinaryNode)this).NodeType != BinaryNode.and) &&
		  (((BinaryNode)this).NodeType != BinaryNode.or)))
					genCode();
		if(this instanceof UnaryNode) {
			if(((UnaryNode)this).nodeType == UnaryNode.notOp) {
				((UnaryNode)this).child.branchIfTrue(lab);
			}
		}
		else if(this instanceof BinaryNode) {
			switch(((BinaryNode)this).NodeType) {
				case BinaryNode.and:
					Label l4 = new Label();
					((BinaryNode)this).LeftChild.branchIfFalse(l4);
					((BinaryNode)this).RightChild.branchIfTrue(lab);
					l4.genCode();
					break;
				case BinaryNode.or:
					Label l5 = new Label();
					((BinaryNode)this).LeftChild.branchIfTrue(l5);
					((BinaryNode)this).RightChild.branchIfFalse(lab);
					l5.genCode();
					break;
				case BinaryNode.less:
					BinaryNode.genComp("jge", lab);
					break;
				case BinaryNode.lessEqual:
					BinaryNode.genComp("jg", lab);
					break;
				case BinaryNode.equal:
					BinaryNode.genComp("jne", lab);
					break;
				case BinaryNode.notEqual:
					BinaryNode.genComp("je", lab);
					break;
				case BinaryNode.greater:
					BinaryNode.genComp("jle", lab);
					break;
				case BinaryNode.greaterEqual:
					BinaryNode.genComp("jl", lab);
					break;
			}
		}
	}

	public boolean isInteger() { return false; }

	public int cValue() { return 0; }

	public BinaryNode isSum() { return null; }
}

class GlobalNode extends Ast {
	public GlobalNode (Type t, String n) { super(t); name = n;}

	public String name;

	public String toString() { return "global node " + name; }

	public void genCode() {
		CodeGen.gen("pushl", "$" + name);
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
		CodeGen.gen("pushl", "$" + String.valueOf(val));
	}

	public boolean isInteger() { return true; }

	public int cValue() { return val; }
}

class RealNode extends Ast {
	public double val;

	public RealNode (double v) 
		{ super(PrimitiveType.RealType); val = v; }
	public RealNode (Double v) 
		{ super(PrimitiveType.RealType); val = v.doubleValue(); }

	public String toString() { return "real " + val; }

	public void genCode() {
		Label lab = new Label();
		CodeGen.addConstant(lab, new Double(val));
		CodeGen.gen("flds", lab.toString());
		CodeGen.gen("subl", "$4", "%esp");
		CodeGen.gen("fstps", "0(%esp)");
	}
}

class StringNode extends Ast {
	private String val;

	public StringNode (String v) 
		{ super(new StringType(v)); val = v; }

	public String toString() { return "string " + val; }

	public void genCode() {
		Label lab = new Label();
		CodeGen.addConstant(lab, val);
		CodeGen.gen("pushl", "$" + lab.toString());
	}
}

class FramePointer extends Ast {
	public FramePointer () { super(PrimitiveType.VoidType); }

	public void genCode () {
		CodeGen.gen("pushl", "%ebp");
	}

	public String toString() { return "frame pointer"; }
}

class UnaryNode extends Ast {
	static final int dereference = 1;
	static final int convertToReal = 2;
	static final int notOp = 3;
	static final int negation = 4;
	static final int newOp = 5;


	public UnaryNode (int nt, Type t, Ast b) { 
		super(t); 
		nodeType = nt;
		child = b;
	}

	public int nodeType;
	public Ast child;

	public String toString() { return "Unary node " + nodeType +
		"(" + child + ")" + type; }

	public Ast optimize() {
		Ast newChild = child.optimize();
		if ((nodeType == negation) && newChild.isInteger())
			return new IntegerNode(- newChild.cValue());
		return new UnaryNode(nodeType, type, child.optimize());
		}

	public void genCode () {
		switch(nodeType) {
			case dereference:
				if((child instanceof BinaryNode) && (((BinaryNode)child).isSum() != null) &&
				 (((BinaryNode)child).LeftChild instanceof FramePointer) &&
				 (((BinaryNode)child).RightChild.isConstant()) ) {
					String offset = String.valueOf(((IntegerNode)((BinaryNode)child).RightChild).val);
					CodeGen.gen("pushl", offset + "(%ebp)");
				}
				else if(child instanceof GlobalNode) {
					CodeGen.gen("pushl", ((GlobalNode)child).name);
				}
				else {
					child.genCode();
					CodeGen.gen("popl",	"%eax");
					CodeGen.gen("pushl", "0(%eax)");
				}
				break;
			case convertToReal:
				child.genCode();
				CodeGen.gen("fildl", "0(%esp)");
				CodeGen.gen("fstps", "0(%esp)");
				break;
			case notOp:
				break;
			case negation:
				child.genCode();
				if(child.type.equals(PrimitiveType.RealType)) {
					CodeGen.gen("flds", "0(%esp)");
					CodeGen.gen("fchs");	
					CodeGen.gen("fstps", "0(%esp)");
				}
				else if(child.type.equals(PrimitiveType.IntegerType)) {
					CodeGen.gen("negl", "0(%esp)");
				}
				break;
			case newOp:
				child.genCode();
				CodeGen.gen("call", "malloc");
				CodeGen.gen("addl", "$4", "%esp");
				CodeGen.gen("pushl", "%eax");
				break;
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

	public BinaryNode isSum() { 
		if (NodeType == plus) return this;
		return null; 
	}

	public BinaryNode (int nt, Type t, Ast l, Ast r) { 
		super(t); 
		NodeType = nt;
		LeftChild = l;
		RightChild = r;
		}

	public String toString() { return "Binary Node " + NodeType +
		"(" + LeftChild + "," + RightChild + ")" + type; }

	public Ast optimize() {
//System.out.println("Optimizing " + toString());
		Ast newLeft = LeftChild.optimize();
//System.out.println("Left child is " + newLeft);
		Ast newRight = RightChild.optimize();
//System.out.println("Right child is " + newRight);
		switch (NodeType) {
			case plus:
				if (newRight.isInteger()) {
					int c = newRight.cValue();
					if (c == 0) {
						newLeft.type = type;
						return newLeft;
						}
					if (newLeft.isInteger())
						return new IntegerNode
						(c + newLeft.cValue());
					BinaryNode leftSum = newLeft.isSum();
					if ((leftSum != null) &&
						leftSum.RightChild.isInteger())
						return new BinaryNode(plus,
						type,
						leftSum.LeftChild,
						new IntegerNode(c +
						leftSum.RightChild.cValue())).optimize();
					}
				if (newLeft.isInteger())
					return new BinaryNode(plus, type,
						newRight, newLeft).optimize();
				BinaryNode LeftSum = newLeft.isSum();
				if ((LeftSum != null) && 
					LeftSum.RightChild.isInteger())
					return new BinaryNode(plus, type,
					new BinaryNode(plus, type,
					LeftSum.LeftChild, newRight),
					LeftSum.RightChild).optimize();
				BinaryNode rightSum = newRight.isSum();
				if ((rightSum != null) &&
					rightSum.RightChild.isInteger())
					return new BinaryNode(plus, type,
					new BinaryNode(plus, type,
					newLeft, rightSum.LeftChild),
					rightSum.RightChild).optimize();
					
			break;

			case minus:
				if (newRight.isInteger())
					return new BinaryNode(plus, type,
						newLeft, 
						new IntegerNode(- newRight.cValue())).optimize();
			break;

			case times:
				if (newRight.isInteger()) {
					int c = newRight.cValue();
					if (c == 0) {
						newRight.type = type;
						return newRight;
						}
					if (c == 1) {
						newLeft.type = type;
						return newLeft;
						}
					if (newLeft.isInteger())
						return new IntegerNode(
							c * newLeft.cValue());
					BinaryNode leftSum = newLeft.isSum();
					if ((leftSum != null) &&
						leftSum.RightChild.isInteger())
						return new BinaryNode(plus,
						type,
						new BinaryNode(times,
						leftSum.type,
						leftSum.LeftChild,
						newRight),
						new IntegerNode(c *
						leftSum.RightChild.cValue())).optimize();

				}
				break;
		}
		return new BinaryNode(NodeType, type, newLeft, newRight);
	}

	public void genCode () {
		switch (NodeType) {
			case plus: 
				if(this.type.equals(PrimitiveType.RealType)) {
					RightChild.genCode();
					LeftChild.genCode();
					CodeGen.gen("flds", "0(%esp)");
					CodeGen.gen("addl", "$4", "%esp");
					CodeGen.gen("fadds", "0(%esp)");
					CodeGen.gen("fstps", "0(%esp)");
				}
				else {
					LeftChild.genCode();
					if(RightChild.isInteger()) {
						CodeGen.gen("addl", "$" + RightChild.cValue(), "0(%esp)");
					}
					else {
						RightChild.genCode();
						CodeGen.gen("popl", "%eax");
						CodeGen.gen("addl",	"%eax", "0(%esp)");
					}
				}
				break;
			case minus:
				if(this.type.equals(PrimitiveType.RealType)) {
					RightChild.genCode();
					LeftChild.genCode();
					CodeGen.gen("flds", "0(%esp)");
					CodeGen.gen("addl",	"$4", "%esp");
					CodeGen.gen("fsubs", "0(%esp)");
					CodeGen.gen("fstps", "0(%esp)");
				}
				else {
					LeftChild.genCode();
					RightChild.genCode();
					CodeGen.gen("popl", "%eax");
					CodeGen.gen("subl", "%eax", "0(%esp)");
				}
				break;
			case leftShift:
				RightChild.genCode();
				LeftChild.genCode();
				CodeGen.gen("popl", "%eax");
				CodeGen.gen("popl", "%ecx");
				CodeGen.gen("sall", "%cl", "%eax");
				CodeGen.gen("pushl", "%eax");
				break;
			case times: 
				if(this.type.equals(PrimitiveType.RealType)) {
					RightChild.genCode();
					LeftChild.genCode();
					CodeGen.gen("flds", "0(%esp)");
					CodeGen.gen("addl", "$4", "%esp");
					CodeGen.gen("fmuls", "0(%esp)");
					CodeGen.gen("fstps", "0(%esp)");
				}
				else {
					LeftChild.genCode();
					RightChild.genCode();
					CodeGen.gen("popl", "%eax");
					CodeGen.gen("imull", "0(%esp)");
					CodeGen.gen("movl", "%eax", "0(%esp)");					
				}
				break;
			case divide: 
				if(this.type.equals(PrimitiveType.RealType)) {
					RightChild.genCode();
					LeftChild.genCode();
					CodeGen.gen("flds", "0(%esp)");
					CodeGen.gen("addl", "$4", "%esp");
					CodeGen.gen("fdivs", "0(%esp)");
					CodeGen.gen("fstps", "0(%esp)");
				}
				else {
					RightChild.genCode();
					LeftChild.genCode();
					CodeGen.gen("popl", "%eax");
					CodeGen.gen("popl", "%ecx");
					CodeGen.gen("cltd");
					CodeGen.gen("idivl", "%ecx");
					CodeGen.gen("pushl", "%eax");
				}
				break;
			case remainder:
				RightChild.genCode();
				LeftChild.genCode();
				CodeGen.gen("popl", "%eax");
				CodeGen.gen("popl",	"%ecx");
				CodeGen.gen("cltd");
				CodeGen.gen("idivl", "%ecx");
				CodeGen.gen("pushl", "%edx");
				break;
			case and: 
			case or:
			case less:
			case lessEqual:
			case equal:
			case notEqual:
			case greater:
			case greaterEqual:
				LeftChild.genCode();
				RightChild.genCode();
				break;
			}
		}
	
	static public void genComp(String operator, Label l) {
		CodeGen.gen("popl", "%eax");
		CodeGen.gen("popl", "%ecx");
		CodeGen.gen("cmpl", "%eax", "%ecx");
		CodeGen.gen(operator, l.toString() );
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

	public String toString() { return "Function Call Node"; }

	public Ast optimize() {
		Vector newArgs = new Vector();
		for (int i = 0; i < args.size(); i++) {
			Ast arg = (Ast) args.elementAt(i);
			newArgs.addElement(arg.optimize());
		}
		return new FunctionCallNode(fun, newArgs);
	}

	public void genCode () {
		int i = args.size();
		int totalSize = 0;
		while (--i >= 0) {
			Ast arg = (Ast) args.elementAt(i);
			arg.genCode();
			totalSize += arg.type.size();
		}

		CodeGen.gen("call", ((GlobalNode)fun).name);
		if(totalSize > 0)
			CodeGen.gen("addl",	"$" + String.valueOf(totalSize), "%esp");
		Type returnType = ((FunctionType)(this.fun.type)).returnType;
		if( returnType.size() != 0) {
			if(returnType.equals(PrimitiveType.RealType)) {
				CodeGen.gen("subl", "$4", "%esp");
				CodeGen.gen("fstps", "0(%esp)");
			}
			else {
				CodeGen.gen("pushl", "%eax");
	
			}
		}
	}
}