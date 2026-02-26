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

  // strictly defined tokens, for cleaner logic in nextToken()
  // is a subsequence of Token.keywords
  private static final String[] CONST_KEYWORDS = new String[] {
    "boolean",
    "break",
    "continue",
    "else",
    "float",
    "for",
    "if",
    "int",
    "return",
    "void",
    "while",
    "+",
    "-",
    "*",
    "/",
    "!",
    "!=",
    "=",
    "==",
    "<",
    "<=",
    ">",
    ">=",
    "&&",
    "||",
    "{",
    "}",
    "(",
    ")",
    "[",
    "]",
    ";",
    ",",
  };

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

    // EOF is unacceptable
    if (currentChar == SourceFile.eof) return;
    if (currentChar == '\n') {
      col = 1;
      line += 1;
    } else col += 1;

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

  private int nextToken() {
    // Tokens: separators, operators, literals, identifiers, and keywords
    switch (currentChar) {
      // Handle singlechar tokens
      case '(':
      case ')':
      case '[':
      case ']':
      case '{':
      case '}':
      case ';':
      case ',':
      case '+':
      case '-':
      case '*':
      case '/':
        accept();
        return __spellingToTokenKind();
      // ...

      case '=':
      case '<':
      case '>':
      case '!':
        accept();
        if (currentChar == '=') accept();
        return __spellingToTokenKind();

      // the rest of these tokens aren't necessarily singlechar,
      // but begins with a specific symbol
      case '.':
        __acceptFloat();
        return Token.FLOATLITERAL;
      case '|':
        accept();
        if (currentChar == '|') {
          accept();
          return Token.OROR;
        } else {
          return Token.ERROR;
        }  
      case '&':
        accept(); if (currentChar == '&') accept();
        return __spellingToTokenKind();

      case '"':
        accept();
        while (currentChar != '"') {
          if (!__isValidStringChar(currentChar)) return Token.ERROR;
          else if (currentChar == '\\') {
            accept();
            switch (currentChar) {
              case 'b':
              case 'f':
              case 'n':
              case 'r':
              case 't':
              case '\'':
              case '"':
              case '\\':
                accept();
                break;
              default: return Token.ERROR;
            }
          } else accept();
        }
        accept();
        return Token.STRINGLITERAL;
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

    if (__isNumeric(currentChar)) {
      boolean isFloat = false;
      accept();
      while (__isNumeric(currentChar)) accept();
      if (currentChar == '.' || currentChar == 'E' || currentChar == 'e') {
        __acceptFloat();
      }
      if (isFloat) return Token.FLOATLITERAL; else return Token.INTLITERAL;
    }

    if (__isValidAsciiIdChar(currentChar, true)) {
      accept();
      boolean isValid = __isValidAsciiIdChar(currentChar);
      while (isValid) {
        accept();
        isValid = __isValidAsciiIdChar(currentChar);
      }
      if (__spellingToTokenKind() == Token.ERROR) return Token.ID;
      else return __spellingToTokenKind();
    }

    accept();
    return Token.ERROR;
  }

  // copies Token class' spelling-to-kind conversion functionality
  // this is designed to simplify lookups and conversions of nextToken()
  private int __spellingToTokenKind() {
    for (int i = 0; i < CONST_KEYWORDS.length; i++)
      if (CONST_KEYWORDS[i].equals(currentSpelling.toString())) return i;
    return Token.ERROR;
  }

  // @note: logic here is a bit finicky, will look for improvement in next assignment
  private void __acceptFloat() {
    // int expFoundState = 0;
    if (currentChar == '.') accept();
    while (__isNumeric(currentChar)) accept();
    // only these kind of exp suffixes are valid
    // E1234...
    // E+1234...
    // E-1234...
    // i.e., numbers must always come after them
    if (currentChar == 'e' || currentChar == 'E') {
      if (inspectChar(1) == '+' || inspectChar(1) == '-') {
        if (__isNumeric(inspectChar(2))) { accept(); accept(); }
        else return;
        while (__isNumeric(currentChar)) accept();
      }
      if (__isNumeric(inspectChar(1))) {
        accept();
        while (__isNumeric(currentChar)) accept();
      }
      else return;
    }
    // while (__isNumeric(currentChar)) accept();
    // return Token.FLOATLITERAL;
  }

  // all chars in string literals incl. escape characters are technically printable
  // (https://www.ascii-code.com/ for reference)
  private boolean __isValidStringChar(char c) {
    return c >= ' ' && c <= '~'; 
  }

  private boolean __isNumeric(char c) {
    return c >= '0' && c <= '9';
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
    while (true) {
      if (currentChar == ' ' || currentChar == '\n' || currentChar == '\t') accept(false);
      else if (currentChar == '/') {
        accept(false);
        __skipComment();
        // @note: allow for consecutive single line comments
        // break;
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
