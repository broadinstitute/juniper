package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.antlr.CohortRuleLexer;
import bio.terra.pearl.core.antlr.CohortRuleParser;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.rule.RuleParsingErrorListener;
import bio.terra.pearl.core.service.rule.RuleParsingException;
import bio.terra.pearl.core.service.search.expressions.*;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchTermParser;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import bio.terra.pearl.core.service.search.terms.UserInputTerm;
import bio.terra.pearl.core.service.search.terms.functions.LowerFunction;
import bio.terra.pearl.core.service.search.terms.functions.MaxFunction;
import bio.terra.pearl.core.service.search.terms.functions.MinFunction;
import bio.terra.pearl.core.service.search.terms.functions.TrimFunction;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Operator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Parses a rule expression into a {@link EnrolleeSearchExpression}. The rule expression is a string
 * in a similar format to SurveyJS rules, e.g., "{age} > 18".
 */
@Component
public class EnrolleeSearchExpressionParser {
    private final EnrolleeDao enrolleeDao;
    private final ProfileDao profileDao;

    private final List<SearchTermParser> searchTermParsers;

    public EnrolleeSearchExpressionParser(EnrolleeDao enrolleeDao,
                                          ProfileDao profileDao,
                                          // any subclass of SearchTermParser with @Service annotation
                                          // will be included
                                          List<SearchTermParser> parsers) {
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;

        searchTermParsers = parsers;
    }

    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        Map<String, SearchValueTypeDefinition> fields = new HashMap<>();

        searchTermParsers.stream().forEach(parser -> fields.putAll(parser.getFacets(studyEnvId)));

        return fields;
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

        if (ctx.UNARY_OP() != null) {
            return new UnaryExpression(
                    enrolleeDao,
                    profileDao,
                    parseTerm(ctx.term(0)),
                    expToUnaryOperator(ctx));
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

    private UnarySearchOperator expToUnaryOperator(CohortRuleParser.ExprContext ctx) {
        return switch (ctx.UNARY_OP().getText().trim()) {
            case "isNull" -> UnarySearchOperator.IS_NULL;
            case "isNotNull" -> UnarySearchOperator.IS_NOT_NULL;
            default -> throw new IllegalArgumentException("Unknown unary operator");
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
        return getTermParser(variable).parseVariable(variable);
    }

    private SearchTermParser getTermParser(String variable) {
        return searchTermParsers.stream()
                .filter(parser -> parser.match(variable))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variable does not exist: " + variable));
    }

    public ExportOptionsWithExpression parseExportOptions(ExportOptions exportOptions) {
        ExportOptionsWithExpression exportOptionsWithExpression = new ExportOptionsWithExpression();
        BeanUtils.copyProperties(exportOptions, exportOptionsWithExpression);
        if (!StringUtils.isBlank(exportOptions.getFilterString())) {
            exportOptionsWithExpression.setFilterExpression(parseRule(exportOptions.getFilterString()));
        }
        return exportOptionsWithExpression;
    }
}
