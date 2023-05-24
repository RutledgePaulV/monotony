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
    | expression
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
   : assignment
   | namespaced_block
   | dynamic_block
   | typed_block
   ;

assignment
   : identifier ASSIGNMENT expression
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
   | RESOURCE
   ;

string_content
   : IN_STRING_INTERPOLATE expression RCURL
   | IN_STRING_ESCAPE_DQUOTE
   | IN_STRING_ESCAPE_INTERPOLATE
   | IN_STRING_DOLLAR
   | RCURL
   | LCURL
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
     : LBRACK (expression (COMMA expression)* COMMA?)? RBRACK # list_entries
     | LBRACK FOR identifier IN expression COLON expression RBRACK # list_comprehension
     | LBRACK FOR identifier IN expression COLON expression IF expression RBRACK # conditional_list_comprehension
     | LBRACK FOR identifier COMMA identifier IN expression COLON expression RBRACK # destructured_list_comprehension
     | LBRACK FOR identifier COMMA identifier IN expression COLON expression IF expression RBRACK # conditional_destructured_list_comprehension
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
    : LCURL (map_entry COMMA?)* RCURL # map_entries
    | LCURL FOR identifier IN expression COLON expression ARROW expression RCURL # map_comprehension
    | LCURL FOR identifier IN expression COLON expression ARROW expression IF expression RCURL # conditional_map_comprehension
    | LCURL FOR identifier COMMA identifier IN expression COLON expression ARROW expression RCURL # destructured_map_comprehension
    | LCURL FOR identifier COMMA identifier IN expression COLON expression ARROW expression IF expression RCURL # conditional_destructured_map_comprehension
    ;


expression
    : expression PLUS expression # expression_plus
    | expression MINUS expression # expression_minus
    | expression STAR expression # expression_star
    | expression FORWARD_SLASH expression # expression_forward_slash
    | expression PERCENT expression # expression_percent
    | expression EQUALS expression # expression_equals
    | expression NOT_EQUALS expression # expression_not_equals
    | expression LT expression # expression_lt
    | expression GT expression # expression_gt
    | expression LTE expression # expression_lte
    | expression GTE expression # expression_gte
    | expression AND expression # expression_and
    | expression OR expression # expression_or
    | function_call # function_call_
    | list_ # list__
    | map_ # map__
    | LPAREN expression RPAREN # expression_paren
    | NEGATE expression # expression_negate
    | null_literal # null_literal_
    | string_literal # string_literal_
    | number_literal # number_literal_
    | boolean # boolean_
    | identifier # identifier_
    | expression QUESTION expression COLON expression # expression_ternary
    | expression DOT (identifier | STAR) # expression_dot
    | expression LBRACK (expression | STAR) RBRACK # expression_bracket
    ;

block_name
   : identifier
   | string_literal
   ;

block_type
   : identifier
   | string_literal
   ;