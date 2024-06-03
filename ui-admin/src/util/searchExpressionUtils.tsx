import {
  isBooleanSearchExpression,
  isComparisonSearchExpression,
  isSearchVariable,
  SearchExpression,
  Term
} from './searchExpressionParser'
import { RuleGroupArray, RuleGroupType } from 'react-querybuilder'

/**
 *
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

  if (isComparisonSearchExpression(expression)) {
    return [{
      field: termToString(expression.left),
      value: termToString(expression.right),
      operator: expression.comparisonOperator
    }]
  }
  throw new Error('')
}

const termToString = (term: Term): string => {
  if (isSearchVariable(term)) {
    return `{${term.model}.${term.field.join('.')}}`
  }
  return term.toString()
}
