import React from 'react'

import { CheckboxQuestion, DropdownQuestion, RadiogroupQuestion } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { TextInput } from 'components/forms/TextInput'
import { i18nSurveyText } from 'util/juniperSurveyUtils'

type QuestionWithOtherOption = CheckboxQuestion | DropdownQuestion | RadiogroupQuestion

type OtherOptionFieldsProps = {
  disabled: boolean
  question: QuestionWithOtherOption
  onChange: (newValue: QuestionWithOtherOption) => void
}

/** Controls for editing other option for questions. */
export const OtherOptionFields = (props: OtherOptionFieldsProps) => {
  const { disabled, question, onChange } = props

  return (
    <>
      <div className="mt-3 mb-3">
        <Checkbox
          checked={!!question.showOtherItem}
          disabled={disabled}
          label={'Show "Other" option'}
          onChange={checked => {
            if (checked) {
              const { otherText, otherPlaceholder, otherErrorText } = question
              onChange({
                ...question,
                showOtherItem: true,
                otherText: otherText || 'Other',
                otherPlaceholder: otherPlaceholder || 'Please specify',
                otherErrorText: otherErrorText || 'A description is required for choices of "other".'
              })
            } else {
              onChange({
                ...question,
                showOtherItem: false
              })
            }
          }}
        />

      </div>

      {!!question.showOtherItem && (
        <fieldset>
          <div className="mb-3">
            <TextInput
              disabled={disabled}
              label="Label"
              labelClassname="mb-0"
              infoContent="Label for the 'Other' option."
              value={i18nSurveyText(question.otherText)}
              onChange={value => {
                onChange({
                  ...question,
                  otherText: value
                })
              }}
            />
          </div>

          <div className="mb-3">
            <TextInput
              disabled={disabled}
              label="Placeholder"
              labelClassname="mb-0"
              infoContent="Placeholder text for the text response input."
              value={i18nSurveyText(question.otherPlaceholder)}
              onChange={value => {
                onChange({
                  ...question,
                  otherPlaceholder: value
                })
              }}
            />
          </div>

          <div className="mb-3">
            <TextInput
              disabled={disabled}
              label="Error message"
              labelClassname="mb-0"
              // eslint-disable-next-line max-len
              infoContent="Error message shown if the participant selects the 'Other' option but does not provide a text response."
              value={i18nSurveyText(question.otherErrorText)}
              onChange={value => {
                onChange({
                  ...question,
                  otherErrorText: value
                })
              }}
            />
          </div>
        </fieldset>
      )}
    </>
  )
}
