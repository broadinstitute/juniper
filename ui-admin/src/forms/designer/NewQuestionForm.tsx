import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from '../../components/forms/TextInput'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate } = props
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>('text')
  const [questionName, setQuestionName] = useState<string>('')

  const emptyQuestions: Record<QuestionType, Question> = {
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

  const [question, setQuestion] = useState<Question>(emptyQuestions[selectedQuestionType])

  // //TODO: probably don't need this as an effect
  // useEffect(() => {
  //   setQuestion(emptyQuestions[selectedQuestionType])
  // }, [selectedQuestionType])

  return (
    <>
      <div className="text-align-right">
        <div className="mb-3">
          <div className="mb-3">
            <TextInput
              description='The stable identifier for the survey question'
              label='Question name'
              value={questionName}
              onChange={value => {
                setQuestionName(value)
                setQuestion({ ...question, name: value })
              }}
            />
          </div>
          <label className="form-label" htmlFor="text-question-input-type">Question type</label>
          <select id="questionType" className="form-select" value={selectedQuestionType}
            onChange={e => {
              const newQuestionType = e.target.value as QuestionType
              setSelectedQuestionType(newQuestionType)
              setQuestion(emptyQuestions[newQuestionType])
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
          showTitle={false}
          readOnly={false} //maybe pass this in from parent component, but you can't really get here if it's read-only
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
