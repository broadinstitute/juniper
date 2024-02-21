grammar CohortRule;

/** A grammar for parsing rules, designed to be a subset of the SurveyJS rule parsing syntax
   See https://github.com/surveyjs/survey-library/blob/master/src/expressions/grammar.pegjs */

// Parser rules
expr: PAR_OPEN expr PAR_CLOSE | term OPERATOR term | expr JOINER expr;
term: NUMBER | STRING | VARIABLE | BOOLEAN;

// Lexer rules
NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '\'' (~[\\'\r\n])* '\'';
VARIABLE: '{' (~[\\'\r\n])* '}';
BOOLEAN: 'true' | 'false';
WS: [ \t\r\n]+ -> skip;
OPERATOR: '=' | '!=';
JOINER: '&&' | '||';
PAR_OPEN: '(';
PAR_CLOSE: ')';


