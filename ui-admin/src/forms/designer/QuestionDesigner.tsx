import React from 'react'

import { Question } from '@juniper/ui-core'

import { BaseFields } from './questions/BaseFields'
import { CheckboxFields } from './questions/CheckboxFields'
import { ChoicesList } from './questions/ChoicesList'
import { OtherOptionFields } from './questions/OtherOptionFields'
import { questionTypeDescriptions, questionTypeLabels } from './questions/questionTypes'
import { VisibilityFields } from './questions/VisibilityFields'

export type QuestionDesignerProps = {
  readOnly: boolean
  value: Question
  onChange: (newValue: Question) => void
}

/** UI for editing a question in a form. */
export const QuestionDesigner = (props: QuestionDesignerProps) => {
  const { readOnly, value, onChange } = props

  const isTemplated = 'questionTemplateName' in value

  return (
    <div>
      <h2>{value.name}</h2>

      {!isTemplated && (
        <>
          <p className="fs-4 mb-0">{questionTypeLabels[value.type]} question</p>
          <p>{questionTypeDescriptions[value.type]}</p>
        </>
      )}

      {isTemplated && (
        <>
          <p className="fs-4 mb-0">Templated question</p>
          <p>
            This question uses <span className="fw-bold">{value.questionTemplateName}</span> as a template.
            Question settings entered here override settings from the template.
          </p>
        </>
      )}

      <BaseFields
        disabled={readOnly}
        question={value}
        onChange={onChange}
      />

      {!isTemplated && (
        <>
          {
            (value.type === 'checkbox' || value.type === 'dropdown' || value.type === 'radiogroup') && (
              <>
                <ChoicesList
                  question={value}
                  readOnly={readOnly}
                  onChange={onChange}
                />
                <OtherOptionFields
                  disabled={readOnly}
                  question={value}
                  onChange={onChange}
                />
              </>
            )
          }
          {
            value.type === 'checkbox' && (
              <CheckboxFields
                disabled={readOnly}
                question={value}
                onChange={onChange}
              />
            )
          }
        </>
      )}

      <VisibilityFields
        disabled={readOnly}
        question={value}
        onChange={onChange}
      />
    </div>
  )
}
