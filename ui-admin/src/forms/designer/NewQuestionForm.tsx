import React, { useState } from 'react'

import { Question, QuestionType, TemplatedQuestion } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { QuestionDesigner } from './QuestionDesigner'
import { TextInput } from 'components/forms/TextInput'
import { baseQuestions } from './questions/questionTypes'
import { isEmpty, omit, pick } from 'lodash'
import { Textarea } from 'components/forms/Textarea'
import { questionFromRawText } from 'util/juniperSurveyUtils'
import { Checkbox } from '../../components/forms/Checkbox'
import Select from 'react-select'

type NewQuestionFormProps = {
    onCreate: (newQuestion: Question) => void
    questionTemplates: Question[]
    readOnly: boolean
}

/** UI for creating a new question. */
export const NewQuestionForm = (props: NewQuestionFormProps) => {
  const { onCreate, questionTemplates, readOnly } = props

  const [question, setQuestion] = useState<Question>(baseQuestions['text'])
  const [selectedQuestionType, setSelectedQuestionType] = useState<QuestionType>()
  const [selectedQuestionTemplateName, setSelectedQuestionTemplateName] = useState<{value: string, label: string}>()
  const [freetext, setFreetext] = useState<string>('')
  const [freetextMode, setFreetextMode] = useState<boolean>(false)
  const { name: questionName } = question

  return (
    <div className="text-align-right" data-testid="newQuestionForm">
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

        { !isEmpty(questionTemplates) && <div className="mb-3">
          <label className="form-label" htmlFor="questionTemplate">Question template (optional)</label>
          <Select
            options={questionTemplates.map(questionTemplate => {
              return { label: questionTemplate.name, value: questionTemplate.name }
            })}
            isClearable={true}
            value={selectedQuestionTemplateName}
            onChange={newValue => {
              setFreetextMode(false)
              if (newValue) {
                setQuestion({ name: questionName, questionTemplateName: newValue.value } as Question)
                setSelectedQuestionTemplateName(newValue)
                //Change the selected question type to match the template
                const referencedQuestionTemplate = questionTemplates.find(questionTemplate =>
                  questionTemplate.name === newValue.value) as Exclude<Question, TemplatedQuestion>
                setSelectedQuestionType(referencedQuestionTemplate.type)
              } else {
                //Remove the questionTemplateName from the question object
                setQuestion(omit({
                  ...baseQuestions[selectedQuestionType || 'text'],
                  ...question,
                  name: questionName,
                  type: selectedQuestionType
                }, ['questionTemplateName']) as Question)
                setSelectedQuestionTemplateName(undefined)
              }
            }}
          />
          <p
            className="form-text"
            id="questionTemplateDesc"
          >
              Select a question template to pre-populate the question fields
          </p>
        </div> }

        <label className="form-label" htmlFor="questionType">Question type</label>
        <select id="questionType"
          disabled={!!selectedQuestionTemplateName}
          className="form-select"
          value={selectedQuestionType}
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
          <option value="html">Html</option>
        </select>
      </div>

      <Checkbox
        checked={freetextMode}
        disabled={readOnly || !!selectedQuestionTemplateName}
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

      { (selectedQuestionType || selectedQuestionTemplateName) && <QuestionDesigner
        question={question}
        isNewQuestion={true}
        showName={false}
        readOnly={readOnly}
        onChange={updatedElement => {
          setQuestion(updatedElement)
        }}
      /> }

      <Button
        variant="primary"
        disabled={readOnly || (!selectedQuestionType && !selectedQuestionTemplateName) || !questionName}
        onClick={() => {
          //While preserving the state during editing, we may have accumulated some extra fields
          //that don't exist on the final question type. So we need to remove them before saving.
          const sanitizedQuestion = selectedQuestionTemplateName ?
                pick(question, [
                  'name', 'title', 'isRequired', 'visibleIf', 'description', 'questionTemplateName'
                ]) as Question :
                pick(question, Object.keys(baseQuestions[selectedQuestionType || 'text'])) as Question
          onCreate(sanitizedQuestion)
        }}
      >
          Create question
      </Button>
    </div>
  )
}
