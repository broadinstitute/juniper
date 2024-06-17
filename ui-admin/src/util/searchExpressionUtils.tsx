import {
  BooleanOperator,
  isBooleanSearchExpression,
  isComparisonSearchFacet,
  isFunctionTerm,
  isNotExpression,
  isSearchVariable,
  SearchExpression,
  Term
} from './searchExpressionParser'
import {
  RuleGroupArray,
  RuleGroupType
} from 'react-querybuilder'
import { isEmpty } from 'lodash/fp'

/**
 * Concatenates search expressions with an 'and' by default.
 */
export const concatSearchExpressions =
  (searchExp: string[], booleanOperator: BooleanOperator = 'and') =>
    searchExp.filter(e => !isEmpty(e)).join(` ${booleanOperator} `)


/**
 * Converts a SearchExpression object into a RuleGroupType object,
 * which can be used by the react-querybuilder component.
 */
export const toReactQueryBuilderState = (searchExpression: SearchExpression): RuleGroupType => {
  return {
    id: '1',
    combinator: 'and',
    rules: _toReactQueryBuilderState('and', searchExpression)
  }
}

/**
 * Recursive helper function for converting search expressions into the react-querybuilder format.
 * Returns an array of rules - these are combined with either 'and' or 'or' depending on the operator
 * parameter. Each element could either be a facet comparison or new group of rules.
 */
const _toReactQueryBuilderState = (operator: BooleanOperator, expression: SearchExpression): RuleGroupArray => {
  if (isBooleanSearchExpression(expression)) {
    // only create a new group if the operator changes
    if (expression.booleanOperator !== operator) {
      return [{
        combinator: expression.booleanOperator,
        rules: _toReactQueryBuilderState(expression.booleanOperator, expression.left)
          .concat(_toReactQueryBuilderState(expression.booleanOperator, expression.right))
      }]
    }
    return _toReactQueryBuilderState(operator, expression.left)
      .concat(_toReactQueryBuilderState(operator, expression.right))
  }

  if (isNotExpression(expression)) {
    throw new Error('Not expressions are not supported in react-querybuilder')
  }

  if (isComparisonSearchFacet(expression)) {
    return [{
      field: termToString(expression.left) || '',
      value: termToString(expression.right),
      operator: expression.comparisonOperator
    }]
  }
  throw new Error('')
}

// Converts a Term object into a string representation.
const termToString = (term: Term): string | null => {
  if (isSearchVariable(term)) {
    if (term.field.length === 0) {
      return term.model
    }
    return `${term.model}.${term.field.join('.')}`
  }
  if (isFunctionTerm(term)) {
    throw new Error('Function terms are not supported')
  }
  return term ? term.toString() : null
}
