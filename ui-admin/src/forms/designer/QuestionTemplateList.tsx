import { faTimes } from '@fortawesome/free-solid-svg-icons'
import { compact, flow, uniq, map } from 'lodash/fp'
import React from 'react'

import { FormContent, getFormElements, Question } from '@juniper/ui-core'

import { IconButton } from 'components/forms/Button'

const isQuestionTemplateReferencedInFormContent =
  (formContent: FormContent, questionTemplate: Question): boolean => {
    const allReferencedQuestionTemplates: string[] = flow(
      getFormElements,
      map('questionTemplateName'),
      compact,
      uniq
    )(formContent)
    return allReferencedQuestionTemplates.includes(questionTemplate.name)
  }

type QuestionTemplateListProps = {
  formContent: FormContent
  readOnly: boolean
  onChange: (newValue: FormContent) => void
}

/** UI for viewing a list of question templates in a form. */
export const QuestionTemplateList = (props: QuestionTemplateListProps) => {
  const { formContent, readOnly, onChange } = props
  const { questionTemplates = [] } = formContent

  return (
    <>
      <h2>Question Templates</h2>
      <ul className="list-group">
        {questionTemplates.map((question, i) => {
          const isReferenced = isQuestionTemplateReferencedInFormContent(formContent, question)
          return (
            <li
              key={i}
              className="list-group-item d-flex align-items-center"
            >
              <div className="flex-grow-1 text-truncate ms-2">
                {question.name}
              </div>
              <div className="flex-shrink-0">
                <IconButton
                  aria-label={isReferenced
                    ? 'This question template cannot be deleted while a question references it'
                    : 'Delete this question template'
                  }
                  className="ms-2"
                  disabled={readOnly || isReferenced}
                  icon={faTimes}
                  variant="light"
                  onClick={() => {
                    onChange({
                      ...formContent,
                      questionTemplates: [
                        ...questionTemplates.slice(0, i),
                        ...questionTemplates.slice(i + 1)
                      ]
                    })
                  }}
                />
              </div>
            </li>
          )
        })}
      </ul>
    </>
  )
}
