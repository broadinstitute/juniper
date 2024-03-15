package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.antlr.CohortRuleLexer;
import bio.terra.pearl.core.antlr.CohortRuleParser;
import bio.terra.pearl.core.service.search.expressions.BooleanSearchExpression;
import bio.terra.pearl.core.service.search.expressions.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.expressions.EnrolleeSearchFacet;
import bio.terra.pearl.core.service.search.terms.ConstantTermExtractor;
import bio.terra.pearl.core.service.search.terms.EnrolleeTermExtractor;
import bio.terra.pearl.core.service.search.terms.ProfileTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeSearchExpressionParser {
    public EnrolleeSearchExpression parseRule(String rule) {
        CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString(rule));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CohortRuleParser parser = new CohortRuleParser(tokens);
        CohortRuleParser.ExprContext exp = parser.expr();

        return parseExpression(exp);
    }

    private EnrolleeSearchExpression parseExpression(CohortRuleParser.ExprContext ctx) {
        if (ctx.expr().size() > 1) {
            EnrolleeSearchExpression left = parseExpression(ctx.expr(0));
            EnrolleeSearchExpression right = parseExpression(ctx.expr(1));
            return new BooleanSearchExpression(left, right, expToBooleanOperator(ctx));
        }
        return new EnrolleeSearchFacet(parseTerm(ctx.term(0)), parseTerm(ctx.term(1)), expToComparisonOperator(ctx));
    }

    private BooleanOperator expToBooleanOperator(CohortRuleParser.ExprContext ctx) {
        if (ctx.AND() != null) {
            return BooleanOperator.AND;
        } else if (ctx.OR() != null) {
            return BooleanOperator.OR;
        } else {
            throw new IllegalArgumentException("Unknown joiner");
        }
    }

    private ComparisonOperator expToComparisonOperator(CohortRuleParser.ExprContext ctx) {
        return switch (ctx.OPERATOR().getText()) {
            case "=" -> ComparisonOperator.EQUALS;
            case "!=" -> ComparisonOperator.NOT_EQUALS;
            case ">" -> ComparisonOperator.GREATER_THAN;
            case "<" -> ComparisonOperator.LESS_THAN;
            case ">=" -> ComparisonOperator.GREATER_THAN_EQ;
            case "<=" -> ComparisonOperator.LESS_THAN_EQ;
            default -> throw new IllegalArgumentException("Unknown operator");
        };
    }

    private EnrolleeTermExtractor parseTerm(CohortRuleParser.TermContext ctx) {
        if (ctx.BOOLEAN() != null) {
            return new ConstantTermExtractor(new Term(Boolean.parseBoolean(ctx.BOOLEAN().getText())));
        } else if (ctx.STRING() != null) {
            return new ConstantTermExtractor(new Term(ctx.STRING().getText()));
        } else if (ctx.NUMBER() != null) {
            return new ConstantTermExtractor(new Term(Double.parseDouble(ctx.NUMBER().getText())));
        } else if (ctx.VARIABLE() != null) {
            return parseVariableTerm(ctx.VARIABLE().getText());
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }

    private EnrolleeTermExtractor parseVariableTerm(String variable) {
        String trimmedVar = variable.substring(1, variable.length() - 1).trim();
        String[] split = trimmedVar.split("\\.");

        String model = split[0];


        switch (model) {
            case "profile":
                if (split.length != 2)
                    throw new IllegalArgumentException("Invalid profile term; expected format: profile.<field>");
                return parseProfileTerm(split[1]);
            default:
                throw new IllegalArgumentException("Unknown model " + model);
        }
    }

    private ProfileTermExtractor parseProfileTerm(String field) {
        return switch (field) {
            case "givenName" -> new ProfileTermExtractor(ProfileTermExtractor.ProfileField.GIVEN_NAME);
            case "familyName" -> new ProfileTermExtractor(ProfileTermExtractor.ProfileField.FAMILY_NAME);
            case "contactEmail" -> new ProfileTermExtractor(ProfileTermExtractor.ProfileField.CONTACT_EMAIL);
            case "phoneNumber" -> new ProfileTermExtractor(ProfileTermExtractor.ProfileField.PHONE_NUMBER);
            case "birthDate" -> new ProfileTermExtractor(ProfileTermExtractor.ProfileField.BIRTH_DATE);
            default -> throw new IllegalArgumentException("Unknown profile field");
        };
    }
}
