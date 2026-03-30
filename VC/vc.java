package VC;

import VC.Scanner.Scanner;
import VC.Scanner.SourceFile;
import VC.Parser.Parser;
import VC.ASTs.AST;
import VC.TreeDrawer.Drawer;
import VC.TreePrinter.Printer;
import VC.UnParser.UnParser;
import VC.Checker.Checker;
import VC.StdEnvironment;

public class vc {

    private static Scanner scanner;
    private static ErrorReporter reporter;
    private static Parser parser;
    private static Drawer drawer;
    private static Printer printer;
    private static UnParser unparser;
    private static Checker checker;

    private static int drawingAST = 0;
    private static boolean printingAST = false;
    private static boolean unparsingAST = false;
    private static String inputFilename;
    private static String astFilename = "";
    private static String unparsingFilename = "";

    private static AST theAST;

    private static void cmdLineOptions() {
        System.out.println("\nUsage: java VC.vc [-options] filename");
        System.out.println("\nwhere options include:");
        System.out.println("-d [1234]           display the AST (without SourcePosition)");
        System.out.println("                    1:  the AST from the parser (without SourcePosition)");
        System.out.println("                    2:  the AST from the parser (with SourcePosition)");
        System.out.println("                    3:  the AST from the checker (without SourcePosition)");
        System.out.println("                    4:  the AST from the checker (with SourcePosition)");
        System.out.println("-t [file]           print the (non-annotated) AST into <file>");
        System.out.println("                    (or filename + \"t\" if <file> is unspecified)");
        System.out.println("-u [file]           unparse the (non-annotated) AST into <file>");
        System.out.println("                    (or filename + \"u\" if <file> is unspecified)");
        System.exit(1);
    }

    public static void main(String[] args) {
        int i = 0;
        String arg;

        System.out.println("======= The VC compiler =======\n");

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            switch (arg) {
                case "-d":
                    handleDOption(args, i);
                    break;
                case "-t":
                    handleTOption(args, i);
                    break;
                case "-u":
                    handleUOption(args, i);
                    break;
                default:
                    System.out.printf("[# vc #]: invalid option %s%n", arg);
                    cmdLineOptions();
            }
        }

        if (i == args.length) {
            System.out.println("[# vc #]: no input file");
            cmdLineOptions();
        } else {
            inputFilename = args[i];
        }

        SourceFile source = null;
        try {
            source = new SourceFile(inputFilename);
            reporter = new ErrorReporter();
            System.out.println("Pass 1: Lexical and syntactic Analysis");

            scanner = new Scanner(source, reporter);
            parser = new Parser(scanner, reporter);
            theAST = parser.parseProgram();

            if (reporter.getNumErrors() == 0) {
                handlePostParsing();
            } else {
                System.out.println("Compilation was unsuccessful.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the SourceFile object explicitly if needed
			if (source != null) {
        		try {
            		source = null;
        		} catch (Exception e) {
            		e.printStackTrace();
        		}
    		}
        }
    }

    private static void handleDOption(String[] args, int i) {
        if (i < args.length) {
            try {
                int n = Integer.parseInt(args[i]);
                if (n >= 1 && n <= 4) {
                    drawingAST = n;
                    i++;
                } else {
                    System.out.println("[# vc #]: invalid option -d " + args[i]);
                    cmdLineOptions();
                }
            } catch (NumberFormatException e) {
                System.out.println("[# vc #]: invalid option -d " + args[i]);
                cmdLineOptions();
            }
        }
    }

    private static void handleTOption(String[] args, int i) {
        printingAST = true;
        if (!args[i].equals("-t")) {
            astFilename = args[i].substring(2);
        } else if (i < args.length && !args[i].startsWith("-")) {
            astFilename = args[i++];
        }
    }

    private static void handleUOption(String[] args, int i) {
        unparsingAST = true;
        if (!args[i].equals("-u")) {
            astFilename = args[i].substring(2);
        }
        if (i < args.length && !args[i].startsWith("-")) {
            unparsingFilename = args[i++];
        }
    }

    private static void handlePostParsing() {
        if (unparsingAST) {
            if (unparsingFilename.isEmpty()) {
                unparsingFilename = inputFilename + "u";
            }
            unparser = new UnParser(unparsingFilename);
            unparser.unparse(theAST);
            System.out.println("[# vc #]: The unparsed VC program printed to " + unparsingFilename);
        }

        if (printingAST) {
            if (astFilename.isEmpty()) {
                astFilename = inputFilename + "p";
            }
            printer = new Printer(astFilename);
            printer.print(theAST);
            System.out.println("[# vc #]: The linearised AST printed to " + astFilename);
        }

        if (drawingAST >= 1 && drawingAST <= 2) {
            drawer = new Drawer();
            if (drawingAST == 2) {
                drawer.enableDebugging(); // show SourcePosition
            }
            drawer.draw(theAST);
        }

        System.out.println("Pass 2: Semantic Analysis");
        checker = new Checker(reporter);
        checker.check(theAST);

        if (reporter.getNumErrors() == 0) {
            System.out.println("Compilation was successful.");
        } else {
            System.out.println("Compilation was unsuccessful.");
        }

        if (drawingAST >= 3) {
            drawer = new Drawer();
            if (drawingAST == 4) {
                drawer.enableDebugging(); // show SourcePosition
            }
            drawer.draw(theAST);
        }
    }
}

