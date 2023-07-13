import React from 'react'

import { Question } from '@juniper/ui-core'

import { BaseFields } from './questions/BaseFields'
import { CheckboxFields } from './questions/CheckboxFields'
import { ChoicesList } from './questions/ChoicesList'
import { OtherOptionFields } from './questions/OtherOptionFields'
import { questionTypeDescriptions, questionTypeLabels } from './questions/questionTypes'
import { TextFields } from './questions/TextFields'
import { VisibilityFields } from './questions/VisibilityFields'

export type QuestionDesignerProps = {
  question: Question
  readOnly: boolean
  showTitle: boolean
  onChange: (newValue: Question) => void
}

/** UI for editing a question in a form. */
export const QuestionDesigner = (props: QuestionDesignerProps) => {
  const { question, readOnly, showTitle, onChange } = props

  const isTemplated = 'questionTemplateName' in question

  return (
    <div>
      {showTitle && <h2>{question.name}</h2>}

      {!isTemplated && (
        <>
          <p className="fs-4 mb-0">{questionTypeLabels[question.type]} question</p>
          <p>{questionTypeDescriptions[question.type]}</p>
        </>
      )}

      {isTemplated && (
        <>
          <p className="fs-4 mb-0">Templated question</p>
          <p>
            This question uses <span className="fw-bold">{question.questionTemplateName}</span> as a template.
            Question settings entered here override settings from the template.
          </p>
        </>
      )}

      <BaseFields
        disabled={readOnly}
        question={question}
        onChange={onChange}
      />

      {!isTemplated && (
        <>
          {
            (question.type === 'checkbox' || question.type === 'dropdown' || question.type === 'radiogroup') && (
              <>
                <ChoicesList
                  question={question}
                  readOnly={readOnly}
                  onChange={onChange}
                />
                <OtherOptionFields
                  disabled={readOnly}
                  question={question}
                  onChange={onChange}
                />
              </>
            )
          }
          {
            question.type === 'checkbox' && (
              <CheckboxFields
                disabled={readOnly}
                question={question}
                onChange={onChange}
              />
            )
          }
          {
            question.type === 'text' && (
              <TextFields
                disabled={readOnly}
                question={question}
                onChange={onChange}
              />
            )
          }
        </>
      )}

      <VisibilityFields
        disabled={readOnly}
        question={question}
        onChange={onChange}
      />
    </div>
  )
}
