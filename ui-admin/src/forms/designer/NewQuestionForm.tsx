import React, { useState } from 'react'

import { Question, QuestionType } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'
import { baseQuestions } from './questions/questionTypes'
import _ from 'lodash'
import { Textarea } from 'components/forms/Textarea'
import { questionFromRawText } from 'util/pearlSurveyUtils'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, readOnly } = props
  const [question, setQuestion] = useState<Question>(baseQuestions['text'])
  const [selectedQuestionType, setSelectedQuestionType] = useState<string | undefined>(undefined)
  const [questionStableId, setQuestionStableId] = useState<string>('')
  const [freetext, setFreetext] = useState<string | undefined>(undefined)

  return (
    <>
      <div className="text-align-right">
        <div className="mb-3">
          <div className="mb-3">
            <TextInput
              description='The unique stable identifier for the survey question'
              label='Question stable ID'
              value={questionStableId}
              onChange={value => {
                setQuestionStableId(value)
                setQuestion({ ...question, name: value })
              }}
            />
          </div>
          <label className="form-label" htmlFor="questionType">Question type</label>
          <select id="questionType" defaultValue="placeholder" className="form-select" value={selectedQuestionType}
            onChange={e => {
              if (e.target.value === 'freetext') {
                setFreetext('')
              } else {
                const newQuestionType = e.target.value as QuestionType
                setSelectedQuestionType(newQuestionType)
                setQuestion({ ...baseQuestions[newQuestionType], ...question, type: newQuestionType } as Question)
                setFreetext(undefined)
              }
            }}>
            <option value="placeholder" disabled={true}>Select a question type</option>
            <option value="text">Text</option>
            <option value="checkbox">Checkbox</option>
            <option value="dropdown">Dropdown</option>
            <option value="medications">Medications</option>
            <option value="radiogroup">Radio group</option>
            <option value="signaturepad">Signature</option>
            <option value="freetext">Automatically detect my question type (freetext)</option>
          </select>
        </div>

        { freetext !== undefined && <div className="mb-3">
          <Textarea
            description="Enter your question text here.
            As you type, we'll try to automatically detect the various question fields."
            disabled={readOnly}
            label="Freetext"
            rows={2}
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

        { (freetext !== undefined || selectedQuestionType) && <QuestionDesigner
          question={question}
          showName={false}
          readOnly={readOnly}
          onChange={updatedElement => {
            setQuestion(updatedElement)
          }}
        /> }

        <Button
          variant="primary"
          disabled={readOnly || !selectedQuestionType || !questionStableId}
          onClick={() => {
            //While preserving the state during editing, we may have accumulated some extra fields
            //that don't exist on the final question type. So we need to remove them before saving.
            const sanitizedQuestion = _.pick(question,
              Object.keys(baseQuestions[selectedQuestionType as QuestionType])) as Question
            onCreate(sanitizedQuestion)
          }}
        >
          Create question
        </Button>
      </div>
    </>
  )
}
