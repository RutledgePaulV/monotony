parser grammar TerraformParser;

options {
    tokenVocab = 'TerraformLexer';
}

file_ : (top_level_block)* EOF ;

top_level_block
    : variable_block
    | resource_block
    | data_block
    | module_block
    | output_block
    | provider_block
    | terraform_block
    | locals_block
    | moved_block
    | string_literal
    ;



 variable_block
    : VARIABLE block_name LCURL (block_entry)* RCURL
    ;

 locals_block
     : LOCALS LCURL (block_entry)* RCURL
     ;

 terraform_block
     : TERRAFORM LCURL (block_entry)* RCURL
     ;

 provider_block
     : PROVIDER block_type LCURL (block_entry)* RCURL
     ;

 output_block
     : OUTPUT block_name LCURL (block_entry)* RCURL
     ;

 module_block
     : MODULE block_name LCURL (block_entry)* RCURL
     ;

 moved_block
     : MOVED LCURL (block_entry)* RCURL
     ;

 data_block
     : DATA block_type block_name LCURL (block_entry)* RCURL
     ;

 resource_block
     : RESOURCE block_type block_name LCURL (block_entry)* RCURL
     ;

block_entry
   : identifier ASSIGNMENT expression
   | inner_block
   ;

dynamic_block
   : DYNAMIC block_name LCURL (block_entry)* RCURL
   ;

typed_block
    : block_type block_name LCURL (block_entry)* RCURL
    ;

namespaced_block
    : identifier LCURL (block_entry)* RCURL
    ;

inner_block
   : namespaced_block
   | typed_block
   | dynamic_block
   ;

identifier
   : IDENTIFIER
   | MODULE
   | LOCAL
   | VAR
   | DATA
   | VARIABLE
   | TERRAFORM
   | PROVIDER
   | DYNAMIC
   ;

string_content
   : IN_STRING_INTERPOLATE expression RCURL
   | IN_STRING_ESCAPE_DQUOTE
   | IN_STRING_ESCAPE_INTERPOLATE
   | IN_STRING_DOLLAR
   | TEXT
   ;


string_literal
    : DQUOTE string_content* DQUOTE
    ;

number_literal
    : NUMBER
    | PLUS NUMBER
    | MINUS NUMBER
    ;

null_literal
    : NULL_
    ;

boolean
    : BOOL
    ;

function_call
    : identifier LPAREN (expression (COMMA expression)* COMMA?)? RPAREN
    ;

list_
     : LBRACK (expression (COMMA expression)* COMMA?)? RBRACK
     | LBRACK FOR identifier IN expression COLON expression RBRACK
     | LBRACK FOR identifier IN expression COLON expression IF expression RBRACK
     | LBRACK FOR identifier COMMA identifier IN expression COLON expression RBRACK
     | LBRACK FOR identifier COMMA identifier IN expression COLON expression IF expression RBRACK
    ;

map_key
    : identifier
    | string_literal
    | LPAREN expression RPAREN
    ;

map_entry
    : map_key (ASSIGNMENT | COLON) expression
    ;

map_
    : LCURL (map_entry COMMA?)* RCURL
    | LCURL FOR identifier IN expression COLON expression ARROW expression RCURL
    | LCURL FOR identifier IN expression COLON expression ARROW expression IF expression RCURL
    | LCURL FOR identifier COMMA identifier IN expression COLON expression ARROW expression RCURL
    | LCURL FOR identifier COMMA identifier IN expression COLON expression ARROW expression IF expression RCURL
    ;

expression
    : expression PLUS expression
    | expression MINUS expression
    | expression STAR expression
    | expression FORWARD_SLASH expression
    | expression PERCENT expression
    | expression EQUALS expression
    | expression NOT_EQUALS expression
    | expression LT expression
    | expression GT expression
    | expression LTE expression
    | expression GTE expression
    | expression AND expression
    | expression OR expression
    | function_call
    | list_
    | map_
    | LPAREN expression RPAREN
    | NEGATE expression
    | null_literal
    | string_literal
    | number_literal
    | boolean
    | identifier
    | expression QUESTION expression COLON expression
    | expression DOT (identifier | STAR)
    | expression LBRACK (expression | STAR) RBRACK
    ;

block_name
   : identifier
   | string_literal
   ;

block_type
   : identifier
   | string_literal
   ;