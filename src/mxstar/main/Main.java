package mxstar.main;

import mxstar.ast.*;
import mxstar.backend.*;
import mxstar.frontend.*;
import mxstar.ir.*;
import mxstar.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

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
		else inS = System.in;
		if (astOutFile != null) astOutS = new PrintStream(new FileOutputStream(astOutFile));
		else astOutS = new PrintStream(new FileOutputStream("ast.txt"));
		if (irOutFile != null) irOutS = new PrintStream(new FileOutputStream(irOutFile));
		else irOutS = new PrintStream(new FileOutputStream("ir.txt"));
		if (outFile != null) outS = new PrintStream(new FileOutputStream(outFile));
		else outS = System.out;

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


	private static void compile() throws Exception {
		ASTRootNode astRoot = buildAST();
		if(astOutS != null)	(new ASTPrinter(astOutS)).visit(astRoot);

		SemanticAnalyser analyser = SemanticAnalyse(astRoot);

		IRRoot ir = buildIR(astRoot, analyser);

		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir1 ok");
		new BinaryOpProcessor(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir2 ok");
		new StaticDataProcessor(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir3 ok");
		new FuncArgProcessor(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir4 ok");
		new RegLivenessAnalyser(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir5 ok");
		new RegisterAllocator(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir6 ok");
		new NASMTransformer(ir).run();
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir7 ok");
		new NASMPrinter(outS).visit(ir);
		if(irOutS != null) (new IRPrinter(irOutS)).visit(ir);
//		System.err.println("ir8 ok");

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

	private static SemanticAnalyser SemanticAnalyse(ASTRootNode astRoot) {
		SemanticAnalyser analyser = new SemanticAnalyser();
		analyser.visit(astRoot);
		return analyser;
	}

	private static IRRoot buildIR(ASTRootNode astRoot, SemanticAnalyser analyser) {
		IRBuilder irBuilder = new IRBuilder(analyser);
		irBuilder.visit(astRoot);
		return irBuilder.getIR();
	}
}
