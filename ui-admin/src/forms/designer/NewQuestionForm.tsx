import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'
import { baseQuestions } from './questions/questionTypes'
import _ from 'lodash'
import { Textarea } from '../../components/forms/Textarea'
import { questionFromRawText } from '../../util/pearlSurveyUtils'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, readOnly } = props
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>('text')
  const [questionName, setQuestionName] = useState<string>('')
  const [freetextMode, setFreetextMode] = useState<boolean>(false)
  const [freetext, setFreetext] = useState<string>('')

  const [question, setQuestion] = useState<Question>(baseQuestions[selectedQuestionType])

  return (
    <>
      <div className="text-align-right">
        <div className="mb-3">
          <div className="mb-3">
            <TextInput
              description='The unique stable identifier for the survey question'
              label='Question stable ID'
              value={questionName}
              onChange={value => {
                setQuestionName(value)
                setQuestion({ ...question, name: value })
              }}
            />
          </div>
          <label className="form-label" htmlFor="questionType">Question type</label>
          <select id="questionType" className="form-select" value={selectedQuestionType}
            onChange={e => {
              const newQuestionType = e.target.value as QuestionType
              setSelectedQuestionType(newQuestionType)
              // @ts-ignore
              if (e.target.value === 'autofill') {
                setFreetextMode(true)
              } else {
                // @ts-ignore
                setQuestion({ ...baseQuestions[newQuestionType], ...question, type: newQuestionType })
                setFreetextMode(false)
              }
            }}>
            <option value="text">Text</option>
            <option value="checkbox">Checkbox</option>
            <option value="dropdown">Dropdown</option>
            <option value="medications">Medications</option>
            <option value="radiogroup">Radio group</option>
            <option value="signaturepad">Signature</option>
            <option value="autofill">Automatically detect my question type (freetext)</option>
          </select>
        </div>

        { freetextMode && <div className="mb-3">
          <Textarea
            description="Enter your question text here.
            We'll try to automatically detect the question type and populate the fields."
            disabled={readOnly}
            label="Freetext"
            rows={2}
            value={freetext}
            onChange={value => {
              setFreetext(value)
              const newQuestionObj = questionFromRawText(value)
              const newType = newQuestionObj.type as QuestionType
              setSelectedQuestionType(newType)
              console.log(newQuestionObj)
              //todo: which types actually need to be set here?
              setQuestion({
                ...question,
                // @ts-ignore
                type: newType,
                name: questionName,
                title: newQuestionObj.title || '',
                //TODO: harmonize the question choice types
                // @ts-ignore
                choices: newQuestionObj.choices
              })
            }}
          />
        </div> }

        { (freetext || !freetextMode) && selectedQuestionType && <QuestionDesigner
          question={question}
          showName={false}
          readOnly={readOnly}
          onChange={updatedElement => {
            setQuestion(updatedElement)
          }}
        /> }

        <Button
          variant="primary"
          onClick={() => {
            //While preserving the state during editing, we may have accumulated some extra fields
            //that don't exist on the final question type. So we need to remove them before saving.
            const sanitizedQuestion = _.pick(question, Object.keys(baseQuestions[selectedQuestionType])) as Question
            onCreate(sanitizedQuestion)
          }}
        >
          Create question
        </Button>
      </div>
    </>
  )
}
