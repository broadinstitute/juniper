import {
  BooleanOperator,
  isBooleanSearchExpression,
  isComparisonSearchFacet,
  isFunctionTerm,
  isIncludeExpression,
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
import {
  ExpressionSearchFacets,
  SearchValueTypeDefinition
} from 'api/api'
import { isNil } from 'lodash'

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
export const toReactQueryBuilderState = (
  searchExpression: SearchExpression,
  facets: ExpressionSearchFacets): RuleGroupType => {
  return {
    id: '1',
    combinator: 'and',
    rules: _toReactQueryBuilderState('and', searchExpression, facets)
  }
}

/**
 * Recursive helper function for converting search expressions into the react-querybuilder format.
 * Returns an array of rules - these are combined with either 'and' or 'or' depending on the operator
 * parameter. Each element could either be a facet comparison or new group of rules.
 */
const _toReactQueryBuilderState = (
  operator: BooleanOperator,
  expression: SearchExpression,
  facets: ExpressionSearchFacets): RuleGroupArray => {
  if (isBooleanSearchExpression(expression)) {
    // only create a new group if the operator changes
    if (expression.booleanOperator !== operator) {
      return [{
        combinator: expression.booleanOperator,
        rules: _toReactQueryBuilderState(expression.booleanOperator, expression.left, facets)
          .concat(_toReactQueryBuilderState(expression.booleanOperator, expression.right, facets))
      }]
    }
    return _toReactQueryBuilderState(operator, expression.left, facets)
      .concat(_toReactQueryBuilderState(operator, expression.right, facets))
  }

  if (isNotExpression(expression)) {
    throw new Error('Not expressions are not supported in react-querybuilder')
  }

  if (isIncludeExpression(expression)) {
    throw new Error('Include expressions are not supported in react-querybuilder')
  }

  if (isComparisonSearchFacet(expression)) {
    const field = termToString(expression.left, undefined)?.toString() || ''

    const typeDefinition = facets[field]

    return [{
      field,
      value: termToString(expression.right, typeDefinition),
      operator: expression.comparisonOperator
    }]
  }
  throw new Error('Unknown expression type')
}

// Converts a Term object into a string representation.
const termToString = (
  term: Term,
  typeDef: SearchValueTypeDefinition | undefined): string | number | boolean | null => {
  if (isSearchVariable(term)) {
    if (term.field.length === 0) {
      return term.model
    }
    return `${term.model}.${term.field.join('.')}`
  }
  if (isFunctionTerm(term)) {
    throw new Error('Function terms are not supported')
  }

  if (!isNil(typeDef) && typeDef.type === 'INSTANT') {
    const parsedDate = Date.parse(term?.toString() || '')
    if (isNaN(parsedDate)) {
      return term // return the original term just to be safe
    }

    const date = new Date(parsedDate)

    return formatDate(date)
  }

  return term
}


// react query builder needs dates with format: 2025-01-21T14:15
const formatDate = (date: Date): string => {
  // we can't use toISOString because it returns the date in UTC and we need it in local time
  return `${
    date.getFullYear()
  }-${
    zeroPad(date.getMonth() + 1)
  }-${
    zeroPad(date.getDate())
  }T${
    zeroPad(date.getHours())
  }:${
    zeroPad(date.getMinutes())
  }`
}

const zeroPad = (num: number): string => {
  return num.toString().padStart(2, '0')
}
