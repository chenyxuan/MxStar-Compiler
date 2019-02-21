package mxstar;

import mxstar.ast.ASTRootNode;
import mxstar.backend.IRBuilder;
import mxstar.backend.IRPrinter;
import mxstar.frontend.ASTBuilder;
import mxstar.frontend.ASTPrinter;
import mxstar.frontend.SemanticAnalyser;
import mxstar.ir.IRRoot;
import mxstar.parser.MxStarLexer;
import mxstar.parser.MxStarParser;
import mxstar.parser.SyntaxErrorListener;
import mxstar.scope.Scope;
import mxstar.utility.error.SemanticError;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

/*
TODO:
Error recorder

 */

public class Main {

	private static InputStream inS;
	private static PrintStream astOutS, irOutS, outS;

	public static void main(String[] args) throws Exception {
		String inFile = null, astOutFile = null, irOutFile = null, outFile = null;
		boolean isPrintHelp = false;

		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			switch (arg) {
				case "-h":
				case "--help":
					isPrintHelp = true;
					break;

				case "-o":
					if (i + 1 < args.length) outFile = args[++i];
					else errorArgs();
					break;

				case "--ast":
					if (i + 1 < args.length) astOutFile = args[++i];
					else errorArgs();
					break;

				case "--ir":
					if (i + 1 < args.length) irOutFile = args[++i];
					else errorArgs();
					break;

				default:
					if (arg.charAt(0) != '-' && inFile == null) inFile = arg;
					else errorArgs();
			}
		}

		if (isPrintHelp) printHelp();

		if (inFile != null) inS = new FileInputStream(inFile);
		else inS = new FileInputStream("input.txt");
		if (astOutFile != null) astOutS = new PrintStream(new FileOutputStream(astOutFile));
		else astOutS = new PrintStream(new FileOutputStream("ast.txt"));
		if (irOutFile != null) irOutS = new PrintStream(new FileOutputStream(irOutFile));
		else irOutS = new PrintStream(new FileOutputStream("ir.txt"));
		if (outFile != null) outS = new PrintStream(new FileOutputStream(outFile));
		else outS = new PrintStream(new FileOutputStream("output.txt"));

		try {
			compile();
		} catch (Error error) {
			System.err.println(error.getMessage());
			System.exit(1);
		}
	}

	private static void errorArgs() {
		System.out.println("Error: invalid arguments");
		printHelp();
		System.exit(1);
	}

	private static void printHelp() {
		System.out.println("Usage: MxStarCompiler [options] file...");
		System.out.println("Options:");
		System.out.println("  -h, --help                    Display this information");
		System.out.println("  -o <file>                     Place the output code into <file>");
		System.out.println("  --ast <file>                  Output abstract syntax tree into <file>");
		System.out.println("  --ir <file>                   Output intermediate representation into <file>");
	}

	private static ASTRootNode astRoot;
	private static SemanticAnalyser analyser;
	private static IRRoot ir;

	private static void compile() throws Exception {
		astRoot = buildAST();
		if(astOutS != null)	(new ASTPrinter(astOutS)).visit(astRoot);
		SemanticAnalyse();
		ir = buildIR();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
	}

	private static ASTRootNode buildAST() throws Exception {
		CharStream input = CharStreams.fromStream(inS);
		MxStarLexer lexer = new MxStarLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MxStarParser parser = new MxStarParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new SyntaxErrorListener());
		ASTBuilder astBuilder = new ASTBuilder();
		return (ASTRootNode) astBuilder.visit(parser.translationUnit());
	}

	private static void SemanticAnalyse() {
		analyser = new SemanticAnalyser();
		analyser.visit(astRoot);
	}

	private static IRRoot buildIR() {
		IRBuilder irBuilder = new IRBuilder(analyser);
		irBuilder.visit(astRoot);
		return irBuilder.getIR();
	}
}
