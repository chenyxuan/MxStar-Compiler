package mxstar.utility.error;

import mxstar.utility.Location;

public class SemanticError extends Error {
	public SemanticError(String msg, Location location) {
		super(String.format("[SemanticError Error] at %s: %s", location, msg));
	}
}