import React from 'react'

import { HtmlQuestion, InteractiveQuestion, Question } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'
import { getI18nSurveyElement } from 'util/juniperSurveyUtils'

type BaseFieldsProps = {
  disabled: boolean
  question: Question
  onChange: (newValue: Question) => void
}

/** Controls for editing base question fields. */
export const BaseFields = (props: BaseFieldsProps) => {
  const { disabled, question, onChange } = props
  if ((question as HtmlQuestion).type === 'html') {
    return null
  }
  const regularQuestion = question as InteractiveQuestion
  return (
    <>
      <div className="mb-3">
        <Textarea
          disabled={disabled}
          label="Question text"
          required={!Object.hasOwnProperty.call(question, 'questionTemplateName')}
          rows={2}
          value={getI18nSurveyElement(regularQuestion.title)}
          onChange={value => {
            onChange({
              ...regularQuestion,
              title: value
            })
          }}
        />
      </div>

      <div className="mb-3">
        <Textarea
          infoContent="Optional additional context for the question.
           Will be displayed in a smaller font beneath the main question text"
          disabled={disabled}
          label="Description"
          rows={2}
          value={getI18nSurveyElement(regularQuestion.description)}
          onChange={value => {
            onChange({
              ...regularQuestion,
              description: value
            })
          }}
        />
      </div>

      <div className="mb-3">
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
      </div>
    </>
  )
}
