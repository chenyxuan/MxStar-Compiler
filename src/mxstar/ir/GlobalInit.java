package mxstar.ir;

import mxstar.ast.ExprNode;

public class GlobalInit {
	private StaticVar dest;
	private ExprNode src;

	public GlobalInit(StaticVar dest, ExprNode src) {
		this.dest = dest;
		this.src = src;
	}
	public ExprNode getSrc() {
		return src;
	}

	public StaticVar getDest() {
		return dest;
	}
}
