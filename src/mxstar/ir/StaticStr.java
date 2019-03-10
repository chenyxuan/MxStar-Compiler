package mxstar.ir;

import static mxstar.utility.GlobalSymbols.*;

public class StaticStr extends StaticData {
	private String value;

	public StaticStr(String value) {
		super(STATIC_STR, REG_SIZE);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
