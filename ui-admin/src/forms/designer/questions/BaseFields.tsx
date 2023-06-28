import React from 'react'

import { Question } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'

type BaseFieldsProps = {
  disabled: boolean
  question: Question
  onChange: (newValue: Question) => void
}

/** Controls for editing base question fields. */
export const BaseFields = (props: BaseFieldsProps) => {
  const { disabled, question, onChange } = props

  return (
    <>
      <div className="mb-3">
        <Textarea
          disabled={disabled}
          label="Question text"
          rows={2}
          value={question.title}
          onChange={value => {
            onChange({
              ...question,
              title: value
            })
          }}
        />
      </div>

      <div className="mb-3">
        <Textarea
          description="Additional context for the question."
          disabled={disabled}
          label="Description"
          rows={2}
          value={question.description}
          onChange={value => {
            onChange({
              ...question,
              description: value
            })
          }}
        />
      </div>

      <div className="mb-3">
        <Checkbox
          checked={!!question.isRequired}
          // eslint-disable-next-line max-len
          description="If checked, participants will be required to enter a response before they can continue to the next page of the form."
          disabled={disabled}
          label="Require response"
          onChange={checked => {
            onChange({
              ...question,
              isRequired: checked
            })
          }}
        />
      </div>
    </>
  )
}
