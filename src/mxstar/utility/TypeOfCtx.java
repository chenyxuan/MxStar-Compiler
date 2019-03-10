package mxstar.utility;


import mxstar.symbol.type.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TypeOfCtx {
	private Type type;

	public TypeOfCtx(TerminalNode ID,
					 TerminalNode INT,
					 TerminalNode BOOL,
					 TerminalNode STRING) {
		this.type = typeOfCtx(ID, INT, BOOL, STRING);
	}

	public static Type typeOfCtx(TerminalNode ID,
		                  TerminalNode INT,
		                  TerminalNode BOOL,
		                  TerminalNode STRING) {
		if(ID != null) return new ClassType(ID.getText());
		if(INT != null) return IntType.getInstance();
		if(BOOL != null) return BoolType.getInstance();
		if(STRING != null) return StringType.getInstance();

		throw new Error("Can't get Type from the context");
	}

	public Type getType() {
		return type;
	}
}
