package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.antlr.CohortRuleLexer;
import bio.terra.pearl.core.antlr.CohortRuleParser;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.participant.*;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.service.rule.RuleParsingErrorListener;
import bio.terra.pearl.core.service.rule.RuleParsingException;
import bio.terra.pearl.core.service.search.expressions.*;
import bio.terra.pearl.core.service.search.terms.*;
import bio.terra.pearl.core.service.search.terms.functions.LowerFunction;
import bio.terra.pearl.core.service.search.terms.functions.MaxFunction;
import bio.terra.pearl.core.service.search.terms.functions.MinFunction;
import bio.terra.pearl.core.service.search.terms.functions.TrimFunction;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Operator;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final ParticipantTaskDao participantTaskDao;
    private final KitRequestDao kitRequestDao;
    private final FamilyDao familyDao;
    private final ParticipantUserDao participantUserDao;

    public EnrolleeSearchExpressionParser(EnrolleeDao enrolleeDao, AnswerDao answerDao, ProfileDao profileDao, MailingAddressDao mailingAddressDao, ParticipantTaskDao participantTaskDao, KitRequestDao kitRequestDao, FamilyDao familyDao, ParticipantUserDao participantUserDao) {
        this.enrolleeDao = enrolleeDao;
        this.answerDao = answerDao;
        this.profileDao = profileDao;
        this.mailingAddressDao = mailingAddressDao;
        this.participantTaskDao = participantTaskDao;
        this.kitRequestDao = kitRequestDao;
        this.familyDao = familyDao;
        this.participantUserDao = participantUserDao;
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
        if (ctx.NOT() != null) {
            if (!ctx.expr().isEmpty()) {
                return new NotSearchExpression(parseExpression(ctx.expr(0)));
            } else {
                return new DefaultSearchExpression(enrolleeDao, profileDao);
            }
        }
        if (ctx.INCLUDE() != null) {
            if (ctx.term().size() != 1) {
                throw new IllegalArgumentException("Include expression requires one term");
            }
            return new IncludeExpression(profileDao, enrolleeDao, parseTerm(ctx.term(0)));
        }
        if (ctx.PAR_OPEN() != null && ctx.PAR_CLOSE() != null) {
            if (!ctx.expr().isEmpty()) {
                return parseExpression(ctx.expr(0));
            } else {
                return new DefaultSearchExpression(enrolleeDao, profileDao);
            }
        }
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

    private SearchOperators expToComparisonOperator(CohortRuleParser.ExprContext ctx) {
        return switch (ctx.OPERATOR().getText().trim()) {
            case "=" -> SearchOperators.EQUALS;
            case "!=" -> SearchOperators.NOT_EQUALS;
            case ">" -> SearchOperators.GREATER_THAN;
            case "<" -> SearchOperators.LESS_THAN;
            case ">=" -> SearchOperators.GREATER_THAN_EQ;
            case "<=" -> SearchOperators.LESS_THAN_EQ;
            case "contains" -> SearchOperators.CONTAINS;
            default -> throw new IllegalArgumentException("Unknown operator");
        };
    }

    private SearchTerm parseTerm(CohortRuleParser.TermContext ctx) {
        if (ctx.FUNCTION_NAME() != null) {
            return parseFunctionTerm(ctx);
        }
        if (ctx.BOOLEAN() != null) {
            return new UserInputTerm(new SearchValue(Boolean.parseBoolean(ctx.BOOLEAN().getText())));
        } else if (ctx.STRING() != null) {
            // remove outer quotes, e.g., 'John' -> John
            return new UserInputTerm(new SearchValue(ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1)));
        } else if (ctx.NUMBER() != null) {
            return new UserInputTerm(new SearchValue(Double.parseDouble(ctx.NUMBER().getText())));
        } else if (ctx.VARIABLE() != null) {
            return parseVariableTerm(ctx.VARIABLE().getText());
        } else if (ctx.NULL() != null) {
            return new UserInputTerm(new SearchValue());
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }

    private SearchTerm parseFunctionTerm(CohortRuleParser.TermContext ctx) {
        String functionName = ctx.FUNCTION_NAME().getText();
        List<CohortRuleParser.TermContext> terms = ctx.term();

        switch (functionName) {
            // lower function (e.g., lower({profile.name})
            case "lower" -> {
                if (terms.size() != 1) {
                    throw new IllegalArgumentException("Lower function requires one argument");
                }

                return new LowerFunction(parseTerm(terms.get(0)));
            }
            case "trim" -> {
                if (terms.size() != 1) {
                    throw new IllegalArgumentException("Trim function requires one argument");
                }

                return new TrimFunction(parseTerm(terms.get(0)));
            }
            case "min" -> {
                return new MinFunction(terms.stream().map(this::parseTerm).toList());
            }
            case "max" -> {
                return new MaxFunction(terms.stream().map(this::parseTerm).toList());
            }
            default -> throw new IllegalArgumentException("Unknown function " + functionName);

        }

    }

    private SearchTerm parseVariableTerm(String variable) {
        String trimmedVar = variable.substring(1, variable.length() - 1).trim();
        String model = parseModel(trimmedVar);

        switch (model) {
            case "profile":
                String profileField = parseField(trimmedVar);

                return new ProfileTerm(profileDao, mailingAddressDao, profileField);
            case "answer":
                String[] answerFields = parseFields(trimmedVar);
                if (answerFields.length != 2) {
                    throw new IllegalArgumentException("Invalid answer variable");
                }

                return new AnswerTerm(answerDao, answerFields[0], answerFields[1]);
            case "age":
                if (!trimmedVar.equals(model)) {
                    throw new IllegalArgumentException("Invalid age variable");
                }

                return new AgeTerm(profileDao);
            case "enrollee":
                String enrolleeField = parseField(trimmedVar);

                return new EnrolleeTerm(enrolleeField);
            case "task":
                String[] taskFields = parseFields(trimmedVar);
                if (taskFields.length != 2) {
                    throw new IllegalArgumentException("Invalid answer variable");
                }

                return new TaskTerm(participantTaskDao, taskFields[0], taskFields[1]);
            case "latestKit":
                String latestKitField = parseField(trimmedVar);

                return new LatestKitTerm(kitRequestDao, latestKitField);
            case "family":
                String familyField = parseField(trimmedVar);

                return new FamilyTerm(familyDao, familyField);
            case "user":
                String userField = parseField(trimmedVar);

                return new UserTerm(participantUserDao, userField);
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
}
