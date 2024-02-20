package bio.terra.pearl.core.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.rule.EnrolleeRuleEvaluator;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EnrolleeRuleEvaluatorTests {

    private EnrolleeRuleData EMPTY_RULE_DATA = new EnrolleeRuleData(null, null);

    @Test
    public void testStringEvaluation() throws Exception {
        EnrolleeRuleEvaluator evaluator = new EnrolleeRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRuleChecked("'yes' = 'yes'"), equalTo(true));
        assertThat(evaluator.evaluateRuleChecked("'no' = 'yes'"), equalTo(false));

        assertThat(evaluator.evaluateRuleChecked("'yes' != 'yes'"), equalTo(false));
        assertThat(evaluator.evaluateRuleChecked("'no' != 'yes'"), equalTo(true));
    }

    @Test
    public void testIntegerNumberEvaluation() throws Exception {
        EnrolleeRuleEvaluator evaluator = new EnrolleeRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRuleChecked("1 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRuleChecked("1 = 2"), equalTo(false));

        assertThat(evaluator.evaluateRuleChecked("1 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRuleChecked("1 != 2"), equalTo(true));
    }

    @Test
    public void testFloatNumberEvaluation() throws Exception {
        EnrolleeRuleEvaluator evaluator = new EnrolleeRuleEvaluator(EMPTY_RULE_DATA);
        assertThat(evaluator.evaluateRuleChecked("1.0 = 1.0"), equalTo(true));
        assertThat(evaluator.evaluateRuleChecked("1.0 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRuleChecked("1.1 = 2.2"), equalTo(false));

        assertThat(evaluator.evaluateRuleChecked("1.0 != 1.0"), equalTo(false));
        assertThat(evaluator.evaluateRuleChecked("1.0 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRuleChecked("1.1 != 2.2"), equalTo(true));
    }

    @Test
    public void testStringVariableInsertion() throws Exception {
        String rule = "{profile.sexAtBirth} = 'M'";
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule,
                new EnrolleeRuleData(null, Profile.builder().sexAtBirth("M").build())),
                equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule,
                        new EnrolleeRuleData(null, Profile.builder().sexAtBirth("F").build())),
                equalTo(false));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule,
                        new EnrolleeRuleData(null, Profile.builder().sexAtBirth(null).build())),
                equalTo(false));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule,
                        new EnrolleeRuleData(null, null)),
                equalTo(false));
    }

    @Test
    public void testBooleanVariableInsertion() throws Exception {
        String rule = "{enrollee.consented} = true";
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule, new EnrolleeRuleData(Enrollee.builder().consented(true).build(), null)), equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule, new EnrolleeRuleData(Enrollee.builder().consented(false).build(), null)), equalTo(false));
    }

    @Test
    public void testUnrecognizedVariableInsertion() throws Exception {
        String rule = "{enrollee.doesNotExistZZZ} = true";
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule, new EnrolleeRuleData(new Enrollee(), null)), equalTo(false));
    }

    @Test
    public void testVariableNullCheck() throws Exception {
        String rule = "{enrollee.shortcode} = null";
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule, new EnrolleeRuleData(new Enrollee(), null)), equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked(rule, new EnrolleeRuleData(Enrollee.builder().shortcode("FOO").build(), null)), equalTo(false));
    }

    /**
     *  We should have tests for numeric values here, but we don't yet have numeric properties on EnrolleeRuleData --
     * that will come once we start attaching answers to EnrolleeRuleData.
     */

    @Test
    public void testOrderOfOperationsAndOr() throws Exception {
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 2 && 2 = 3 || 3 = 3", EMPTY_RULE_DATA), equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 1 && 2 = 2 || 3 = 4", EMPTY_RULE_DATA), equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 1 || 2 = 3 && 3 = 4", EMPTY_RULE_DATA), equalTo(true));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 2 || 2 = 2 && 3 = 3", EMPTY_RULE_DATA), equalTo(true));

        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 2 || 2 = 3 && 3 = 3", EMPTY_RULE_DATA), equalTo(false));
        assertThat(EnrolleeRuleEvaluator.evaluateRuleChecked("1 = 2 || 2 = 2 && 3 = 4", EMPTY_RULE_DATA), equalTo(false));
    }

}
