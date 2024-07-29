import React from 'react'

import { HtmlQuestion, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'

import { BaseFields } from './questions/BaseFields'
import { ChoicesList } from './questions/ChoicesList'
import { questionTypeDescriptions, questionTypeLabels } from './questions/questionTypes'
import { TextFields } from './questions/TextFields'
import { VisibilityFields } from './questions/VisibilityFields'
import { Textarea } from 'components/forms/Textarea'
import { Button } from 'components/forms/Button'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import InfoPopup from '../../components/forms/InfoPopup'
import { i18nSurveyText } from 'util/juniperSurveyUtils'

export type QuestionDesignerProps = {
  question: Question
  isNewQuestion: boolean
  readOnly: boolean
  showName: boolean
  showQuestionTypeHeader?: boolean
  currentLanguage: PortalEnvironmentLanguage
  supportedLanguages: PortalEnvironmentLanguage[]
  onChange: (newValue: Question) => void
  addNextQuestion?: () => void
}

/** UI for editing a question in a form. */
export const QuestionDesigner = (props: QuestionDesignerProps) => {
  const {
    question, isNewQuestion, readOnly, showName, showQuestionTypeHeader = true,
    onChange, addNextQuestion, currentLanguage, supportedLanguages
  } = props

  const isTemplated = 'questionTemplateName' in question

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between">
        {showName && <h2>{question.name}</h2>}
        {addNextQuestion && <div>
          <Button variant="secondary" className="ms-auto" onClick={addNextQuestion}>
            <FontAwesomeIcon icon={faPlus}/> Add next question
          </Button>
        </div>}
      </div>
      {!isTemplated && showQuestionTypeHeader && (
        <>
          <p className="fs-4">{questionTypeLabels[question.type]} question
            <InfoPopup content={questionTypeDescriptions[question.type]}/>
          </p>
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
        currentLanguage={currentLanguage}
        supportedLanguages={supportedLanguages}
        question={question}
        onChange={onChange}
      />

      {!isTemplated && (
        <>
          {
            (question.type === 'checkbox' || question.type === 'dropdown' || question.type === 'radiogroup') && (
              <ChoicesList
                question={question}
                isNewQuestion={isNewQuestion}
                currentLanguage={currentLanguage}
                supportedLanguages={supportedLanguages}
                readOnly={readOnly}
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
          {
            question.type === 'html' && <Textarea
              disabled={readOnly}
              label="HTML"
              rows={5}
              value={i18nSurveyText((question as HtmlQuestion)?.html)}
              onChange={value => {
                onChange({
                  ...question,
                  html: value
                })
              }}
            />
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
