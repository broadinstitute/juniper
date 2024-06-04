import {
  isBooleanSearchExpression,
  isComparisonSearchFacet,
  isSearchVariable,
  SearchExpression,
  Term
} from './searchExpressionParser'
import { RuleGroupArray, RuleGroupType } from 'react-querybuilder'

/**
 * Converts a SearchExpression object into a RuleGroupType object,
 * which can be used by the react-querybuilder component.
 */
export const toReactQueryBuilderState = (searchExpression: SearchExpression): RuleGroupType => {
  return {
    id: '1',
    combinator: 'and',
    rules: _toReactQueryBuilderState(searchExpression)
  }
}

const _toReactQueryBuilderState = (expression: SearchExpression): RuleGroupArray => {
  if (isBooleanSearchExpression(expression)) {
    return [{
      combinator: expression.booleanOperator,
      rules: _toReactQueryBuilderState(expression.left).concat(_toReactQueryBuilderState(expression.right))
    }]
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

const termToString = (term: Term): string | null => {
  if (isSearchVariable(term)) {
    if (term.field.length === 0) {
      return term.model
    }
    return `${term.model}.${term.field.join('.')}`
  }
  return term ? term.toString() : null
}
