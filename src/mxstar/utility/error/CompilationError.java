package mxstar.utility.error;

import mxstar.utility.Location;

public class CompilationError extends Error {
	public CompilationError(String msg, Location location) {
		super(String.format("[Compilation Error] at %s: %s", location, msg));
	}
}
