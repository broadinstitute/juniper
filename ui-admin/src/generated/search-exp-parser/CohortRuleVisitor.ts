// Generated from src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.1

import {ParseTreeVisitor} from 'antlr4';


import { ExprContext } from "./CohortRuleParser";
import { TermContext } from "./CohortRuleParser";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `CohortRuleParser`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export default class CohortRuleVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `CohortRuleParser.expr`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitExpr?: (ctx: ExprContext) => Result;
	/**
	 * Visit a parse tree produced by `CohortRuleParser.term`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitTerm?: (ctx: TermContext) => Result;
}

