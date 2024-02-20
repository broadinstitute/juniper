package bio.terra.pearl.core.rule;

import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.rule.CohortRuleEvaluator;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CohortRuleEvaluatorTests {

    private EnrolleeRuleData EMPTY_RULE_DATA = new EnrolleeRuleData(null, null);

    @Test
    public void testStringEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRule("'yes' = 'yes'"), equalTo(true));
        assertThat(evaluator.evaluateRule("'no' = 'yes'"), equalTo(false));

        assertThat(evaluator.evaluateRule("'yes' != 'yes'"), equalTo(false));
        assertThat(evaluator.evaluateRule("'no' != 'yes'"), equalTo(true));
    }

    @Test
    public void testIntegerNumberEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRule("1 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRule("1 = 2"), equalTo(false));

        assertThat(evaluator.evaluateRule("1 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRule("1 != 2"), equalTo(true));
    }

    @Test
    public void testFloatNumberEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRule("1.0 = 1.0"), equalTo(true));
        assertThat(evaluator.evaluateRule("1.0 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRule("1.1 = 2.2"), equalTo(false));

        assertThat(evaluator.evaluateRule("1.0 != 1.0"), equalTo(false));
        assertThat(evaluator.evaluateRule("1.0 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRule("1.1 != 2.2"), equalTo(true));
    }

    @Test
    public void testStringVariableInsertion() throws Exception {
        String rule = "{profile.sexAtBrith} = 'M'";
        assertThat(CohortRuleEvaluator.evaluateRule(rule,
                new EnrolleeRuleData(null, Profile.builder().sexAtBirth("M").build())),
                equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(false));
    }

    @Test
    public void testNumberVariableInsertion() throws Exception {
        String rule = "{foo} = 1";
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(false));
    }

    @Test
    public void testBooleanVariableInsertion() throws Exception {
        String rule = "{foo} = true";
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, EMPTY_RULE_DATA), equalTo(false));
    }

    @Test
    public void testOrderOfOperationsAndOr() throws Exception {
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 && 2 = 3 || 3 = 3", EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 1 && 2 = 2 || 3 = 4", EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 1 || 2 = 3 && 3 = 4", EMPTY_RULE_DATA), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 2 && 3 = 3", EMPTY_RULE_DATA), equalTo(true));

        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 3 && 3 = 3", EMPTY_RULE_DATA), equalTo(false));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 2 && 3 = 4", EMPTY_RULE_DATA), equalTo(false));
    }

}
