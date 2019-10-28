lexer grammar MainLexer;

LPAREN:         '(';
RPAREN:         ')';
LBRACE:         '{';
RBRACE:         '}';
LSQBRACKET:     '[';
RSQBRACKET:     ']';
SEMICOLON:      ';';
COLON:          ':';
EQUAL:          '=';
GREATER:        '>';
GREATEREQ:      '>=';
LESS:           '<';
LESSEQ:         '<=';
EQEQ:           '==';
NOTEQ:          '!=';
COMMA:          ',';
EXCL:           '!';
AND:            '&';
OR:             '|';
STAR:           '*';
SLASH:          '/';
PLUS:           '+';
MINUS:          '-';
ARROW:          '->';
APPEND_ARROW:   '+>';

FUN_KW:         'fun';
VAR_KW:         'var';
TRUE_KW:        'true';
FALSE_KW:       'false';

IDENT:          [a-zA-Z_]+ [a-zA-Z0-9_]*;
INT:            '-'? [0-9]+;
WS:             [ \t\n\r]+ -> skip;
COMMENT:        '/*' .*? '*/' -> skip;
LINE_COMMENT:   '//' ~'\n'* '\n' -> skip;