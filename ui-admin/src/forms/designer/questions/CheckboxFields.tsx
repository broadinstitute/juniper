import React from 'react'

import { CheckboxQuestion } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { TextInput } from 'components/forms/TextInput'
import { i18nSurveyText } from 'util/juniperSurveyUtils'

type CheckboxFieldsProps = {
  disabled: boolean
  question: CheckboxQuestion
  onChange: (newValue: CheckboxQuestion) => void
}

/** Controls for editing fields specific to checkbox questions. */
export const CheckboxFields = (props: CheckboxFieldsProps) => {
  const { disabled, question, onChange } = props

  return (
    <>
      <div className="mb-3">
        <Checkbox
          checked={!!question.showNoneItem}
          disabled={disabled}
          label={'Show "None" option'}
          infoContent={'Show a "None" option that, when selected, clears all other selections for this question.'}
          onChange={checked => {
            if (checked) {
              const { noneText, noneValue } = question
              onChange({
                ...question,
                showNoneItem: true,
                noneText: noneText || 'None of the above',
                noneValue: noneValue || 'noneOfAbove'
              })
            } else {
              onChange({
                ...question,
                showNoneItem: false
              })
            }
          }}
        />
      </div>

      {!!question.showNoneItem && (
        <fieldset>
          <div className="mb-3">
            <TextInput
              disabled={disabled}
              label="Label"
              labelClassname="mb-0"
              infoContent="Label for the 'None' option."
              value={i18nSurveyText(question.noneText)}
              onChange={value => {
                onChange({
                  ...question,
                  noneText: value
                })
              }}
            />
          </div>

          <div className="mb-3">
            <TextInput
              disabled={disabled}
              label="Value"
              labelClassname="mb-0"
              infoContent="Value for the 'None' option."
              value={i18nSurveyText(question.noneValue)}
              onChange={value => {
                onChange({
                  ...question,
                  noneValue: value
                })
              }}
            />
          </div>
        </fieldset>
      )}
    </>
  )
}
