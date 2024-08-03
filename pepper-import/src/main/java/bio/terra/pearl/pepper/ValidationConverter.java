package bio.terra.pearl.pepper;

import bio.terra.pearl.pepper.dto.SurveyJSQuestion;
import bio.terra.pearl.pepper.dto.SurveyJsValidator;
import org.broadinstitute.ddp.model.activity.definition.question.QuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.TextQuestionDef;
import org.broadinstitute.ddp.model.activity.definition.validation.DateRangeRuleDef;
import org.broadinstitute.ddp.model.activity.definition.validation.IntRangeRuleDef;
import org.broadinstitute.ddp.model.activity.definition.validation.RegexRuleDef;
import org.broadinstitute.ddp.model.activity.definition.validation.RuleDef;
import org.broadinstitute.ddp.model.activity.types.QuestionType;
import org.broadinstitute.ddp.model.activity.types.RuleType;
import org.broadinstitute.ddp.model.activity.types.TextInputType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationConverter {

    public static void applyValidation(QuestionDef questionDef, SurveyJSQuestion targetQuestion) {
        for (RuleDef ruleDef : questionDef.getValidations()) {

            List<SurveyJsValidator> validators = new ArrayList<>();

            // ATCP uses: required, int_range, date_range, and regex, so we only support those for now
            if (ruleDef.getRuleType().equals(RuleType.REQUIRED)) {
                // pepper is weird; if type of question is email, it should add an email validator
                // but the text for the email validation is actually in the required error text

                if (questionDef.getQuestionType().equals(QuestionType.TEXT)) {
                    if (((TextQuestionDef) questionDef).getInputType().equals(TextInputType.EMAIL)) {
                        validators.add(SurveyJsValidator.builder()
                                .type("email")
                                .text(ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                                        ruleDef.getHintTemplate().getVariables()))
                                .build());

                        // let's still add the required validator, though
                    }
                }

                targetQuestion.setIsRequired(true);
                targetQuestion.setRequiredErrorText(
                        ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                                ruleDef.getHintTemplate().getVariables())
                );
            }


            if (ruleDef.getRuleType().equals(RuleType.INT_RANGE)) {
                IntRangeRuleDef intRangeRuleDef = (IntRangeRuleDef) ruleDef;
                validators.add(SurveyJsValidator.builder()
                        .type("numeric")
                        .minValue(intRangeRuleDef.getMin())
                        .maxValue(intRangeRuleDef.getMax())
                        .text(ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                                ruleDef.getHintTemplate().getVariables()))
                        .build());
            }

            if (ruleDef.getRuleType().equals(RuleType.DATE_RANGE)) {
                DateRangeRuleDef dateRangeRuleDef = (DateRangeRuleDef) ruleDef;

                if (Objects.nonNull(dateRangeRuleDef.getStartDate())) {
                    targetQuestion.setMin(dateRangeRuleDef.getStartDate().toString());
                    targetQuestion.setMinErrorText(ActivityImporter.translatePepperTemplate(dateRangeRuleDef.getHintTemplate()));
                }
                if (Objects.nonNull(dateRangeRuleDef.getEndDate())) {
                    targetQuestion.setMax(dateRangeRuleDef.getEndDate().toString());
                    targetQuestion.setMaxErrorText(ActivityImporter.translatePepperTemplate(dateRangeRuleDef.getHintTemplate()));
                }
                if (dateRangeRuleDef.isUseTodayAsEnd()) {
                    targetQuestion.setMaxValueExpression("today()");
                    targetQuestion.setMaxErrorText(ActivityImporter.translatePepperTemplate(dateRangeRuleDef.getHintTemplate()));

                }

            }

            if (ruleDef.getRuleType().equals(RuleType.REGEX)) {
                RegexRuleDef regexRuleDef = (RegexRuleDef) ruleDef;
                validators.add(SurveyJsValidator.builder()
                        .type("regex")
                        .regex(regexRuleDef.getPattern())
                        .text(ActivityImporter.getVariableTranslationsTxt(ruleDef.getHintTemplate().getTemplateText(),
                                ruleDef.getHintTemplate().getVariables()))
                        .build());
            }

            targetQuestion.setValidators(validators);
        }
    }
}
