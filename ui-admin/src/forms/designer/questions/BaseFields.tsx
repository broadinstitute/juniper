import React, { useId } from 'react'

import { HtmlQuestion, InteractiveQuestion, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'
import { i18nSurveyText, updateI18nSurveyText } from 'util/juniperSurveyUtils'
import { CollapsibleSectionButton } from 'portal/siteContent/designer/components/CollapsibleSectionButton'

type BaseFieldsProps = {
    showIsRequired?: boolean
  disabled: boolean
  question: Question
  currentLanguage: PortalEnvironmentLanguage
  supportedLanguages: PortalEnvironmentLanguage[]
  onChange: (newValue: Question) => void
}

/** Controls for editing base question fields. */
export const BaseFields = (props: BaseFieldsProps) => {
  const { showIsRequired = true, disabled, question, onChange } = props
  if ((question as HtmlQuestion).type === 'html') {
    return null
  }
  const regularQuestion = question as InteractiveQuestion
  const additionalFieldsTargetId = useId()

  return (
    <>
      <div className="mb-3">
        <Textarea
          disabled={disabled}
          label="Text"
          required={!Object.hasOwnProperty.call(question, 'questionTemplateName')}
          rows={2}
          value={i18nSurveyText(regularQuestion.title, props.currentLanguage.languageCode)}
          onChange={valueText => {
            onChange({
              ...regularQuestion,
              title: updateI18nSurveyText({
                valueText,
                oldValue: regularQuestion.title,
                languageCode: props.currentLanguage.languageCode,
                supportedLanguages: props.supportedLanguages
              })
            })
          }}
        />
      </div>

      {showIsRequired && <div className="mb-3">
        <Checkbox
          checked={!!regularQuestion.isRequired}
          infoContent="If checked, participants will be required to enter a response
          before they can continue to the next page of the form."
          disabled={disabled}
          label="Require response"
          onChange={checked => {
            onChange({
              ...regularQuestion,
              isRequired: checked
            })
          }}
        />
      </div>}

      <div className="bg-white rounded-3 p-2 mb-2">
        <CollapsibleSectionButton targetSelector={`#${additionalFieldsTargetId}`}
          sectionLabel={'Additional options'}/>
        <div className="mb-3 collapse hide" id={additionalFieldsTargetId}>
          <Textarea
            infoContent="Optional additional context for the question.
           Will be displayed in a smaller font beneath the main question text"
            disabled={disabled}
            label="Description"
            rows={2}
            value={i18nSurveyText(regularQuestion.description)}
            onChange={value => {
              onChange({
                ...regularQuestion,
                description: value
              })
            }}
          />
        </div>
      </div>
    </>
  )
}
