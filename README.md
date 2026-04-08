Some tasks I'm gonna list here, IDK.

- [x] Fix errors in Parser.java

Complete Type Checker by
implementing Visitor overrides in Checker.

## Programs
- [x] visitProgram
Stubbed, implementation incomplete.

## Lists for denoting the null reference

- [x] visitEmptyDeclList
- [x] visitEmptyStmtList
- [x] visitEmptyArrayExprList
- [x] visitEmptyParaList
- [x] visitEmptyArgList

## Declarations
- [x] visitDeclList
DeclList (and other AST list in general) are implemented linked-list style,
so it's guaranteed this is correctly implemented.
- [ ] visitFuncDecl
Stubbed, incomplete implementation.
What is currently within the scope is for
the identification subphase.
- [x] visitGlobalVarDecl
Commented as incomplete, unsure what's missing though.
- [x] visitLocalVarDecl
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
- [x] visitCompoundStmt
Stubbed, implementation incomplete.
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
- [x] visitUnaryExpr
- [x] visitBinaryExpr
- [ ] visitArrayInitExpr
- [ ] visitArrayExprList
- [x] visitArrayExpr
- [x] visitVarExpr
I'm not sure what this one does, so while we consider it implemented, probably
give it a lookback just in case it all goes wrong.
- [x] visitCallExpr
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

- [ ] visitParaList
- [x] visitParaDecl

## Arguments
- [x] visitArgList
- [x] visitArg

## Types
- [x] visitVoidType
- [x] visitBooleanType
- [x] visitIntType
- [x] visitFloatType
- [x] visitStringType
- [x] visitArrayType
- [x] visitErrorType


## Variables
- [x] visitSimpleVar
