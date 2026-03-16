/*
 * CompoundStmt.java       
 */

package VC.ASTs;

import VC.Scanner.SourcePosition;

public class CompoundStmt extends Stmt {

  public ASTList<Decl> DL;
  public ASTList<Stmt> SL;

  public CompoundStmt(ASTList<Decl> dlAST, ASTList<Stmt> slAST, SourcePosition position) {
    super (position);
    DL = dlAST;
    SL = slAST;
    DL.parent = SL.parent = this;
  }

  public Object visit(Visitor v, Object o) {
    return v.visitCompoundStmt(this, o);
  }

}
