/*
 * Parser.java 
 *
 * Tue 03 Mar 2026 14:04:48 AEDT
 *
 * PLEASE COMPARE Recogniser.java PROVIDED IN ASSIGNMENT 2 AND Parser.java
 * PROVIDED BELOW TO UNDERSTAND HOW THE FORMER IS MODIFIED TO OBTAIN THE LATTER.
 *
 * This parser for a subset of the VC language is intended to 
 *  demonstrate how to create the AST nodes, including (among others): 
 *  (1) a list (of statements)
 *  (2) a function
 *  (3) a statement (which is an expression statement), 
 *  (4) a unary expression
 *  (5) a binary expression
 *  (6) terminals (identifiers, integer literals and operators)
 *
 * In addition, it also demonstrates how to use the two methods start 
 * and finish to determine the position information for the start and 
 * end of a construct (known as a phrase) corresponding an AST node.
 *
 * NOTE THAT THE POSITION INFORMATION WILL NOT BE MARKED. HOWEVER, IT CAN BE
 * USEFUL TO DEBUG YOUR IMPLEMENTATION.
 *
 * Note that what is provided below is an implementation for a subset of VC
 * given below rather than VC itself. It provides a good starting point for you
 * to implement a parser for VC yourself, by modifying the parsing methods
 * provided (whenever necessary).
 *
 *
 * Alternatively, you are free to disregard the starter code entirely and 
 * develop your own solution, as long as it adheres to the same public 
 * interface.


program       -> func-decl
func-decl     -> type identifier "(" ")" compound-stmt
type          -> void
identifier    -> ID
// statements
compound-stmt -> "{" stmt* "}" 
stmt          -> expr-stmt
expr-stmt     -> expr? ";"
// expressions 
expr                -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
                    |  additive-expr "-" multiplicative-expr
multiplicative-expr -> unary-expr
                    |  multiplicative-expr "*" unary-expr
                    |  multiplicative-expr "/" unary-expr
unary-expr          -> "-" unary-expr
                    |  primary-expr

primary-expr        -> identifier
                    |  INTLITERAL
                    | "(" expr ")"
 */

package VC.Parser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;
import VC.ASTs.*;

public class Parser {

    private Scanner scanner;
    private ErrorReporter errorReporter;
    private Token currentToken;
    private SourcePosition previousTokenPosition;
    private SourcePosition dummyPos = new SourcePosition();

    public Parser (Scanner lexer, ErrorReporter reporter) {
        scanner = lexer;
        errorReporter = reporter;

        previousTokenPosition = new SourcePosition();

        currentToken = scanner.getToken();
    }

    // match checks to see f the current token matches tokenExpected.
    // If so, fetches the next token.
    // If not, reports a syntactic error.

    void match(int tokenExpected) throws SyntaxError {
        if (currentToken.kind == tokenExpected) {
            previousTokenPosition = currentToken.position;
            currentToken = scanner.getToken();
        } else {
            throw syntacticError("\"%\" expected here", Token.spell(tokenExpected));
        }
    }

    void accept() {
        previousTokenPosition = currentToken.position;
        currentToken = scanner.getToken();
    }

    private SyntaxError syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
        SourcePosition pos = currentToken.position;
        errorReporter.reportError(messageTemplate, tokenQuoted, pos);
        return new SyntaxError();
    }

    // start records the position of the start of a phrase.
    // This is defined to be the position of the first
    // character of the first token of the phrase.

    void start(SourcePosition position) {
        position.lineStart = currentToken.position.lineStart;
        position.charStart = currentToken.position.charStart;
    }

    // finish records the position of the end of a phrase.
    // This is defined to be the position of the last
    // character of the last token of the phrase.

    void finish(SourcePosition position) {
        position.lineFinish = previousTokenPosition.lineFinish;
        position.charFinish = previousTokenPosition.charFinish;
    }

    void copyStart(SourcePosition from, SourcePosition to) {
        to.lineStart = from.lineStart;
        to.charStart = from.charStart;
    }

    // ========================== PROGRAMS ========================

    // ========================== PROGRAMS ========================

    public Program parseProgram() {
        SourcePosition programPos = new SourcePosition();
        start(programPos);

        try {
            ASTList<Decl> dlAST = parseFuncDeclList();
            finish(programPos);
            Program programAST = new Program(dlAST, programPos);
            if (currentToken.kind != Token.EOF) {
                syntacticError("\"%\" unknown type", currentToken.spelling);
            }
            return programAST;
        } catch (SyntaxError s) {
            return null;
        }
    }



    // ========================== DECLARATIONS ========================

    ASTList<Decl> parseFuncDeclList() throws SyntaxError {
        ASTList<Decl> dlAST = null;
        Decl dAST = null;

        SourcePosition funcPos = new SourcePosition();
        start(funcPos);

        dAST = parseFuncDecl();
    
        if (currentToken.kind == Token.VOID) {
            dlAST = parseFuncDeclList();
            finish(funcPos);
            dlAST = new DeclList(dAST, dlAST, funcPos);
        } else {
            finish(funcPos);
            dlAST = new DeclList(dAST, new EmptyDeclList(dummyPos), funcPos);
        }
        if (dlAST == null) dlAST = new EmptyDeclList(dummyPos);

        return dlAST;
    }

    Decl parseFuncDecl() throws SyntaxError {
        Decl fAST = null; 

        SourcePosition funcPos = new SourcePosition();
        start(funcPos);

        Type tAST = parseType();
        Ident iAST = parseIdent();
        ASTList<Decl> fplAST = parseParaList();
        Stmt cAST = parseCompoundStmt();
        finish(funcPos);
        fAST = new FuncDecl(tAST, iAST, fplAST, cAST, funcPos);
        return fAST;
    }

    //  ======================== TYPES ==========================

    Type parseType() throws SyntaxError {
        Type typeAST = null;

        SourcePosition typePos = new SourcePosition();
        start(typePos);

        match(Token.VOID);

        finish(typePos);
        typeAST = new VoidType(typePos);

        return typeAST;
    }

    // ======================= STATEMENTS ==============================

    Stmt parseCompoundStmt() throws SyntaxError {
        Stmt cAST = null; 

        SourcePosition stmtPos = new SourcePosition();
        start(stmtPos);

        match(Token.LCURLY);

        // Insert code here to build a DeclList node for variable declarations
        ASTList<Stmt> slAST = parseStmtList();
        match(Token.RCURLY);
        finish(stmtPos);

        /* In the subset of the VC grammar implemented in this starter code, no variable declarations are
          * allowed. Therefore, a block (i.e., compound statement) is empty iff it has no statements.
          */

        if (slAST.isEmpty()) 
             cAST = new EmptyCompStmt(stmtPos);
        else
              cAST = new CompoundStmt(new EmptyDeclList(dummyPos), slAST, stmtPos);
        return cAST;
    }


    ASTList<Stmt> parseStmtList() throws SyntaxError {
        ASTList<Stmt> slAST = null; 

        SourcePosition stmtPos = new SourcePosition();
        start(stmtPos);

        if (currentToken.kind != Token.RCURLY) {
            Stmt sAST = parseStmt();
            if (currentToken.kind != Token.RCURLY) {
                slAST = parseStmtList();
                finish(stmtPos);
                slAST = new StmtList(sAST, slAST, stmtPos);
            } else {
                finish(stmtPos);
                slAST = new StmtList(sAST, new EmptyStmtList(dummyPos), stmtPos);
            }
        } else {
            slAST = new EmptyStmtList(dummyPos);
        }
    
        return slAST;
    }

    Stmt parseStmt() throws SyntaxError {
        Stmt sAST = null;

        sAST = parseExprStmt();

        return sAST;
    }

    Stmt parseExprStmt() throws SyntaxError {
        Stmt sAST = null;

        SourcePosition stmtPos = new SourcePosition();
        start(stmtPos);

        if (currentToken.kind == Token.ID
            || currentToken.kind == Token.INTLITERAL
            || currentToken.kind == Token.LPAREN) {
            Expr eAST = parseExpr();
            match(Token.SEMICOLON);
            finish(stmtPos);
            sAST = new ExprStmt(eAST, stmtPos);
        } else {
            match(Token.SEMICOLON);
            finish(stmtPos);
            sAST = new ExprStmt(new EmptyExpr(dummyPos), stmtPos);
        }
        return sAST;
    }


    // ======================= PARAMETERS =======================

    ASTList<Decl> parseParaList() throws SyntaxError {
        ASTList<Decl> formalsAST = null;

        SourcePosition formalsPos = new SourcePosition();
        start(formalsPos);

        match(Token.LPAREN);
        match(Token.RPAREN);
        finish(formalsPos);
        formalsAST = new EmptyParaList(formalsPos);

        return formalsAST;
    }


    // ======================= EXPRESSIONS ======================


    Expr parseExpr() throws SyntaxError {
        Expr exprAST = null;
        exprAST = parseAdditiveExpr();
        return exprAST;
    }

    Expr parseAdditiveExpr() throws SyntaxError {
        Expr exprAST = null;

        SourcePosition addStartPos = new SourcePosition();
        start(addStartPos);

        exprAST = parseMultiplicativeExpr();
        while (currentToken.kind == Token.PLUS
           || currentToken.kind == Token.MINUS) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseMultiplicativeExpr();

            SourcePosition addPos = new SourcePosition();
            copyStart(addStartPos, addPos);
            finish(addPos);
            exprAST = new BinaryExpr(exprAST, opAST, e2AST, addPos);
        }
        return exprAST;
    }

    Expr parseMultiplicativeExpr() throws SyntaxError {

        Expr exprAST = null;

        SourcePosition multStartPos = new SourcePosition();
        start(multStartPos);

        exprAST = parseUnaryExpr();
        while (currentToken.kind == Token.MULT
           || currentToken.kind == Token.DIV) {
            Operator opAST = acceptOperator();
            Expr e2AST = parseUnaryExpr();
            SourcePosition multPos = new SourcePosition();
            copyStart(multStartPos, multPos);
            finish(multPos);
            exprAST = new BinaryExpr(exprAST, opAST, e2AST, multPos);
        }
        return exprAST;
    }

    Expr parseUnaryExpr() throws SyntaxError {
        SourcePosition unaryPos = new SourcePosition();
        start(unaryPos);

        return switch (currentToken.kind) {
            case Token.MINUS, Token.PLUS, Token.NOT -> {
                Operator opAST = acceptOperator();
                Expr e2AST = parseUnaryExpr();
                finish(unaryPos);
                yield new UnaryExpr(opAST, e2AST, unaryPos);
            }
            default -> parsePrimaryExpr();
        };
    }


    Expr parsePrimaryExpr() throws SyntaxError {
        SourcePosition primPos = new SourcePosition();
        start(primPos);

        return switch (currentToken.kind) {
            case Token.ID -> {
                Ident iAST = parseIdent();
                finish(primPos);
                Var simVAST = new SimpleVar(iAST, primPos);
                yield new VarExpr(simVAST, primPos);
            }
            case Token.LPAREN -> {
                accept();
                Expr exprAST = parseExpr();
                match(Token.RPAREN);
                yield exprAST;
            }
            case Token.INTLITERAL -> {
                IntLiteral ilAST = parseIntLiteral();
                finish(primPos);
                yield new IntExpr(ilAST, primPos);
            }
            case Token.FLOATLITERAL -> {
                FloatLiteral flAST = parseFloatLiteral();
                finish(primPos);
                yield new FloatExpr(flAST, primPos);
            }
            case Token.BOOLEANLITERAL -> {
                BooleanLiteral blAST = parseBooleanLiteral();
                finish(primPos);
                yield new BooleanExpr(blAST, primPos);
            }
            case Token.STRINGLITERAL -> {
                StringLiteral slAST = parseStringLiteral();
                finish(primPos);
                yield new StringExpr(slAST, primPos);
            }
            default -> throw syntacticError("illegal primary expression", currentToken.spelling);
        };
    }



    // ========================== ID, OPERATOR and LITERALS ========================

    Ident parseIdent() throws SyntaxError {
        if (currentToken.kind != Token.ID) {
            throw syntacticError("identifier expected here", "");
        }

        String spelling = currentToken.spelling;
        accept();
        return new Ident(spelling, previousTokenPosition);
    }


    // acceptOperator parses an operator, and constructs a leaf AST for it

      Operator acceptOperator() throws SyntaxError {
        String spelling = currentToken.spelling;
        accept();
        return new Operator(spelling, previousTokenPosition);
      }

 // ========================== LITERALS ========================
    private IntLiteral parseIntLiteral() throws SyntaxError {
        if (currentToken.kind == Token.INTLITERAL) {
            String spelling = currentToken.spelling;
            accept();
            return new IntLiteral(spelling, previousTokenPosition);
        } else {
            throw syntacticError("integer literal expected here", "");
        }
    }

    private FloatLiteral parseFloatLiteral() throws SyntaxError {
        if (currentToken.kind == Token.FLOATLITERAL) {
            String spelling = currentToken.spelling;
            accept();
            return new FloatLiteral(spelling, previousTokenPosition);
        } else {
            throw syntacticError("float literal expected here", "");
        }   
    }   
    
    private BooleanLiteral parseBooleanLiteral() throws SyntaxError {
        if (currentToken.kind == Token.BOOLEANLITERAL) {
            String spelling = currentToken.spelling;
            accept();
            return new BooleanLiteral(spelling, previousTokenPosition);
        } else {
            throw syntacticError("boolean literal expected here", "");
        }
    }   

    private StringLiteral parseStringLiteral() throws SyntaxError {
        if (currentToken.kind == Token.STRINGLITERAL) {
            String spelling = currentToken.spelling;
            accept();
            return new StringLiteral(spelling, previousTokenPosition);
        } else {
            throw syntacticError("string literal expected here", "");
        }
    }   
}

