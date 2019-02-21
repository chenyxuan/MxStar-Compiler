package mxstar.utility;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Location {
	private int line, charPositionInLine;

	public Location(int line, int charPositionInLine) {
		this.line = line;
		this.charPositionInLine = charPositionInLine;
	}

	public Location(Token token) {
		this.line = token.getLine();
		this.charPositionInLine = token.getCharPositionInLine();
	}

	static public Location fromCtx(ParserRuleContext ctx) {
		return new Location(ctx.getStart());
	}

	public String toString() {
		return String.format("(%d:%d)", line, charPositionInLine);
	}
}
