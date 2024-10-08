import { unset } from 'lodash/fp'
import React from 'react'

import { Question } from '@juniper/ui-core'

import { Checkbox } from 'components/forms/Checkbox'
import { TextInput } from 'components/forms/TextInput'

type VisibilityFieldsProps = {
  disabled: boolean
  question: Question
  onChange: (newValue: Question) => void
}

/** Controls for making a question conditionally visible. */
export const VisibilityFields = (props: VisibilityFieldsProps) => {
  const { disabled, question, onChange } = props

  const hasVisibleIfExpression = Object.hasOwnProperty.call(question, 'visibleIf') && question.visibleIf !== undefined

  const infoPopupContent = (
    <div>
      <p>
        If this expression evaluates to true, the question will be shown. Here are a few examples:
        <li><code>{'{age} >= 21'}</code></li>
        <li><code>{'{name} notempty'}</code></li>
        <li><code>{'{languages} contains \'Spanish\''}</code></li>
      </p>
      <p>
        For additional details, please see
        <a
          href={'https://surveyjs.io/form-library/documentation/design-survey/conditional-logic#conditional-visibility'}
          target="_blank"
        > Conditional Visibility documentation</a>
      </p>
    </div>
  )

  return (
    <div className="bg-white rounded-left-3 rounded-bottom-3 p-2 mb-2 border border-top-0">
      <div className="m-2">
        <Checkbox
          checked={hasVisibleIfExpression}
          disabled={disabled}
          label="Conditionally show this question"
          onChange={checked => {
            if (checked) {
              onChange({
                ...question,
                visibleIf: ''
              })
            } else {
              onChange(unset('visibleIf', question))
            }
          }}
        />
      </div>

      {hasVisibleIfExpression && (
        <div className="m-2">
          <TextInput
            infoContent={infoPopupContent}
            disabled={disabled}
            label="Visibility expression"
            labelClassname={'mb-0'}
            required={true}
            value={question.visibleIf}
            onChange={value => {
              onChange({
                ...question,
                visibleIf: value
              })
            }}
          />
        </div>
      )}
    </div>
  )
}
