package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.antlr.CohortRuleLexer;
import bio.terra.pearl.core.antlr.CohortRuleParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

@Slf4j
public class CohortRuleEvaluator {
    private final EnrolleeRuleData ruleData;

    public CohortRuleEvaluator(EnrolleeRuleData ruleData) {
        this.ruleData = ruleData;
    }

    public static boolean evaluateRule(String rule, EnrolleeRuleData ruleData) throws RuleEvaluationException, RuleParsingException {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(ruleData);
        return evaluator.evaluateRule(rule);
    }

    public boolean evaluateRule(String rule) throws RuleEvaluationException, RuleParsingException {
        if (StringUtils.isBlank(rule)) {
            // the default rule is that the action is applied if the enrollee is a subject
            return ruleData.enrollee().isSubject();
        }
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString(rule));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CohortRuleParser parser = new CohortRuleParser(tokens);
        CohortRuleParser.ExprContext exp = parser.expr();
        return evaluateExpression(exp);
    }

    public boolean evaluateExpression(CohortRuleParser.ExprContext ctx) throws RuleEvaluationException, RuleParsingException {
        if (ctx.expr().size() > 1) {
            Boolean left = evaluateExpression(ctx.expr(0));
            Boolean right = evaluateExpression(ctx.expr(1));
            if (ctx.AND() != null) {
                return left && right;
            } else if (ctx.OR() != null) {
                return left || right;
            } else {
                throw new RuleParsingException("Unknown joiner");
            }
        } else {
            Object left = evaluateTerm(ctx.term(0));
            Object right = evaluateTerm(ctx.term(1));
            String operator = ctx.OPERATOR().getText();
            switch (operator)  {
                case "=":
                    return left.equals(right);
                case "!=":
                    return !left.equals(right);
                default:
                    throw new RuleParsingException("Unknown operator %s".formatted(operator));
            }
        }
    }

    public Object evaluateTerm(CohortRuleParser.TermContext ctx) throws RuleEvaluationException, RuleParsingException {
        if (ctx.BOOLEAN() != null) {
            return Boolean.parseBoolean(ctx.BOOLEAN().getText());
        } else if (ctx.STRING() != null) {
            String rawString = ctx.STRING().getText();
            // trim off brackets and any whitespace immediately inside the brackets
            return rawString.substring(1, rawString.length() - 1).trim();
        } else if (ctx.NUMBER() != null) {
            return Double.parseDouble(ctx.NUMBER().getText());
        } else if (ctx.VARIABLE() != null) {
            String rawString = ctx.VARIABLE().getText();
            // trim off brackets and any whitespace immediately inside the quotes
            String variableName = rawString.substring(1, rawString.length() - 1).trim();
            try {
                Object value = PropertyUtils.getNestedProperty(ruleData, variableName);
                if (value instanceof Integer || value instanceof Float || value instanceof BigInteger || value instanceof BigDecimal) {
                    // cast all numbers to Doubles for comparison -- this assumes that this
                    // framework will never be used for anything that requires more precision
                    return ((Number) value).doubleValue();
                } else {
                    return value;
                }
            } catch (Exception e) {
                // if the property doesn't exist, return null
                return null;
            }
        }
        throw new RuleParsingException("Unknown term type");
    }
}
