lexer grammar TerraformLexer;

// BEGIN KEYWORDS

OUTPUT
   : 'output'
   ;

MODULE
   : 'module'
   ;

MOVED
   : 'moved'
   ;

LOCALS
   : 'locals'
   ;

RESOURCE
   : 'resource'
   ;

TERRAFORM
   : 'terraform'
   ;

DATA
   : 'data'
   ;

VAR
   : 'var'
   ;

LOCAL
   : 'local'
   ;

VARIABLE
   : 'variable'
   ;

PROVIDER
   : 'provider'
   ;

ARROW
    : '=>'
    ;

IF
    : 'if'
    ;

IN
   : 'in'
   ;

STAR
   : '*'
   ;

DOT
   : '.'
   ;

ASSIGNMENT
    : '='
    ;

COMMA
    : ','
    ;

NEGATE
    : '!'
    ;

COLON
    : ':'
    ;

FOR
    : 'for'
    ;

QUESTION
    : '?'
    ;

LBRACK
   : '['
   ;

RBRACK
   : ']'
   ;

LCURL
   : '{'
   ;

RCURL
   : '}'
   ;

LPAREN
   : '('
   ;

RPAREN
   : ')'
   ;

DYNAMIC
   : 'dynamic'
   ;

NULL_
   : 'null'
   ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

FORWARD_SLASH
    : '/'
    ;

PERCENT
    : '%'
    ;

GT
    : '>'
    ;

GTE
    : '>='
    ;

LT
    : '<'
    ;

LTE
    : '<='
    ;

EQUALS
    : '=='
    ;

 NOT_EQUALS
    : '!='
    ;

 AND
    : '&&'
    ;

 OR
    : '||'
    ;

EOF_
   : '<<EOF' .*? 'EOF'
   ;

BOOL
   : 'true'
   | 'false'
   ;


// BEGIN NUMBER PARSING

NUMBER
   : [0-9]+ DOT [0-9]*
   | [0-9]+
   ;


// BEGIN STRING PARSING

IDENTIFIER
   : [a-zA-Z_] ([a-zA-Z0-9_-])*
   ;

STRING
   : '"' ( '\\"' | ~["\r\n] )* '"'
   ;

// BEGIN COMMENT PARSING

COMMENT
  : ('#' | '//') ~ [\r\n]* -> channel(HIDDEN)
  ;

BLOCKCOMMENT
  : '/*' .*? '*/' -> channel(HIDDEN)
  ;

// BEGIN WHITESPACE PARSING

WS
   : [ \r\n\t]+ -> skip
   ;

