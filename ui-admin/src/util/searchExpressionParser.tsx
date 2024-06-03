import { CharStream, CommonTokenStream } from 'antlr4'
import CohortRuleParser, { ExprContext, TermContext } from '../generated/search-exp-parser/CohortRuleParser'
import CohortRuleLexer from '../generated/search-exp-parser/CohortRuleLexer'

export type SearchExpression = BooleanSearchExpression | ComparisonSearchExpression

export type BooleanSearchExpression = {
  booleanOperator: BooleanOperator
  left: SearchExpression
  right: SearchExpression
}

/**
 *
 */
export const isBooleanSearchExpression = (expression: SearchExpression): expression is BooleanSearchExpression => {
  return (expression as BooleanSearchExpression).booleanOperator !== undefined
}

export type BooleanOperator = 'and' | 'or'

export type ComparisonOperator = '=' | '!=' | '>' | '>=' | '<' | '<=' | 'contains'
export type ComparisonSearchExpression = {
  comparisonOperator: ComparisonOperator
  left: Term
  right: Term
}

/**
 *
 */
export const isComparisonSearchExpression = (
  expression: SearchExpression
): expression is ComparisonSearchExpression => {
  return (expression as ComparisonSearchExpression).comparisonOperator !== undefined
}

export type Term = SearchVariable | string | number

export type SearchVariable = {
  model: string
  field: string[]
}

/**
 *
 */
export const isSearchVariable = (term: Term): term is SearchVariable => {
  return (term as SearchVariable).model !== undefined
}

/**
 * Parses a search expression string into a SearchExpression object.
 */
export const parseExpression = (expression: string): SearchExpression => {
  const chars = new CharStream(expression)
  const lexer = new CohortRuleLexer(chars)
  const tokens = new CommonTokenStream(lexer)
  const parser = new CohortRuleParser(tokens)
  const tree = parser.expr()

  return _parseExpression(tree)
}

const _parseExpression = (ctx: ExprContext): SearchExpression => {
  if (ctx.PAR_OPEN() != null && ctx.PAR_CLOSE() != null) {
    return _parseExpression(ctx.expr(0))
  }

  if (ctx.expr_list().length > 1) {
    return {
      booleanOperator: _ctxToBooleanOperator(ctx),
      left: _parseExpression(ctx.expr(0)),
      right: _parseExpression(ctx.expr(1))
    }
  }

  return {
    comparisonOperator: _ctxToComparisonOperator(ctx),
    left: _parseTerm(ctx.term(0)),
    right: _parseTerm(ctx.term(1))
  }
}

const _ctxToBooleanOperator = (ctx: ExprContext): BooleanOperator => {
  if (ctx.AND() != null) {
    return 'and'
  }
  if (ctx.OR() != null) {
    return 'or'
  }
  throw new Error('Invalid boolean operator')
}

const _ctxToComparisonOperator = (ctx: ExprContext): ComparisonOperator => {
  const rawOperator = ctx.OPERATOR().getText()
  if (['=', '!=', '>', '>=', '<', '<=', 'contains'].includes(rawOperator)) {
    return rawOperator as ComparisonOperator
  }
  throw new Error('Invalid comparison operator')
}

const _parseTerm = (term: TermContext): Term => {
  if (term.VARIABLE() != null) {
    return _parseVariable(term.VARIABLE().getText())
  }

  if (term.NUMBER() != null) {
    return parseFloat(term.NUMBER().getText())
  }

  if (term.STRING() != null) {
    return term.STRING().getText().slice(1, -1)
  }

  throw new Error('Invalid term')
}

const _parseVariable = (variable: string): SearchVariable => {
  const [model, ...field] = variable.split('.')
  return {
    model,
    field
  }
}
