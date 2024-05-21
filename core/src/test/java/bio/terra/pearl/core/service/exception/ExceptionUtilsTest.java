package bio.terra.pearl.core.service.exception;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class ExceptionUtilsTest extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;

    @Test
    public void testExceptionTruncating() {
        try {
            surveyService.create(Survey.builder().build());
            Assertions.fail("expected exception not thrown");
        } catch (Exception e) {
            int prevLength = e.getStackTrace().length;
            ExceptionUtils.truncateExceptionTrace(e);
            int newLength = e.getStackTrace().length;
            assertThat(newLength, lessThan(prevLength));
        }
    }
}
