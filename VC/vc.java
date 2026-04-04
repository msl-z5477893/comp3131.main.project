package VC;

import VC.Scanner.Scanner;
import VC.Scanner.SourceFile;
import VC.Parser.Parser;
import VC.ASTs.AST;
import VC.TreeDrawer.Drawer;
import VC.TreePrinter.Printer;
import VC.UnParser.UnParser;
import VC.Checker.Checker;

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
        System.out.println("                    1: the AST from the parser (without SourcePosition)");
        System.out.println("                    2: the AST from the parser (with SourcePosition)");
        System.out.println("                    3: the AST from the checker (without SourcePosition)");
        System.out.println("                    4: the AST from the checker (with SourcePosition)");
        System.out.println("-t [file]           print the (non-annotated) AST into <file>");
        System.out.println("                    (or filename + \"p\" if <file> is unspecified)");
        System.out.println("-u [file]           unparse the (non-annotated) AST into <file>");
        System.out.println("                    (or filename + \"u\" if <file> is unspecified)");
        System.exit(1);
    }

    public static void main(String[] args) {
        System.out.println("======= The VC compiler =======\n");

        ArgReader reader = new ArgReader(args);
        parseOptions(reader);

        if (!reader.hasNext()) {
            System.out.println("[# vc #]: no input file");
            cmdLineOptions();
        }

        inputFilename = reader.next();

        if (reader.hasNext()) {
            System.out.println("[# vc #]: too many command-line arguments");
            cmdLineOptions();
        }

        SourceFile source = null;
        try {
            System.out.println("input filename " + inputFilename);

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
            source = null;
        }
    }

    private static void parseOptions(ArgReader reader) {
        while (reader.hasNext()) {
            String arg = reader.peek();
            if (!arg.startsWith("-")) {
                return;
            }

            arg = reader.next();

            if (arg.equals("-d")) {
                handleDOption(reader);
            } else if (arg.startsWith("-d")) {
                handleDInlineOption(arg);
            } else if (arg.equals("-t")) {
                handleTOption(reader);
            } else if (arg.startsWith("-t")) {
                handleTInlineOption(arg);
            } else if (arg.equals("-u")) {
                handleUOption(reader);
            } else if (arg.startsWith("-u")) {
                handleUInlineOption(arg);
            } else {
                System.out.printf("[# vc #]: invalid option %s%n", arg);
                cmdLineOptions();
            }
        }
    }

    private static void handleDOption(ArgReader reader) {
        if (!reader.hasNext()) {
            System.out.println("[# vc #]: missing argument for -d");
            cmdLineOptions();
        }

        String value = reader.next();
        setDrawingOption(value, "-d " + value);
    }

    private static void handleDInlineOption(String arg) {
        String value = arg.substring(2);
        if (value.isEmpty()) {
            System.out.printf("[# vc #]: invalid option %s%n", arg);
            cmdLineOptions();
        }
        setDrawingOption(value, arg);
    }

    private static void setDrawingOption(String value, String originalArg) {
        try {
            int n = Integer.parseInt(value);
            if (n >= 1 && n <= 4) {
                drawingAST = n;
            } else {
                System.out.printf("[# vc #]: invalid option %s%n", originalArg);
                cmdLineOptions();
            }
        } catch (NumberFormatException e) {
            System.out.printf("[# vc #]: invalid option %s%n", originalArg);
            cmdLineOptions();
        }
    }

    private static void handleTOption(ArgReader reader) {
        printingAST = true;
        if (reader.hasNext() && !reader.peek().startsWith("-")) {
            astFilename = reader.next();
        }
    }

    private static void handleTInlineOption(String arg) {
        printingAST = true;
        astFilename = arg.substring(2);
        if (astFilename.isEmpty()) {
            System.out.printf("[# vc #]: invalid option %s%n", arg);
            cmdLineOptions();
        }
    }

    private static void handleUOption(ArgReader reader) {
        unparsingAST = true;
        if (reader.hasNext() && !reader.peek().startsWith("-")) {
            unparsingFilename = reader.next();
        }
    }

    private static void handleUInlineOption(String arg) {
        unparsingAST = true;
        unparsingFilename = arg.substring(2);
        if (unparsingFilename.isEmpty()) {
            System.out.printf("[# vc #]: invalid option %s%n", arg);
            cmdLineOptions();
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
                drawer.enableDebugging();
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

        if (drawingAST >= 3 && drawingAST <= 4) {
            drawer = new Drawer();
            if (drawingAST == 4) {
                drawer.enableDebugging();
            }
            drawer.draw(theAST);
        }
    }

    private static final class ArgReader {
        private final String[] args;
        private int index;

        private ArgReader(String[] args) {
            this.args = args;
            this.index = 0;
        }

        private boolean hasNext() {
            return index < args.length;
        }

        private String peek() {
            return args[index];
        }

        private String next() {
            return args[index++];
        }
    }
}
