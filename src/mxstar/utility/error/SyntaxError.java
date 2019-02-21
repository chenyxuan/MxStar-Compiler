package mxstar.utility.error;

import mxstar.utility.Location;

public class SyntaxError extends Error {
	public SyntaxError(String msg, Location location) {
		super(String.format("[Syntax Error] at %s: %s", location.toString(), msg));
	}
}
