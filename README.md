Some tasks I'm gonna list here, IDK.

- [x] Fix errors in Parser.java

Implement Visitor overrides in Checker.

## Programs
- [ ] visitProgram
Stubbed, implementation incomplete.

## Lists for denoting the null reference

- [x] visitEmptyDeclList
- [x] visitEmptyStmtList
- [ ] visitEmptyArrayExprList
- [x] visitEmptyParaList
- [ ] visitEmptyArgList

## Declarations
- [x] visitDeclList
DeclList (and other AST list in general) are implemented linked-list style,
so it's guaranteed this is correctly implemented.
- [ ] visitFuncDecl
Stubbed, incomplete implementation.
What is currently within the scope is for
the identification subphase.
- [ ] visitGlobalVarDecl
Commented as incomplete, unsure what's missing though.
- [ ] visitLocalVarDecl
Same as `visitGlobalVarDecl`.

## Stmts
- [x] visitStmtList
Implied complete as per assignment spec.
- [ ] visitIfStmt
- [ ] visitWhileStmt
- [ ] visitForStmt
- [ ] visitBreakStmt
- [ ] visitContinueStmt
- [ ] visitReturnStmt
- [ ] visitCompoundStmt
Subbed, implementation incomplete.
- [x] visitExprStmt
Most likely complete, any issues that may involve this function is either
rare or should not have happened.
- [ ] visitEmptyCompStmt
- [x] visitEmptyStmt

## Expressions
- [x] visitIntExpr
- [x] visitFloatExpr
- [x] visitBooleanExpr
- [x] visitStringExpr
I assume these four are implemented correctly, but the fact that literals
exist don't exactly makes this easy.
- [ ] visitUnaryExpr
- [ ] visitBinaryExpr
- [ ] visitArrayInitExpr
- [ ] visitArrayExprList
- [ ] visitArrayExpr
- [x] visitVarExpr
I'm not sure what this one does, so while we consider it implemented, probably
give it a lookback just in case it all goes wrong.
- [ ] visitCallExpr
- [ ] visitAssignExpr
- [x] visitEmptyExpr

## Literals and identifiers
- [x] visitIntLiteral
- [x] visitFloatLiteral
- [x] visitBooleanLiteral
- [x] visitStringLiteral
- [x] visitIdent
- [x] visitOperator

## Parameters

- [x] visitParaList
- [x] visitParaDecl

## Arguments
- [ ] visitArgList
- [ ] visitArg

## Types
- [x] visitVoidType
- [x] visitBooleanType
- [x] visitIntType
- [x] visitFloatType
- [x] visitStringType
- [x] visitArrayType
- [x] visitErrorType


## Variables
- [ ] visitSimpleVar
