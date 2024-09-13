import React from 'react'

import { HtmlQuestion, PortalEnvironmentLanguage, Question, QuestionType } from '@juniper/ui-core'

import { Textarea } from 'components/forms/Textarea'
import { i18nSurveyText } from 'util/juniperSurveyUtils'
import { BaseFields } from '../questions/BaseFields'
import { ChoicesList } from '../questions/ChoicesList'
import { TextFields } from '../questions/TextFields'
import { VisibilityFields } from '../questions/VisibilityFields'
import { TextInput } from 'components/forms/TextInput'
import { IconButton } from 'components/forms/Button'
import { faAsterisk } from '@fortawesome/free-solid-svg-icons'
import QuestionTypeSelector from './QuestionTypeSelector'
import classNames from 'classnames'
import { baseQuestions } from '../questions/questionTypes'

export type QuestionDesignerProps = {
    question: Question
    isNewQuestion: boolean
    readOnly: boolean
    currentLanguage: PortalEnvironmentLanguage
    supportedLanguages: PortalEnvironmentLanguage[]
    onChange: (newValue: Question) => void
}

/**
 * UI for editing a question in a form.
 * Note that this will eventually replace the QuestionDesigner component
 */
export const FullQuestionDesigner = (props: QuestionDesignerProps) => {
  const {
    question, isNewQuestion, readOnly,
    onChange, currentLanguage, supportedLanguages
  } = props

  const isTemplated = 'questionTemplateName' in question

  return (
    <div>
      <label className={'form-label fw-semibold mb-0'}>Stable ID</label>
      <span className="text-danger fw-semibold ms-1">*</span>
      <div className="d-flex align-items-center">
        <div className="w-100">
          <TextInput
            className={'mb-2'}
            required={true}
            value={question.name}
            onChange={name => {
              onChange({
                ...question,
                name
              })
            }}
          />
        </div>
        { (!isTemplated && question.type !== 'html') &&
            <IconButton
              className={classNames('mb-2', 'ms-1', 'border', question.isRequired ? 'text-danger' : 'text-muted')}
              icon={faAsterisk}
              aria-label={'Toggle required'}
              onClick={() => {
                onChange({
                  ...question,
                  isRequired: !question.isRequired
                })
              }}
            /> }
      </div>
      { !isTemplated && <QuestionTypeSelector
        key={question.name}
        questionType={question.type}
        onChange={newType => {
          onChange({
            ...baseQuestions[newType as QuestionType],
            ...question,
            // @ts-ignore
            type: newType
          })
        }}
      /> }

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
        showIsRequired={false}
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
              <>
                <ChoicesList
                  question={question}
                  isNewQuestion={isNewQuestion}
                  currentLanguage={currentLanguage}
                  supportedLanguages={supportedLanguages}
                  readOnly={readOnly}
                  onChange={onChange}
                />
              </>
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
            question.type === 'html' &&
              <div className="mb-3">
                <Textarea
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
              </div>
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
