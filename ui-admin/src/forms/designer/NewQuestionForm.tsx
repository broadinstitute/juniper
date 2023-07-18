import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, readOnly } = props
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>('text')
  const [questionName, setQuestionName] = useState<string>('')

  const baseQuestions: Record<QuestionType, Question> = {
    checkbox: {
      type: 'checkbox',
      name: questionName,
      title: '',
      choices: []
    },
    dropdown: {
      type: 'dropdown',
      name: questionName,
      title: '',
      choices: []
    },
    medications: {
      type: 'medications',
      name: questionName,
      title: ''
    },
    radiogroup: {
      type: 'radiogroup',
      name: questionName,
      title: '',
      choices: []
    },
    signaturepad: {
      type: 'signaturepad',
      name: questionName,
      title: ''
    },
    text: {
      type: 'text',
      name: questionName,
      title: ''
    }
  }

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
              setQuestion(baseQuestions[newQuestionType])
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
            onCreate(question)
          }}
        >
          Create question
        </Button>
      </div>
    </>
  )
}
