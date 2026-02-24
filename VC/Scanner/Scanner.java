/*
 * Scanner.java                        
 *
 * Mon 09 Feb 2026 16:54:28 AEDT
 *
 * The starter code here is provided as a high-level guide for implementation.
 *
 * You may completely disregard the starter code and develop your own solution, 
 * provided that it maintains the same public interface.
 *
 * STUDENT NOTES:
 * Added functions are prefixed with 2 underscores (__).
 */

package VC.Scanner;

import VC.ErrorReporter;

public final class Scanner {

    private SourceFile sourceFile;
    private ErrorReporter errorReporter;
    private boolean debug;

    private StringBuilder currentSpelling;
    private char currentChar;
    private SourcePosition sourcePos;


    // =========================================================
    private int line;
    private int col;

    private int anchorTokenLine;
    private int anchorTokenCol;

    private final int SKIP_WHITESPACE = 0;
    private final int SKIP_COMMENT_LINE = 1;
    private final int SKIP_COMMENT_MULTI = 2;

    public Scanner(SourceFile source, ErrorReporter reporter) {
        sourceFile = source;
        errorReporter = reporter;
        debug = false;

	// Initiaise currentChar for the starter code. 
	// Change it if necessary for your full implementation
	currentChar = sourceFile.getNextChar();

        // Initialise your counters for counting line and column numbers here
        line = 1;
        col = 1;
    }

    public void enableDebugging() {
        debug = true;
    }

    // accept gets the next character from the source program.
    private void accept() {

     	currentChar = sourceFile.getNextChar();

  	// You may save the lexeme of the current token incrementally here
  	// You may also increment your line and column counters here
  	col += 1;
    }


    // inspectChar returns the n-th character after currentChar in the input stream. 
    // If there are fewer than nthChar characters between currentChar 
    // and the end of file marker, SourceFile.eof is returned.
    //
    // Both currentChar and the current position in the input stream
    // are *not* changed. Therefore, a subsequent call to accept()
    // will always return the next char after currentChar.

    // That is, inspectChar does not change 

    private char inspectChar(int nthChar) {
        return sourceFile.inspectChar(nthChar);
    }

    // private void __recallRecentTokenPos() {
    //     recentTokenLine = line;
    //     recentTokenCol = col;
    // }

    // private boolean __isSingleChar(int tokenKind) {
    //     return 10 < tokenKind && tokenKind < 33;
    // }

    private int nextToken() {
        // Tokens: separators, operators, literals, identifiers, and keywords
        switch (currentChar) {
            // Handle separators
            case '(':
                currentSpelling = new StringBuilder("(");
                sourcePos = new SourcePosition(line, line, col, col);
                accept();
                return Token.LPAREN;

            case ')':
                currentSpelling = new StringBuilder(")");
                sourcePos = new SourcePosition(line, line, col, col);
                accept();
                return Token.RPAREN;
            case '[':
                currentSpelling = new StringBuilder("[");
                sourcePos = new SourcePosition(line, line, col, col);
                accept();
                return Token.LBRACKET;

            case ']':
                currentSpelling = new StringBuilder("]");
                sourcePos = new SourcePosition(line, line, col, col);
                accept();
                return Token.RPAREN;
            // ...
            case '.':
       	    // Handle floats (by calling auxiliary functions)

            // Handle separators
            case '|':
                accept();
                if (currentChar == '|') {
                    accept();
                    return Token.OROR;
                } else {
                    return Token.ERROR;
		}  
            case '&':
                accept();
                if (currentChar == '&') {
                    accept();
                    return Token.OROR;
                } else {
                    return Token.ERROR;
		}  
	    // ...
            case SourceFile.eof:
                currentSpelling.append(Token.spell(Token.EOF));
                sourcePos = new SourcePosition(line, line, col, col);
                return Token.EOF;

            default:
                break;
        }

        // Handle identifiers, keywords and numeric literals
        // ...

        if (Character.isLetter(currentChar) || currentChar == '_') {
            currentSpelling = new StringBuilder(currentChar);
            anchorTokenCol = col;
            anchorTokenLine = line;
            accept();
            boolean isValid = Character.isLetter(currentSpelling)
            || Character.isDigit(currentSpelling)
            || currentChar == '_';
            while (isValid) {
                currentSpelling.append(currentChar);
                accept();
                isValid = Character.isLetter(currentSpelling)
                || Character.isDigit(currentSpelling)
                || currentChar == '_';
            }

            sourcePos = nw SourcePosition()
        } 

        accept();
        return Token.ERROR;
    }

    private void skipSpaceAndComments(int skip_type) {
        // ...
        switch (skip_type) {
            case SKIP_WHITESPACE:
                switch (currentChar) {
                    case ' ':
                        accept();
                        break;
                    case '\n':
                        line += 1;
                        col = 1;
                        accept();
                        break;
                }
                break;
	}
    }

    private void __skipWhitespaceUnit() {
        switch (currentChar) {
            case ' ':
                accept();
                break;
            case '\n':
                accept();
                line += 1;
                col = 1;
                break;
        }
    }

    // TODO: these will make errors, write code that handles them
    private void __skipComment(boolean multiline) {
        if (multiline) {
            while (currentChar != '\n' || currentChar != SourceFile.eof) accept();
        } else {
            while (true) {
                if (currentChar == '*') {
                    accept();
                    if (currentChar == '/') break;
                }
                if (currentChar == SourceFile.eof) break;

                accept();
            }
        }
    }

    // preferably, from the initial state of the source code, it should be possible
    // for us to incrementally build the tokens within the nextToken function.
    public Token getToken() {
        Token token;
        int kind;

        // Skip white space and comments
        skipSpaceAndComments(SKIP_WHITESPACE);
        if (currentSpelling == null) {
            currentSpelling = new StringBuilder();
        }
        if (sourcePos == null) {
            sourcePos = new SourcePosition();
        }

        // You need to record the position of the current token somehow

        kind = nextToken();

        token = new Token(kind, currentSpelling.toString(), sourcePos);

        // * do not remove these three lines below (for debugging purposes)
        if (debug) {
            System.out.println(token);
        }

        sourcePos = new SourcePosition(line, line, col, col);
        currentSpelling = new StringBuilder();
        return token;
    }
}
