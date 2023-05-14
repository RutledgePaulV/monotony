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


// NEED TO PUSH/POP default mode
LCURL: '{' -> pushMode(DEFAULT_MODE);
RCURL: '}' -> popMode;
DQUOTE: '"' -> pushMode(IN_STRING);


mode IN_STRING;
IN_STRING_ESCAPE_INTERPOLATE: '$${';
IN_STRING_ESCAPE_DQUOTE: '\\"' ;
IN_STRING_INTERPOLATE: '${' -> pushMode(EMBEDDED);
IN_STRING_DOLLAR: '$' ;
IN_STRING_DQUOTE: '"' -> type(DQUOTE), popMode;
IN_STRING_RCURL: '}' -> type(RCURL), popMode;
TEXT: ( IN_STRING_DOLLAR | IN_STRING_ESCAPE_DQUOTE | IN_STRING_ESCAPE_INTERPOLATE | ~["\r\n$]+ ) ;

mode EMBEDDED;
E_LCURL: '{' -> type(LCURL), pushMode(DEFAULT_MODE);
E_RCURL: '}' -> type(RCURL), popMode;
E_DQUOTE: '"' -> type(DQUOTE), pushMode(IN_STRING);


E_OUTPUT
   : 'output' -> type(OUTPUT)
   ;

E_MODULE
   : 'module' -> type(MODULE)
   ;

E_MOVED
   : 'moved' -> type(MOVED)
   ;

E_LOCALS
   : 'locals' -> type(LOCALS)
   ;

E_RESOURCE
   : 'resource' -> type(RESOURCE)
   ;

E_TERRAFORM
   : 'terraform' -> type(TERRAFORM)
   ;

E_DATA
   : 'data' -> type(DATA)
   ;

E_VAR
   : 'var' -> type(VAR)
   ;

E_LOCAL
   : 'local' -> type(LOCAL)
   ;

E_VARIABLE
   : 'variable' -> type(VARIABLE)
   ;

E_PROVIDER
   : 'provider' -> type(PROVIDER)
   ;

E_ARROW
    : '=>' -> type(ARROW)
    ;

E_IF
    : 'if' -> type(IF)
    ;

E_IN
   : 'in' -> type(IN)
   ;

E_STAR
   : '*' -> type(STAR)
   ;

E_DOT
   : '.' -> type(DOT)
   ;

E_ASSIGNMENT
    : '=' -> type(ASSIGNMENT)
    ;

E_COMMA
    : ',' -> type(COMMA)
    ;

E_NEGATE
    : '!' -> type(NEGATE)
    ;

E_COLON
    : ':' -> type(COLON)
    ;

E_FOR
    : 'for' -> type(FOR)
    ;

E_QUESTION
    : '?' -> type(QUESTION)
    ;

E_LBRACK
   : '[' -> type(LBRACK)
   ;

E_RBRACK
   : ']' -> type(RBRACK)
   ;

E_LPAREN
   : '(' -> type(LPAREN)
   ;

E_RPAREN
   : ')' -> type(RPAREN)
   ;

E_DYNAMIC
   : 'dynamic' -> type(DYNAMIC)
   ;

E_NULL_
   : 'null' -> type(NULL_)
   ;

E_PLUS
    : '+' -> type(PLUS)
    ;

E_MINUS
    : '-' -> type(MINUS)
    ;

E_FORWARD_SLASH
    : '/' -> type(FORWARD_SLASH)
    ;

E_PERCENT
    : '%' -> type(PERCENT)
    ;

E_GT
    : '>' -> type(GT)
    ;

E_GTE
    : '>=' -> type(GTE)
    ;

E_LT
    : '<' -> type(LT)
    ;

E_LTE
    : '<=' -> type(LTE)
    ;

E_EQUALS
    : '==' -> type(EQUALS)
    ;

E_NOT_EQUALS
    : '!=' -> type(NOT_EQUALS)
    ;

E_AND
    : '&&' -> type(AND)
    ;

E_OR
    : '||' -> type(OR)
    ;

E_EOF_
   : '<<EOF' .*? 'EOF' -> type(EOF_)
   ;

E_BOOL
   : ('true' | 'false') -> type(BOOL)
   ;


// BEGIN NUMBER PARSING

E_NUMBER
   : ([0-9]+ DOT [0-9]* | [0-9]+) -> type(NUMBER)
   ;


// BEGIN STRING PARSING

E_IDENTIFIER
   : [a-zA-Z_] ([a-zA-Z0-9_-])* -> type(IDENTIFIER)
   ;


// BEGIN COMMENT PARSING

E_COMMENT
  : ('#' | '//') ~ [\r\n]* -> type(COMMENT), channel(HIDDEN)
  ;

E_BLOCKCOMMENT
  : '/*' .*? '*/' -> type(BLOCKCOMMENT), channel(HIDDEN)
  ;

// BEGIN WHITESPACE PARSING

E_WS
   : [ \r\n\t]+ -> skip
   ;