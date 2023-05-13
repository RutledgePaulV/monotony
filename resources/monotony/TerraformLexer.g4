lexer grammar TerraformLexer;

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

EQUALS
    : '='
    ;

COMMA
    : ','
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

EOF_
   : '<<EOF' .*? 'EOF'
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

SLASH
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

EQUALITY
    : '=='
    ;

 NOT_EQUALITY
    : '!='
    ;

 AND
    : '&&'
    ;

 OR
    : '||'
    ;

fragment DIGIT
   : [0-9]
   ;

NATURAL_NUMBER
   : DIGIT+
   ;

BOOL
   : 'true'
   | 'false'
   ;

MULTILINESTRING
   : '<<-EOF' .*? 'EOF'
   ;

STRING
   : '"' ( '\\"' | ~["\r\n] )* '"'
   ;

IDENTIFIER
   : [a-zA-Z] ([a-zA-Z0-9_-])*
   ;

COMMENT
  : ('#' | '//') ~ [\r\n]* -> channel(HIDDEN)
  ;

BLOCKCOMMENT
  : '/*' .*? '*/' -> channel(HIDDEN)
  ;

WS
   : [ \r\n\t]+ -> skip
   ;
