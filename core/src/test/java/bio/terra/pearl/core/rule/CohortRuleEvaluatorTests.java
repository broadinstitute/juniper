package bio.terra.pearl.core.rule;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CohortRuleEvaluatorTests {

    @Test
    public void testStringEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(new HashMap<>());
        assertThat(evaluator.evaluateRule("'yes' = 'yes'"), equalTo(true));
        assertThat(evaluator.evaluateRule("'no' = 'yes'"), equalTo(false));

        assertThat(evaluator.evaluateRule("'yes' != 'yes'"), equalTo(false));
        assertThat(evaluator.evaluateRule("'no' != 'yes'"), equalTo(true));
    }

    @Test
    public void testIntegerNumberEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(new HashMap<>());
        assertThat(evaluator.evaluateRule("1 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRule("1 = 2"), equalTo(false));

        assertThat(evaluator.evaluateRule("1 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRule("1 != 2"), equalTo(true));
    }

    @Test
    public void testFloatNumberEvaluation() throws Exception {
        CohortRuleEvaluator evaluator = new CohortRuleEvaluator(new HashMap<>());
        assertThat(evaluator.evaluateRule("1.0 = 1.0"), equalTo(true));
        assertThat(evaluator.evaluateRule("1.0 = 1"), equalTo(true));
        assertThat(evaluator.evaluateRule("1.1 = 2.2"), equalTo(false));

        assertThat(evaluator.evaluateRule("1.0 != 1.0"), equalTo(false));
        assertThat(evaluator.evaluateRule("1.0 != 1"), equalTo(false));
        assertThat(evaluator.evaluateRule("1.1 != 2.2"), equalTo(true));
    }

    @Test
    public void testStringVariableInsertion() throws Exception {
        String rule = "{foo} = 'yes'";
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", "yes")), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", "no")), equalTo(false));
    }

    @Test
    public void testNumberVariableInsertion() throws Exception {
        String rule = "{foo} = 1";
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", 1)), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", 1.0)), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", 2)), equalTo(false));
    }

    @Test
    public void testBooleanVariableInsertion() throws Exception {
        String rule = "{foo} = true";
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", true)), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule(rule, Map.of("foo", false)), equalTo(false));
    }

    @Test
    public void testOrderOfOperationsAndOr() throws Exception {
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 && 2 = 3 || 3 = 3", Map.of()), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 1 && 2 = 2 || 3 = 4", Map.of()), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 1 || 2 = 3 && 3 = 4", Map.of()), equalTo(true));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 2 && 3 = 3", Map.of()), equalTo(true));

        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 3 && 3 = 3", Map.of()), equalTo(false));
        assertThat(CohortRuleEvaluator.evaluateRule("1 = 2 || 2 = 2 && 3 = 4", Map.of()), equalTo(false));
    }

}
