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
    |   '+>' codeExpression                                 #proofElement
    ;

expressionList
    :   (expressions += codeExpression (',' expressions += codeExpression)*)?
    ;

codeExpression
    :   INT                                                                         #intLiteral
    |   name=IDENT '(' expressionList ')'                                           #invocation
    |   name=IDENT                                                                  #symbolReference
    |   '!' codeExpression                                                          #negate
    |   '(' codeExpression ')'                                                      #paren
    |   left=codeExpression op=('<'|'>'|'<='|'>='|'=='|'!=') right=codeExpression   #comparison
    |   left=codeExpression '&' right=codeExpression                                #and
    |   left=codeExpression '|' right=codeExpression                                #or
    |   <assoc=right> left=codeExpression '->' right=codeExpression                 #arrow
    ;

functionContract
    :   '[' expressionList ']'
    ;