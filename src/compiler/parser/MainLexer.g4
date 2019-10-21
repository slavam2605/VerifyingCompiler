lexer grammar MainLexer;

LPAREN:         '(';
RPAREN:         ')';
LBRACE:         '{';
RBRACE:         '}';
SEMICOLON:      ';';
COLON:          ':';
EQUAL:          '=';
COMMA:          ',';

FUN_KW:         'fun';
VAR_KW:         'var';

IDENT:          [a-zA-Z_]+ [a-zA-Z0-9_]*;
INT:            [0-9]+;
WS:             [ \t\n\r]+ -> skip;
COMMENT:        '/*' .*? '*/' -> skip;
LINE_COMMENT:   '//' ~'\n'* '\n' -> skip;