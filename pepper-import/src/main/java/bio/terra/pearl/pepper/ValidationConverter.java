package bio.terra.pearl.pepper;

import bio.terra.pearl.pepper.dto.SurveyJSQuestion;
import bio.terra.pearl.pepper.dto.SurveyJsValidator;
import org.broadinstitute.ddp.model.activity.definition.question.QuestionDef;
import org.broadinstitute.ddp.model.activity.definition.validation.IntRangeRuleDef;
import org.broadinstitute.ddp.model.activity.definition.validation.RuleDef;
import org.broadinstitute.ddp.model.activity.types.RuleType;

public class ValidationConverter {
    public static SurveyJsValidator convert(RuleDef ruleDef) {
        RuleType ruleType = ruleDef.getRuleType();
        if (ruleType.equals(RuleType.INT_RANGE)) {
            IntRangeRuleDef intRangeDef = (IntRangeRuleDef) ruleDef;
            return SurveyJsValidator.builder()
                    .type("numeric")
                    .minValue(intRangeDef.getMin())
                    .maxValue(intRangeDef.getMax())
                    .text(ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                            ruleDef.getHintTemplate().getVariables()))
                    .build();
        }
        return null;
    }

    public static void applyValidation(QuestionDef questionDef, SurveyJSQuestion targetQuestion) {
        for (RuleDef ruleDef : questionDef.getValidations()) {
            SurveyJsValidator validator = convert(ruleDef);
            if (validator != null) {
                targetQuestion.getValidators().add(ValidationConverter.convert(ruleDef));
            }
            if (ruleDef.getRuleType().equals(RuleType.REQUIRED)) {
                targetQuestion.setRequired(true);
                targetQuestion.setRequiredText(
                        ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                                ruleDef.getHintTemplate().getVariables())
                );
            }
        }
    }
}
