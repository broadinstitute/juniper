import React from 'react'

import { CheckboxQuestion } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { TextInput } from 'components/forms/TextInput'

type CheckboxFieldsProps = {
  disabled: boolean
  question: CheckboxQuestion
  onChange: (newValue: CheckboxQuestion) => void
}

/** Controls for editing other option for questions. */
export const CheckboxFields = (props: CheckboxFieldsProps) => {
  const { disabled, question, onChange } = props

  return (
    <>
      <div className="mb-3">
        <Checkbox
          checked={!!question.showNoneItem}
          description={'Show a "None" option that, when selected, clears all other selections for this question.'}
          disabled={disabled}
          label={'Show "None" option'}
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
          <legend className="form-label fs-5">&ldquo;None&rdquo; option</legend>

          <div className="mb-3">
            <TextInput
              description={'Label for the "None" option.'}
              disabled={disabled}
              label="Label"
              value={question.noneText || ''}
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
              description={'Value for the "None" option.'}
              disabled={disabled}
              label="Value"
              value={question.noneValue || ''}
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
