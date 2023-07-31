import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'
import { baseQuestions } from './questions/questionTypes'
import _ from 'lodash'
import { Textarea } from 'components/forms/Textarea'
import { questionFromRawText } from 'util/pearlSurveyUtils'
import { Checkbox } from '../../components/forms/Checkbox'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, readOnly } = props

  const [question, setQuestion] = useState<Question>(baseQuestions['text'])
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>()
  const [freetext, setFreetext] = useState<string>('')
  const [freetextMode, setFreetextMode] = useState<boolean>(false)
  const { name: questionName } = question

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
                setQuestion({ ...question, name: value })
              }}
            />
          </div>
          <label className="form-label" htmlFor="questionType">Question type</label>
          <select id="questionType" className="form-select" value={selectedQuestionType}
            onChange={e => {
              const newQuestionType = e.target.value as QuestionType
              setSelectedQuestionType(newQuestionType)
              setQuestion({ ...baseQuestions[newQuestionType], ...question, type: newQuestionType } as Question)
            }}>
            <option hidden>Select a question type</option>
            <option value="text">Text</option>
            <option value="checkbox">Checkbox</option>
            <option value="dropdown">Dropdown</option>
            <option value="medications">Medications</option>
            <option value="radiogroup">Radio group</option>
            <option value="signaturepad">Signature</option>
          </select>
        </div>

        <Checkbox
          checked={freetextMode}
          disabled={readOnly}
          description={'Automatically fill out the question fields based on entered text'}
          label={'Enable freetext mode'}
          onChange={checked => {
            setFreetextMode(checked)
          }}
        />

        { freetextMode && <div className="mb-3">
          <Textarea
            description="As you enter text, we'll try to automatically detect the various question fields"
            disabled={readOnly}
            label="Freetext"
            rows={3}
            value={freetext}
            onChange={value => {
              setFreetext(value)
              const newQuestionObj = questionFromRawText(value)
              const newType = newQuestionObj.type as QuestionType
              setSelectedQuestionType(newType)
              //questionFromRawText can only reasonably predict the type of question, it's choices, and it's text.
              //We'll pick those three fields out and allow the user to decide the rest of the fields explicitly.
              setQuestion({
                ...question,
                type: newType,
                title: newQuestionObj.title || '',
                choices: newQuestionObj.choices
              } as Question)
            }}
          />
        </div> }

        { selectedQuestionType && <QuestionDesigner
          question={question}
          showName={false}
          readOnly={readOnly}
          onChange={updatedElement => {
            setQuestion(updatedElement)
          }}
        /> }

        <Button
          variant="primary"
          disabled={readOnly || !selectedQuestionType || !questionName}
          onClick={() => {
            //While preserving the state during editing, we may have accumulated some extra fields
            //that don't exist on the final question type. So we need to remove them before saving.
            const sanitizedQuestion = _.pick(question,
              Object.keys(baseQuestions[selectedQuestionType || 'text'])) as Question
            onCreate(sanitizedQuestion)
          }}
        >
          Create question
        </Button>
      </div>
    </>
  )
}
