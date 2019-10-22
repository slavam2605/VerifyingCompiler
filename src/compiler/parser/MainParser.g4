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
    :   'fun' name=IDENT '(' (parameters += functionParameter (',' parameters += functionParameter)*)? ')'
        inputContract=functionContract? ':' returnType=type outputContract=functionContract? codeBlock
    ;

codeBlock
    :   '{' (statements += codeStatement)* '}'
    ;

codeStatement
    :   'var' name=IDENT ':' type ('=' codeExpression)?     #varDeclaration
    ;

codeExpression
    :   INT                                                 #intLiteral
    |   name=IDENT                                          #symbolReference
    |   '!' codeExpression                                  #negate
    |   '(' codeExpression ')'                              #paren
    |   left=codeExpression '&' right=codeExpression        #and
    |   left=codeExpression '|' right=codeExpression        #or
    |   left=codeExpression '->' right=codeExpression       #arrow
    ;

functionContract
    :   '[' (expressions += codeExpression (',' expressions += codeExpression)*)? ']'
    ;