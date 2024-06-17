import {
  CharStream,
  CommonTokenStream,
  ErrorListener,
  Recognizer
} from 'antlr4'
import CohortRuleParser, {
  ExprContext,
  TermContext
} from '../generated/search-exp-parser/CohortRuleParser'
import CohortRuleLexer from '../generated/search-exp-parser/CohortRuleLexer'

/**
 * Parses a search expression string into a SearchExpression object.
 */
export const parseExpression = (expression: string): SearchExpression => {
  const chars = new CharStream(expression)
  const lexer = new CohortRuleLexer(chars)
  lexer.removeErrorListeners()
  lexer.addErrorListener(new ThrowErrorListener())
  const tokens = new CommonTokenStream(lexer)
  const parser = new CohortRuleParser(tokens)
  parser.removeErrorListeners()
  parser.addErrorListener(new ThrowErrorListener())
  const tree = parser.expr()

  return _parseExpression(tree)
}

/**
 * Represents the overall search expression, which can either be a BooleanSearchExpression
 * or a ComparisonSearchFacet. This is a recursive data structure.
 */
export type SearchExpression = BooleanSearchExpression | ComparisonSearchFacet | NotExpression

/**
 * Represents an AND or OR of two search expressions.
 */
export type BooleanSearchExpression = {
  booleanOperator: BooleanOperator
  left: SearchExpression
  right: SearchExpression
}

/**
 * Represents a NOT of a search expression.
 */
export type NotExpression = {
  inner: SearchExpression
}

/**
 * Tests if a search expression is a NotExpression.
 */
export const isNotExpression = (expression: SearchExpression): expression is NotExpression => {
  return (expression as NotExpression).inner !== undefined
}

/**
 * Tests if a search expression is a BooleanSearchExpression.
 */
export const isBooleanSearchExpression = (expression: SearchExpression): expression is BooleanSearchExpression => {
  return (expression as BooleanSearchExpression).booleanOperator !== undefined
}

export type BooleanOperator = 'and' | 'or'

export type ComparisonOperator = '=' | '!=' | '>' | '>=' | '<' | '<=' | 'contains'

/**
 * Represents a comparison between two terms.
 */
export type ComparisonSearchFacet = {
  comparisonOperator: ComparisonOperator
  left: Term
  right: Term
}

/**
 * Tests if a search expression is a ComparisonSearchFacet.
 */
export const isComparisonSearchFacet = (
  expression: SearchExpression
): expression is ComparisonSearchFacet => {
  return (expression as ComparisonSearchFacet).comparisonOperator !== undefined
}

export type Term = SearchVariable | FunctionTerm | string | number | boolean | null

export type SearchVariable = {
  model: string
  field: string[]
}

/**
 * Tests if a term is a SearchVariable.
 */
export const isSearchVariable = (term: Term): term is SearchVariable => {
  return (term as SearchVariable).model !== undefined
}

export type FunctionTerm = {
  name: string
  args: Term[]
}

/**
 * Tests if a term is a FunctionTerm.
 */
export const isFunctionTerm = (term: Term): term is FunctionTerm => {
  return (term as FunctionTerm).name !== undefined
}

// Helpers for parsing the ANTLR parse tree
const _parseExpression = (ctx: ExprContext): SearchExpression => {
  if (ctx.NOT()) {
    return {
      inner: _parseExpression(ctx.expr(0))
    }
  }

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
  if (term.FUNCTION_NAME() != null) {
    return {
      name: term.FUNCTION_NAME().getText(),
      args: term.term_list().map(_parseTerm)
    }
  }

  if (term.VARIABLE() != null) {
    return _parseVariable(term.VARIABLE().getText())
  }

  if (term.NUMBER() != null) {
    return parseFloat(term.NUMBER().getText())
  }

  if (term.STRING() != null) {
    return term.STRING().getText().slice(1, -1)
  }

  if (term.BOOLEAN() != null) {
    return term.BOOLEAN().getText() === 'true'
  }

  if (term.NULL() != null) {
    return null
  }

  throw new Error('Invalid term')
}

const _parseVariable = (variable: string): SearchVariable => {
  // slice to remove curly braces, then split by '.' to get model and field
  const [model, ...field] = variable.slice(1, variable.length - 1).split('.')
  return {
    model,
    field
  }
}

class ThrowErrorListener extends ErrorListener<unknown> {
  syntaxError(recognizer: Recognizer<unknown>,
    offendingSymbol: unknown,
    line: number,
    column: number,
    msg: string) {
    throw new Error(`unknown token ${offendingSymbol}: ${msg}`)
  }
}
