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
import java.util.Objects;

@Slf4j
public class EnrolleeRuleEvaluator {
    private final EnrolleeProfileBundle ruleData;

    public EnrolleeRuleEvaluator(EnrolleeProfileBundle ruleData) {
        this.ruleData = ruleData;
    }

    // for evaluations where we need to indicate whether the rule itself is valid
    public static boolean evaluateRuleChecked(String rule, EnrolleeProfileBundle ruleData) throws RuleEvaluationException, RuleParsingException {
        EnrolleeRuleEvaluator evaluator = new EnrolleeRuleEvaluator(ruleData);
        return evaluator.evaluateRuleChecked(rule);
    }

    // Evaluates the rule. Returns false if the rule cannot be parsed, or if an error occurred accessing the ruleData
    public static boolean evaluateRule(String rule, EnrolleeProfileBundle ruleData) {
        try {
            return evaluateRuleChecked(rule, ruleData);
        } catch (RuleEvaluationException | RuleParsingException e) {
            log.warn("Error evaluating rule [ {} ]: studyEnvironment {}, enrollee {}: ".formatted(rule,
                    ruleData.getEnrollee().getStudyEnvironmentId(),
                    ruleData.getEnrollee().getShortcode()), e);
            return false;
        }
    }


    public boolean evaluateRuleChecked(String rule) throws RuleEvaluationException, RuleParsingException {
        if (StringUtils.isBlank(rule)) {
            return applyDefaultRule();
        }
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString(rule));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CohortRuleParser parser = new CohortRuleParser(tokens);
        CohortRuleParser.ExprContext exp = parser.expr();
        return evaluateExpression(exp);
    }

    // the default rule is that the action is applied if the enrollee is a subject
    protected boolean applyDefaultRule() {
        return ruleData.getEnrollee().isSubject();
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
                    return Objects.equals(left, right);
                case "!=":
                    return !Objects.equals(left, right);
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
        } else if (ctx.NULL() != null) {
            return null;
        }
        throw new RuleParsingException("Unknown term type");
    }
}
