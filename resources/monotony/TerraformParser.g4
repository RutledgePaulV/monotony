parser grammar TerraformParser;

options {
    tokenVocab = 'TerraformLexer';
}

file_
   : (local | module | output | provider | variable | data | resource | moved | terraform)* EOF
   ;

terraform
   : TERRAFORM blockbody
   ;

resource
   : RESOURCE resourcetype name blockbody
   ;

moved
   : MOVED blockbody
   ;

data
   : DATA resourcetype name blockbody
   ;

provider
  : PROVIDER resourcetype blockbody
  ;

output
  : OUTPUT name blockbody
  ;

local
  : LOCALS blockbody
  ;

module
  : MODULE name blockbody
  ;

variable
   : VARIABLE name blockbody
   ;

block
   : blocktype label? blockbody
   ;

blocktype
   : IDENTIFIER
   ;

resourcetype
   : STRING
   | IDENTIFIER
   ;

name
   : STRING
   | IDENTIFIER
   ;

label
   : STRING
   ;

blockbody
   : LCURL (blockargument | block)* RCURL
   ;

blockargument
   : identifier EQUALS expression
   ;

mapkey
   : identifier
   | expression
   | STRING
   | LPAREN expression RPAREN
   ;

mapentry
   : mapkey EQUALS expression
   | mapkey COLON expression
   ;

keyword
   : IN
   | VARIABLE
   | PROVIDER
   | DATA
   | MODULE
   | TERRAFORM
   | LOCAL
   | OUTPUT
   | MOVED
   | RESOURCE
   | VAR
   | LOCALS
   ;

identifier
   : (IDENTIFIER | keyword)
   ;

inline_index
   : NATURAL_NUMBER
   ;

expression
   : list_
   | map_
   | NULL_
   | BOOL
   | signed_number
   | string
   | identifier
   | functioncall
   | expression DOT inline_index
   | expression DOT identifier
   | expression LBRACK STAR RBRACK
   | expression LBRACK expression RBRACK
   | expression operator_ expression
   | LPAREN expression RPAREN
   | expression QUESTION expression COLON expression
   ;

functioncall
   : IDENTIFIER LPAREN expression? (COMMA expression)* COMMA? RPAREN
   ;

list_
   : LBRACK (expression (COMMA expression)* COMMA?)? RBRACK
   | LBRACK FOR identifier IN expression COLON expression RBRACK
   | LBRACK FOR identifier IN expression COLON expression IF expression RBRACK
   | LBRACK FOR identifier COMMA identifier IN expression COLON expression RBRACK
   | LBRACK FOR identifier COMMA identifier IN expression COLON expression IF expression RBRACK
   ;

map_
   : LCURL (mapentry COMMA?)* RCURL
   | LCURL FOR identifier IN expression COLON mapkey ARROW expression RCURL
   | LCURL FOR identifier IN expression COLON mapkey ARROW expression IF expression RCURL
   | LCURL FOR identifier COMMA identifier IN expression COLON mapkey ARROW expression RCURL
   | LCURL FOR identifier COMMA identifier IN expression COLON mapkey ARROW expression IF expression RCURL
   ;

string
   : STRING
   | MULTILINESTRING
   ;

signed_number
   : (PLUS | MINUS)? number
   ;

operator_
   : SLASH
   | STAR
   | PERCENT
   | PLUS
   | MINUS
   | GT
   | GTE
   | LT
   | LTE
   | EQUALITY
   | NOT_EQUALITY
   | AND
   | OR
   ;

number
   : NATURAL_NUMBER (DOT NATURAL_NUMBER)?
   ;
