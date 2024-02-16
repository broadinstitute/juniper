package bio.terra.pearl.core.service.rule;

/**
 * Placeholder class for server-side rule evaluation -- this will likely get transformed as we adopt a 3rd party
 * rules engine like EasyRule
 */
public class RuleEvaluator {
    /** return whether the rule is satisfied by the given data */
    public static boolean evaluateEnrolleeRule(String rule, EnrolleeRuleData ruleData) {
        // for now, all enrollees are eligible for everything, as long as they are a research subject (e.g. not just a proxy)
        return ruleData.enrollee().isSubject();
    }
}
