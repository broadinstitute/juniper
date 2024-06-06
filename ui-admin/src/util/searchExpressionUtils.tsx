import { isEmpty } from 'lodash/fp'

/**
 * Concatenates search expressions with an 'and' by default.
 */
export const concatSearchExpressions =
  (searchExp: string[], booleanOperator: SearchExpressionBooleanOperator = 'and') =>
    searchExp.filter(e => !isEmpty(e)).join(` ${booleanOperator} `)

export type SearchExpressionBooleanOperator = 'and' | 'or'

export type SearchExpressionComparisonOperator = '<=' | '>=' | '!=' | '=' | '<' | '>' | 'contains'
