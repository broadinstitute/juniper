import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'
import { baseQuestions } from './questions/questionTypes'
import _ from 'lodash'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, readOnly } = props
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>('text')

  const [question, setQuestion] = useState<Question>(baseQuestions[selectedQuestionType])
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
            <option value="text">Text</option>
            <option value="checkbox">Checkbox</option>
            <option value="dropdown">Dropdown</option>
            <option value="medications">Medications</option>
            <option value="radiogroup">Radio group</option>
            <option value="signaturepad">Signature</option>
          </select>
        </div>

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
