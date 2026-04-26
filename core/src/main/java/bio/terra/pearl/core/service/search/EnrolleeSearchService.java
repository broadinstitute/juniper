package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class EnrolleeSearchService {
    private final EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;


    public EnrolleeSearchService(EnrolleeSearchExpressionDao enrolleeSearchExpressionDao,
                                 EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.enrolleeSearchExpressionDao = enrolleeSearchExpressionDao;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }


    public Map<String, SearchValueTypeDefinition> getExpressionSearchFacetsForStudyEnv(UUID studyEnvId) {
        return enrolleeSearchExpressionParser.getFacets(studyEnvId);
    }

    public List<EnrolleeSearchExpressionResult> executeSearchExpression(UUID studyEnvId, String expression, EnrolleeSearchOptions opts) {
        try {

            return enrolleeSearchExpressionDao.executeSearch(
                    enrolleeSearchExpressionParser.parseRule(expression),
                    studyEnvId,
                    opts
            );
        } catch (UnableToExecuteStatementException e) {
            String message = e.getShortMessage();

            // PSQLException has the most useful error message, so we should
            // see if we can grab it
            if (e.getCause().getClass().equals(PSQLException.class)) {
                PSQLException psqlException = (PSQLException) e.getCause();
                if (Objects.nonNull(psqlException.getServerErrorMessage())) {
                    message = psqlException.getServerErrorMessage().getMessage();
                }
            }

            throw new IllegalArgumentException("Invalid search expression: " + message);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid search expression: " + e.getMessage());
        }

    }

    public List<EnrolleeSearchExpressionResult> executeSearchExpression(UUID studyEnvId, String expression) {
        return executeSearchExpression(studyEnvId, expression, EnrolleeSearchOptions.builder().build());
    }
}
