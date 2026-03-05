# PARSER PROGRESS GOES HERE

Blockquoted are either aspects of the grammar that are to be changed from the starter
or are the changes that must be implemented from the starter code to their current variant.

Overall Structure
-----------------

- [ ] Change program grammar implemented
> FROM: program -> ( func-decl | var-decl )*
> TO: program -> func-decl

Declarations
------------

- [ ] Change function grammar implemented
> FROM: func-decl -> "void" identifier paralist compound-stmt
> TO: func-decl -> type identifier para-list compound-stmt

- [ ] implement matching variable declaration grammar

- [ ] implement initialisation declarators

- [ ] implement type matching

Statements
----------

Starter code only has continue statements and expressions implemented
for the overall statement parsing.
> compound-stmt -> "{" stmt* "}"
> stmt          -> continue-stmt
>     	      |  expr-stmt
> continue-stmt -> continue ";"
> expr-stmt     -> expr? ";"

- [ ] Change compound statement declaration
> FROM: compound-stmt -> "{" stmt* "}"
> TO: compound-stmt -> "{" var-decl* stmt* "}"

The following statement variants are assigned to `stmt`.
- [x] compound-stmt (in starter code, needs expansion)
- [x] continue-stmt (implemented in starter)
- [ ] if-stmt
- [ ] for-stmt
- [ ] while-stmt
- [ ] break-stmt
- [ ] return-stmt
- [ ] expr-stmt

Expressions
-----------

The starter grammar looks like this.
> expr                -> assignment-expr
> assignment-expr     -> additive-expr
> additive-expr       -> multiplicative-expr
>                     |  additive-expr "+" multiplicative-expr
> multiplicative-expr -> unary-expr
>                     |  multiplicative-expr "*" unary-expr
> unary-expr          -> "-" unary-expr
>                     |  primary-expr
>
> primary-expr        -> identifier
>                     |  INTLITERAL
>                     | "(" expr ")"

In summary,

- Only additive and multiplicative expressions are implemented
if not limited.
- Nesting via parenthesis and identifiers is in there.

- [ ] Change implemented `assignment-expr` implemented.
> FROM: assignment-expr -> additive-expr
> TO: assignment-expr     -> ( cond-or-expr "=" )* cond-or-expr

These are the nonterms described in both with their implementation
status.

- [x] assignment-expr (in starter code, needs expansion)
- [ ] cond-or-expr
- [ ] cond-and-expr
- [ ] equality-expr
- [ ] rel-expr
- [ ] additive-expr (in starter code, needs expansion)
- [ ] multiplicative-expr
- [ ] unary-expr
- [ ] primary-expr (in starter code, needs expansion)

Parameter List
--------------

This one is straight-up unimplemented.

- [ ] Implement parameter list.

# Approach Proposal

Start with the simple ones first, as these will become building
blocks toward the more complex ones.
1. primary-expr -> unary-expr -> additive-expr

Each parse function `parse<grammarName>()` seem to correspond with
an actual production in VC grammar.
So a solid approach is to implement each production one-by-one.
