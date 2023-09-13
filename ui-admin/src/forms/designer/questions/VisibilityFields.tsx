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
        Here are a few examples:
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
    <>
      <div className="mb-3">
        <Checkbox
          checked={hasVisibleIfExpression}
          description="Show or hide this question based on responses to other questions."
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
        <div className="mb-3">
          <TextInput
            infoContent={infoPopupContent}
            // eslint-disable-next-line max-len
            description={'Expression for this question\'s visibility. If this expression evaluates to true, the question will be shown.'}
            disabled={disabled}
            label="Visibility expression"
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
    </>
  )
}
