grammar CohortRule;

/** A grammar for parsing rules, designed to be a subset of the SurveyJS rule parsing syntax
   See https://github.com/surveyjs/survey-library/blob/master/src/expressions/grammar.pegjs */

// Parser rules
expr: PAR_OPEN expr PAR_CLOSE | term OPERATOR term | expr AND expr | expr OR expr | NOT expr | INCLUDE PAR_OPEN term PAR_CLOSE;
term: NUMBER | STRING | VARIABLE | BOOLEAN | NULL | FUNCTION_NAME PAR_OPEN term (',' term)* PAR_CLOSE;

// Lexer rules
NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' (~[\\'\r\n])* '\'';
// matches {variableName.arg1.arg2} or {variableName["study"].arg1.arg2}
VARIABLE: '{' ([a-zA-Z0-9_]+'["'[a-zA-Z0-9_]+'"].')? ([a-zA-Z0-9_]|'.')+ '}';
BOOLEAN: 'true' | 'false';
NULL: 'null';
WS: [ \t\r\n]+ -> skip;
OPERATOR: '=' | '!=' | '>' | '<' | '>=' | '<=' | 'contains';
AND: 'and';
OR: 'or';
PAR_OPEN: '(';
PAR_CLOSE: ')';
NOT: '!';
INCLUDE: 'include';
FUNCTION_NAME: [a-zA-Z_]+;
