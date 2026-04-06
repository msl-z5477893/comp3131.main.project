/*
 * Checker.java
 *
 * Tue 17 Mar 2026 23:19:18 AEDT
 *
 * This component performs semantic analysis on the VC compiler's abstract syntax tree (AST).
 * It handles two key functions:
 * 1. Identification: Links identifiers to their declarations using scope rules
 * 2. Type checking: Decorates the AST with type information using type rules
 *
 * Implementation Status:
 * - The identification pass is fully implemented using:
 *   - SymbolTable infrastructure
 *   - IdEntry components
 *   - idTable-related operations
 *
 * Implementation Tasks:
 * Your responsibility is to complete the type checker implementation:
 * - Finish all partially implemented visit methods
 * - Implement all remaining unimplemented visit methods
 *
 * Key Implementation Notes:
 * - Core type checking occurs primarily in:
 *   - visitBinaryExpr()
 *   - visitUnaryExpr()
 *   - visitGlobalVarDecl()
 *   - visitLocalVarDecl()
 *
 * You can examine the tree drawer, unparser, and tree printer, all of which use 
 * the visitor design pattern, to understand how to implement the type checker.
 *
 *
 * Testing Information:
 * - Each test case is small and typically contains one type error
 * - The marking script verifies correct error codes (not message content)
 * - You may customize error messages for debugging purposes by:
 *   1. Using the wrapper error reporter
 *   2. Ultimately calling the ErrorReporter instance
 */

package VC.Checker;

import VC.ASTs.*;
import VC.Scanner.SourcePosition;
import VC.ErrorReporter;
import VC.StdEnvironment;

import java.util.Objects;
import java.util.Optional;

// student imports
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.Set;

public final class Checker implements Visitor {

    // Enum for error messages
    private enum ErrorMessage {
        MISSING_MAIN("*0: main function is missing"),
        MAIN_RETURN_TYPE_NOT_INT("*1: return type of main is not int"),
        IDENTIFIER_REDECLARED("*2: identifier redeclared"),
        IDENTIFIER_DECLARED_VOID("*3: identifier declared void"),
        IDENTIFIER_DECLARED_VOID_ARRAY("*4: identifier declared void[]"),
        IDENTIFIER_UNDECLARED("*5: identifier undeclared"),
        INCOMPATIBLE_TYPE_FOR_ASSIGNMENT("*6: incompatible type for ="),
        INVALID_LVALUE_IN_ASSIGNMENT("*7: invalid lvalue in assignment"),
        INCOMPATIBLE_TYPE_FOR_RETURN("*8: incompatible type for return"),
        INCOMPATIBLE_TYPE_FOR_BINARY_OPERATOR("*9: incompatible type for this binary operator"),
        INCOMPATIBLE_TYPE_FOR_UNARY_OPERATOR("*10: incompatible type for this unary operator"),
        ARRAY_FUNCTION_AS_SCALAR("*11: attempt to use an array/function as a scalar"),
        SCALAR_FUNCTION_AS_ARRAY("*12: attempt to use a scalar/function as an array"),
        WRONG_TYPE_FOR_ARRAY_INITIALISER("*13: wrong type for element in array initialiser"),
        INVALID_INITIALISER_ARRAY_FOR_SCALAR("*14: invalid initialiser: array initialiser for scalar"),
        INVALID_INITIALISER_SCALAR_FOR_ARRAY("*15: invalid initialiser: scalar initialiser for array"),
        EXCESS_ELEMENTS_IN_ARRAY_INITIALISER("*16: excess elements in array initialiser"),
        ARRAY_SUBSCRIPT_NOT_INTEGER("*17: array subscript is not an integer"),
        ARRAY_SIZE_MISSING("*18: array size missing"),
        SCALAR_ARRAY_AS_FUNCTION("*19: attempt to reference a scalar/array as a function"),
        IF_CONDITIONAL_NOT_BOOLEAN("*20: if conditional is not boolean (found: %)"),
        FOR_CONDITIONAL_NOT_BOOLEAN("*21: for conditional is not boolean (found: %)"),
        WHILE_CONDITIONAL_NOT_BOOLEAN("*22: while conditional is not boolean (found: %)"),
        BREAK_NOT_IN_LOOP("*23: break must be in a while/for"),
        CONTINUE_NOT_IN_LOOP("*24: continue must be in a while/for"),
        TOO_MANY_ACTUAL_PARAMETERS("*25: too many actual parameters"),
        TOO_FEW_ACTUAL_PARAMETERS("*26: too few actual parameters"),
        WRONG_TYPE_FOR_ACTUAL_PARAMETER("*27: wrong type for actual parameter"),
        MISC_1("*28: misc 1"),
        MISC_2("*29: misc 2"),
        STATEMENTS_NOT_REACHED("*30: statement(s) not reached"),
        MISSING_RETURN_STATEMENT("*31: missing return statement");

        private final String message;

        ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final SymbolTable idTable;
    private static final SourcePosition dummyPos = new SourcePosition();
    private final ErrorReporter reporter;

    public Checker(ErrorReporter reporter) {
        this.reporter = Objects.requireNonNull(reporter, "ErrorReporter must not be null");
        this.idTable = new SymbolTable();
        establishStdEnvironment();
    }

    // =========================== AUXILIARY METHODS ===========================

     /* 
      * Declares a variable in the symbol table and checks for redeclaration errors.
      */

    private void declareVariable(Ident ident, Decl decl) {
        idTable.retrieveOneLevel(ident.spelling).ifPresent(entry -> 
            reporter.reportError(ErrorMessage.IDENTIFIER_REDECLARED.getMessage(), ident.spelling, ident.position)
        );
        idTable.insert(ident.spelling, decl);
        ident.visit(this, null);
    }

    // Your other auxilary methods go here
    // ....

    public void check(AST ast) {
        ast.visit(this, null);
    }

    public Type getType(AST ast) {
        Object type = ast.visit(this, null);
        if (!(type instanceof Type)) {
            reporter.reportError("Programming Error: visit function did not return a type!", "", ast.position);
        }
        return (Type) type;
    }

    // =========================== PROGRAMS ===========================

    @Override
    public Object visitProgram(Program ast, Object o) {
        ast.FL.visit(this, null);

         // You type-checking code goes here

        return null;
    }

    // =========================== STATEMENTS ===========================

    @Override
    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        idTable.openScope();

           // Your type-checking code goes here

        idTable.closeScope();
        return null;
    }

    @Override
    public Object visitStmtList(StmtList ast, Object o) {
        ast.getHead().visit(this, o); 
        if (ast.getHead() instanceof ReturnStmt && ast.getNext() instanceof StmtList) {
            reporter.reportError(ErrorMessage.STATEMENTS_NOT_REACHED.getMessage(), "", ast.getNext().position);
        }   
        ast.getNext().visit(this, o); 
        return null;
    }   

    @Override
    public Object visitExprStmt(ExprStmt ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }


    @Override
    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    @Override
    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    // Expressions
    public Object visitUnaryExpr(UnaryExpr ast, Object o) {

    }

    // TODO: incomplete implementation
    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        Predicate<Type> isNumeric = t -> t == StdEnvironment.floatType
                || t == StdEnvironment.intType;
        BiPredicate<Type, Type> isFloat = (t1, t2) -> t1 == StdEnvironment.floatType
                || t2 == StdEnvironment.floatType;
        Set<String> relational = Set.of("<", "<=", ">=", ">");
        Set<String> arithmetic = Set.of("+", "-", "*", "/");
        Set<String> equality = Set.of("==", "!=");
        Set<String> logical = Set.of("||", "&&", "!");
        String assignment = "=";
        Type express1Type = getType(ast.E1);
        Type express2Type = getType(ast.E2);
        String operator = ast.O.spelling;
        // bools and numeric types can never mix
        if (isNumeric.test(express1Type) != isNumeric.test(express2Type)) {
            reporter.reportError(
                ErrorMessage.INCOMPATIBLE_TYPE_FOR_BINARY_OPERATOR.getMessage(),
                "",
                ast.position
            );
            return StdEnvironment.errorType;
        }
        // TODO: determine order of matching between kinds of binary expressions
        if (relational.contains(operator)) {
            boolean valid = isNumeric.test(express1Type)
                    && isNumeric.test(express2Type);
            if (!valid) {
                reporter.reportError(
                    ErrorMessage.INCOMPATIBLE_TYPE_FOR_BINARY_OPERATOR.getMessage(),
                    "",
                    ast.position
                );
                return StdEnvironment.errorType;
            }
            return StdEnvironment.booleanType;
        }

        if (arithmetic.contains(operator)) {
            boolean valid = isNumeric.test(express1Type)
                    && isNumeric.test(express2Type);
            if (!valid) {
                reporter.reportError(
                    ErrorMessage.INCOMPATIBLE_TYPE_FOR_BINARY_OPERATOR.getMessage(),
                    "",
                    ast.position
                );
                return StdEnvironment.errorType;
            }
            if (isFloat.test(express1Type, express2Type)) return StdEnvironment.floatType;
            return StdEnvironment.intType
        }

        if (logical.contains(operator)) {
            boolean valid = express1Type == StdEnvironment.booleanType
                && express2Type == StdEnvironment.booleanType;
            if (!valid) {
                reporter.reportError(
                    ErrorMessage.INCOMPATIBLE_TYPE_FOR_BINARY_OPERATOR.getMessage(),
                    "",
                    ast.position
                );
                return StdEnvironment.errorType;
            }
            return StdEnvironment.booleanType;
        }

        if (operator == assignment) {
            // NOTE: for this to work, ast.E1 *must* be non-null.
            boolean lhsIsId = ast.E1 instanceof Ident;
            boolean matchingType = getType(ast.E1) == getType(ast.E2);
            boolean valid = matchingType == lhsIsId;
            if (!valid) {
                reporter.reportError(
                    ErrorMessage.INCOMPATIBLE_TYPE_FOR_ASSIGNMENT.getMessage(),
                    "",
                    ast.position
                );
                return StdEnvironment.errorType;
            }
            return getType(ast.E1);
        }

        // the previous code making up this function is exhaustive in terms of its operators
        // therefore it should never reach here.
        reporter.reportError(
           "error on visitBinaryExpr(): executed unreachable code/unhandled case found.",
            "",
            ast.position
        );
        return StdEnvironment.errorType;
    }


    @Override
    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        ast.type = (ast.parent instanceof ReturnStmt) ? StdEnvironment.voidType : StdEnvironment.errorType;
        return ast.type;
    }

    @Override
    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        ast.type = StdEnvironment.booleanType;
        return ast.type;
    }

    @Override
    public Object visitIntExpr(IntExpr ast, Object o) {
        ast.type = StdEnvironment.intType;
        return ast.type;
    }

    @Override
    public Object visitFloatExpr(FloatExpr ast, Object o) {
        ast.type = StdEnvironment.floatType;
        return ast.type;
    }

    @Override
    public Object visitVarExpr(VarExpr ast, Object o) {
        ast.type = (Type) ast.V.visit(this, null);
        return ast.type;
    }

    @Override
    public Object visitStringExpr(StringExpr ast, Object o) {
        ast.type = StdEnvironment.stringType;
        return ast.type;
    }

    // =========================== DECLARATIONS ===========================

    @Override
    public Object visitFuncDecl(FuncDecl ast, Object o) {
        idTable.insert(ast.I.spelling, ast); // Insert the function declaration into the symbol table

        // Your code goes here

        // HINT: Pass ast as the 2nd argument so that the formal parameters
        // of the function can be extracted when the function body is visited.
        //

        ast.S.visit(this, ast); // Visit the function body

        return null;
    }

    @Override
    public Object visitDeclList(DeclList ast, Object o) {
        ast.getHead().visit(this, null);
        ast.getNext().visit(this, null);
        return null;
    }

    @Override
    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    @Override
    public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
        declareVariable(ast.I, ast);

        // Fill the rest
        Type declaredType = ast.T;
        Type expressionType = getType(ast.E);
        if (declaredType != expressionType) {
            reporter.reportError(ErrorMessage.INCOMPATIBLE_TYPE_FOR_ASSIGNMENT.getMessage(), "", ast.position);
        }

        return null;
    }

    @Override
    public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
        declareVariable(ast.I, ast);

        // Fill the rest
        Type declaredType = ast.T;
        Type expressionType = getType(ast.E);
        if (declaredType != expressionType) {
            reporter.reportError(ErrorMessage.INCOMPATIBLE_TYPE_FOR_ASSIGNMENT.getMessage(), "", ast.position);
        }

        return null;
    }

    // =========================== PARAMETERS ===========================

    @Override
    public Object visitParaList(ParaList ast, Object o) {

        // Fill the rest

        return null;
    }

    @Override
    public Object visitParaDecl(ParaDecl ast, Object o) {
        declareVariable(ast.I, ast);
        
        if (ast.T.isVoidType()) {
            reporter.reportError(ErrorMessage.IDENTIFIER_DECLARED_VOID.getMessage(), ast.I.spelling, ast.I.position);
        } else if (ast.T instanceof ArrayType aType) {
            if (aType.T.isVoidType()) {
                reporter.reportError(ErrorMessage.IDENTIFIER_DECLARED_VOID_ARRAY.getMessage(), ast.I.spelling, ast.I.position);
            }
        }
        return null;
    }


    @Override
    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    // =========================== ARGUMENTS ===========================
   
    // Your visitor methods for arguments go here

    // =========================== TYPES ===========================

    @Override
    public Object visitErrorType(ErrorType ast, Object o) {
        return StdEnvironment.errorType;
    }

    @Override
    public Object visitBooleanType(BooleanType ast, Object o) {
        return StdEnvironment.booleanType;
    }

    @Override
    public Object visitIntType(IntType ast, Object o) {
        return StdEnvironment.intType;
    }

    @Override
    public Object visitFloatType(FloatType ast, Object o) {
        return StdEnvironment.floatType;
    }

    @Override
    public Object visitStringType(StringType ast, Object o) {
        return StdEnvironment.stringType;
    }

    @Override
    public Object visitVoidType(VoidType ast, Object o) {
        return StdEnvironment.voidType;
    }

    @Override
    public Object visitArrayType(ArrayType ast, Object o) {
        return ast;
    }

    // =========================== LITERALS, IDENTIFIERS AND OPERATORS ===========================

    @Override
    public Object visitIdent(Ident I, Object o) {
        Optional<IdEntry> binding = idTable.retrieve(I.spelling);
        binding.ifPresent(entry -> I.decl = entry.attr); // Link the identifier to its declaration
        return binding.map(entry -> entry.attr).orElse(null);
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral SL, Object o) {
        return StdEnvironment.booleanType;
    }

    @Override
    public Object visitIntLiteral(IntLiteral IL, Object o) {
        return StdEnvironment.intType;
    }

    @Override
    public Object visitFloatLiteral(FloatLiteral IL, Object o) {
        return StdEnvironment.floatType;
    }

    @Override
    public Object visitStringLiteral(StringLiteral IL, Object o) {
        return StdEnvironment.stringType;
    }

    @Override
    public Object visitOperator(Operator O, Object o) {
        return null;
    }

    // =========================== VARIABLE NAMES ===========================

    // Creates a small AST to represent the "declaration" of each built-in
    // function, and enters it in the symbol table.

    private FuncDecl declareStdFunc(Type resultType, String id, ASTList<Decl> pl) {
        var binding = new FuncDecl(resultType, new Ident(id, dummyPos), pl,
                new EmptyStmt(dummyPos), dummyPos);
        idTable.insert(id, binding);
        return binding;
    }

    // Creates small ASTs to represent "declarations" of all
    // build-in functions.
    // Inserts these "declarations" into the symbol table.

    private final static Ident dummyI = new Ident("x", dummyPos);

    private void establishStdEnvironment() {
        // Define four primitive types
        // errorType is assigned to ill-typed expressions

        StdEnvironment.booleanType = new BooleanType(dummyPos);
        StdEnvironment.intType = new IntType(dummyPos);
        StdEnvironment.floatType = new FloatType(dummyPos);
        StdEnvironment.stringType = new StringType(dummyPos);
        StdEnvironment.voidType = new VoidType(dummyPos);
        StdEnvironment.errorType = new ErrorType(dummyPos);

        // enter into the declarations for built-in functions into the table

        StdEnvironment.getIntDecl = declareStdFunc(StdEnvironment.intType,
                "getInt", new EmptyParaList(dummyPos));
        StdEnvironment.putIntDecl = declareStdFunc(StdEnvironment.voidType,
                "putInt", new ParaList(
                        new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));
        StdEnvironment.putIntLnDecl = declareStdFunc(StdEnvironment.voidType,
                "putIntLn", new ParaList(
                        new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));
        StdEnvironment.getFloatDecl = declareStdFunc(StdEnvironment.floatType,
                "getFloat", new EmptyParaList(dummyPos));
        StdEnvironment.putFloatDecl = declareStdFunc(StdEnvironment.voidType,
                "putFloat", new ParaList(
                        new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));
        StdEnvironment.putFloatLnDecl = declareStdFunc(StdEnvironment.voidType,
                "putFloatLn", new ParaList(
                        new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));
        StdEnvironment.putBoolDecl = declareStdFunc(StdEnvironment.voidType,
                "putBool", new ParaList(
                        new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));
        StdEnvironment.putBoolLnDecl = declareStdFunc(StdEnvironment.voidType,
                "putBoolLn", new ParaList(
                        new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));

        StdEnvironment.putStringLnDecl = declareStdFunc(StdEnvironment.voidType,
                "putStringLn", new ParaList(
                        new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));

        StdEnvironment.putStringDecl = declareStdFunc(StdEnvironment.voidType,
                "putString", new ParaList(
                        new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
                        new EmptyParaList(dummyPos), dummyPos));

        StdEnvironment.putLnDecl = declareStdFunc(StdEnvironment.voidType,
                "putLn", new EmptyParaList(dummyPos));
    }
}
