parser grammar MainParser;

options {
    tokenVocab = MainLexer;
}

type
    :   name=IDENT
    ;

functionParameter
    :   name=IDENT ':' type
    ;

function
    :   'fun' name=IDENT '(' (parameters += functionParameter (',' parameters += functionParameter)*)? ')' ':' returnType=type codeBlock
    ;

codeBlock
    :   '{' (statements += codeStatement) '}'
    ;

codeStatement
    :   'var' name=IDENT ':' type ('=' codeExpression)? #varDeclaration
    ;

codeExpression
    :   INT #intLiteral
    ;