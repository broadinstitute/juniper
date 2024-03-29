package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.antlr.CohortRuleLexer;
import bio.terra.pearl.core.antlr.CohortRuleParser;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.service.rule.RuleParsingErrorListener;
import bio.terra.pearl.core.service.rule.RuleParsingException;
import bio.terra.pearl.core.service.search.expressions.BooleanSearchExpression;
import bio.terra.pearl.core.service.search.expressions.ComparisonOperator;
import bio.terra.pearl.core.service.search.expressions.DefaultSearchExpression;
import bio.terra.pearl.core.service.search.expressions.EnrolleeTermComparisonFacet;
import bio.terra.pearl.core.service.search.terms.AgeTerm;
import bio.terra.pearl.core.service.search.terms.AnswerTerm;
import bio.terra.pearl.core.service.search.terms.EnrolleeTerm;
import bio.terra.pearl.core.service.search.terms.ProfileTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import bio.terra.pearl.core.service.search.terms.UserInputTerm;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Operator;
import org.springframework.stereotype.Component;

/**
 * Parses a rule expression into a {@link EnrolleeSearchExpression}. The rule expression is a string
 * in a similar format to SurveyJS rules, e.g., "{age} > 18".
 */
@Component
public class EnrolleeSearchExpressionParser {
    private final EnrolleeDao enrolleeDao;
    private final AnswerDao answerDao;
    private final ProfileDao profileDao;
    private final MailingAddressDao mailingAddressDao;


    public EnrolleeSearchExpressionParser(EnrolleeDao enrolleeDao, AnswerDao answerDao, ProfileDao profileDao, MailingAddressDao mailingAddressDao) {
        this.enrolleeDao = enrolleeDao;
        this.answerDao = answerDao;
        this.profileDao = profileDao;
        this.mailingAddressDao = mailingAddressDao;
    }


    public EnrolleeSearchExpression parseRule(String rule) throws RuleParsingException {
        if (StringUtils.isBlank(rule)) {
            return new DefaultSearchExpression(enrolleeDao, profileDao);
        }

        try {
            CohortRuleLexer lexer = new CohortRuleLexer(CharStreams.fromString(rule));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new RuleParsingErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CohortRuleParser parser = new CohortRuleParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new RuleParsingErrorListener());
            CohortRuleParser.ExprContext exp = parser.expr();

            return parseExpression(exp);
        } catch (ParseCancellationException e) {
            throw new RuleParsingException("Error parsing rule: " + e.getMessage());
        }

    }

    private EnrolleeSearchExpression parseExpression(CohortRuleParser.ExprContext ctx) {
        if (ctx.expr().size() > 1) {
            EnrolleeSearchExpression left = parseExpression(ctx.expr(0));
            EnrolleeSearchExpression right = parseExpression(ctx.expr(1));
            return new BooleanSearchExpression(left, right, expToBooleanOperator(ctx));
        }
        return new EnrolleeTermComparisonFacet(
                enrolleeDao,
                profileDao,
                parseTerm(ctx.term(0)),
                parseTerm(ctx.term(1)),
                expToComparisonOperator(ctx));
    }

    private Operator expToBooleanOperator(CohortRuleParser.ExprContext ctx) {
        if (ctx.AND() != null) {
            return Operator.AND;
        } else if (ctx.OR() != null) {
            return Operator.OR;
        } else {
            throw new IllegalArgumentException("Unknown joiner");
        }
    }

    private ComparisonOperator expToComparisonOperator(CohortRuleParser.ExprContext ctx) {
        return switch (ctx.OPERATOR().getText().trim()) {
            case "=" -> ComparisonOperator.EQUALS;
            case "!=" -> ComparisonOperator.NOT_EQUALS;
            case ">" -> ComparisonOperator.GREATER_THAN;
            case "<" -> ComparisonOperator.LESS_THAN;
            case ">=" -> ComparisonOperator.GREATER_THAN_EQ;
            case "<=" -> ComparisonOperator.LESS_THAN_EQ;
            default -> throw new IllegalArgumentException("Unknown operator");
        };
    }

    private EnrolleeTerm parseTerm(CohortRuleParser.TermContext ctx) {
        if (ctx.BOOLEAN() != null) {
            return new UserInputTerm(new SearchValue(Boolean.parseBoolean(ctx.BOOLEAN().getText())));
        } else if (ctx.STRING() != null) {
            // remove outer quotes, e.g., 'John' -> John
            return new UserInputTerm(new SearchValue(ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1)));
        } else if (ctx.NUMBER() != null) {
            return new UserInputTerm(new SearchValue(Double.parseDouble(ctx.NUMBER().getText())));
        } else if (ctx.VARIABLE() != null) {
            return parseVariableTerm(ctx.VARIABLE().getText());
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }

    private EnrolleeTerm parseVariableTerm(String variable) {
        String trimmedVar = variable.substring(1, variable.length() - 1).trim();
        String model = parseModel(trimmedVar);


        switch (model) {
            case "profile":
                String field = parseField(trimmedVar);
                return parseProfileTerm(field);
            case "answer":
                String[] fields = parseFields(trimmedVar);
                if (fields.length != 2) {
                    throw new IllegalArgumentException("Invalid answer variable");
                }

                return parseAnswerTerm(fields[0], fields[1]);
            case "age":
                if (!trimmedVar.equals(model)) {
                    throw new IllegalArgumentException("Invalid age variable");
                }

                return new AgeTerm(profileDao);
            default:
                throw new IllegalArgumentException("Unknown model " + model);
        }
    }

    private String parseModel(String variable) {
        if (variable.contains(".")) {
            return variable.substring(0, variable.indexOf("."));
        }
        return variable;
    }

    private String parseField(String variable) {
        if (variable.contains(".")) {
            return variable.substring(variable.indexOf(".") + 1);
        }
        throw new IllegalArgumentException("No field in variable");
    }

    private String[] parseFields(String variable) {
        if (variable.contains(".")) {
            return variable.substring(variable.indexOf(".") + 1).split("\\.");
        }
        throw new IllegalArgumentException("No field in variable");
    }

    private ProfileTerm parseProfileTerm(String field) {
        return new ProfileTerm(profileDao, mailingAddressDao, field);
    }

    private AnswerTerm parseAnswerTerm(String surveyStableId, String questionStableId) {
        return new AnswerTerm(answerDao, surveyStableId, questionStableId);
    }
}
