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
    private void accept(boolean save) {

        // EOF is unacceptable
        if (currentChar == SourceFile.eof) return;
        if (currentChar == '\n') {
            col = 1;
            line += 1;
        } else col += 1;


  	// You may save the lexeme of the current token incrementally here
  	// You may also increment your line and column counters here
        if (save) {
            currentSpelling.append(currentChar);

            boolean isSourcePosDefault = sourcePos.charStart == 0 &&
                                         sourcePos.charFinish == 0 &&
                                         sourcePos.lineStart == 0 &&
                                         sourcePos.lineFinish == 0;

            if (isSourcePosDefault) sourcePos = new SourcePosition(line, line, col, col);
            else {
                // TODO: this implementation is potentially buggy
                sourcePos.charFinish += 1;
            }
  	}

     	currentChar = sourceFile.getNextChar();
    }

    private void accept() { accept(true); }


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
                accept();
                return Token.LPAREN;

            case ')':
                accept();
                return Token.RPAREN;
            case '[':
                accept();
                return Token.LBRACKET;

            case ']':
                accept();
                return Token.RBRACKET;
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

        if (__isValidAsciiIdChar(currentChar, true)) {
            accept();
            boolean isValid = __isValidAsciiIdChar(currentChar);
            while (isValid) {
                accept();
                isValid = __isValidAsciiIdChar(currentChar);
            }
        } 

        accept();
        return Token.ERROR;
    }

    private boolean __isValidAsciiIdChar(char c, boolean isStarting) {
        boolean starting = (c >= 'a' && c <= 'z') ||
                           (c >= 'A' && c <= 'Z') ||
                           c == '_';
       if (!isStarting) return starting ||
                               (c >= '0' && c <= '9');
       else return starting;
    }

    private boolean __isValidAsciiIdChar(char c) {
        return __isValidAsciiIdChar(c, false);
    }

    private void skipSpaceAndComments() {
    /*   switch (skip_type) {
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
    }*/
        while (true) {
            if (currentChar == ' ' && currentChar == '\n') accept(false);
            else if (currentChar == '/') {
                accept(false);
                __skipComment();
                break;
            }
            else break;
        }
    }

    // FIX: give error if multiline comment is left unterminated
    private void __skipComment() {
        if (currentChar == '/') {
            while (currentChar != '\n') accept(false);
            accept(false);
        } else if (currentChar == '*') {
            accept(false);
            int toExitState = 0;
            while (toExitState != 2) {
                if (currentChar == '/' && toExitState == 1) toExitState = 2;
                else if (currentChar == '*') toExitState = 1;
                else if (currentChar == SourceFile.eof) return;
                else toExitState = 0;
                accept(false);
            }
        }
    }

    public Token getToken() {
        Token token;
        int kind;

        // Skip white space and comments
        skipSpaceAndComments();
        currentSpelling = new StringBuilder();
        sourcePos = new SourcePosition();

        // You need to record the position of the current token somehow

        kind = nextToken();

        token = new Token(kind, currentSpelling.toString(), sourcePos);

        // * do not remove these three lines below (for debugging purposes)
        if (debug) {
            System.out.println(token);
        }

        return token;
    }
}
