import React, { useState } from 'react'

import { HtmlQuestion, PortalEnvironmentLanguage, Question, QuestionType } from '@juniper/ui-core'

import { Textarea } from 'components/forms/Textarea'
import { i18nSurveyText } from 'util/juniperSurveyUtils'
import { BaseFields } from '../questions/BaseFields'
import { ChoicesList } from '../questions/ChoicesList'
import { VisibilityFields } from '../questions/VisibilityFields'
import { TextInput } from 'components/forms/TextInput'
import { IconButton } from 'components/forms/Button'
import { faAsterisk, faEye, faGear, faList, faPenToSquare } from '@fortawesome/free-solid-svg-icons'
import QuestionTypeSelector from './QuestionTypeSelector'
import classNames from 'classnames'
import { baseQuestions } from '../questions/questionTypes'
import { Tab, Tabs } from 'react-bootstrap'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { TextFields } from '../questions/TextFields'

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
  const [activeTab, setActiveTab] = useState<string | null>('visibility')
  const isTemplated = 'questionTemplateName' in question

  return (
    <div className={'pb-2'}>
      <label className={'form-label fw-semibold mb-0'}>Stable ID</label>
      <span className="text-danger fw-semibold ms-1">*</span>
      <div className="d-flex align-items-center">
        <div className="w-100">
          <TextInput
            className={'mb-2'}
            placeholder={'Enter a stable identifier for this question'}
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
        hideDescription={true}
        disabled={readOnly}
        currentLanguage={currentLanguage}
        supportedLanguages={supportedLanguages}
        question={question}
        onChange={onChange}
      />

      {!isTemplated && (
        <>
          {
            question.type === 'html' &&
              <div className="mb-3">
                <Textarea
                  disabled={readOnly}
                  label="HTML"
                  labelClassname={'mb-0'}
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

      <Tabs
        activeKey={activeTab ?? undefined}
        mountOnEnter
        unmountOnExit
        onSelect={setActiveTab}
      >
        <Tab
          eventKey="visibility"
          title={<><FontAwesomeIcon icon={faEye}/> Visibility</>}
        >
          <VisibilityFields
            disabled={readOnly}
            question={question}
            onChange={onChange}
          />
        </Tab>
        { (!isTemplated && question.type === 'text') && <Tab
          eventKey="input"
          title={<><FontAwesomeIcon icon={faPenToSquare}/> Input</>}
        >
          <TextFields
            disabled={readOnly}
            question={question}
            onChange={onChange}
          />
        </Tab> }
        { (!isTemplated &&
            (question.type === 'checkbox' || question.type === 'dropdown' || question.type === 'radiogroup')) && <Tab
          eventKey="choices"
          title={<><FontAwesomeIcon icon={faList}/> Choices ({question.choices.length})</>}
        >
          <ChoicesList
            question={question}
            isNewQuestion={isNewQuestion}
            currentLanguage={currentLanguage}
            supportedLanguages={supportedLanguages}
            readOnly={readOnly}
            onChange={onChange}
          />
        </Tab> }
        { (!isTemplated && question.type !== 'html') && <Tab
          eventKey="advanced"
          title={<><FontAwesomeIcon icon={faGear}/> Advanced</>}
        >
          <div className='bg-white rounded-left-3 rounded-bottom-3 p-2 mb-2 border border-top-0'>
            <div className="m-2">
              <Textarea
                infoContent="Optional additional context for the question.
                Will be displayed in a smaller font beneath the main question text"
                disabled={readOnly}
                label="Description"
                labelClassname={'mb-0'}
                rows={2}
                value={i18nSurveyText(question.description)}
                onChange={value => {
                  onChange({
                    ...question,
                    description: value
                  })
                }}
              />
            </div>
          </div>
        </Tab> }
      </Tabs>


    </div>
  )
}
