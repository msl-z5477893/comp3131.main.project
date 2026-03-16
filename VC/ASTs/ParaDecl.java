/*
 * ParaDecl.java      
 *
 *
 * See t12.vc and t13.vc.
 */

package VC.ASTs;

import VC.Scanner.SourcePosition;

public class ParaDecl extends Decl {

  public ParaDecl (Type tAST, Ident idAST, SourcePosition position) {
    super (position);
    T = tAST;
    I = idAST;
    T.parent = I.parent = this;
  }

  public String toString() {
    return "ParaDecl(" + T + " " + I + ")";
  }

  public Object visit(Visitor v, Object o) {
    return v.visitParaDecl(this, o);
  }

}
