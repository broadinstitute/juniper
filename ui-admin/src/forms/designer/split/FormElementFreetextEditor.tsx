import { Question, QuestionType } from '@juniper/ui-core'
import React, { useState } from 'react'
import { Textarea } from 'components/forms/Textarea'
import { questionFromRawText } from 'util/juniperSurveyUtils'

export const FormElementFreetextEditor = ({ question, onChange }: {
    question: Question, onChange: (newQuestion: Question) => void
}) => {
  const [freetext, setFreetext] = useState<string>('')

  return <Textarea
    className="form-control mb-3 p-2"
    value={freetext}
    rows={15}
    onChange={value => {
      setFreetext(value)
      const newQuestionObj = questionFromRawText(value)
      const newType = newQuestionObj.type as QuestionType
      //questionFromRawText can only reasonably predict the type of question, it's choices, and it's text.
      //We'll pick those three fields out and allow the user to decide the rest of the fields explicitly.
      onChange({
        ...question,
        type: newType,
        title: newQuestionObj.title || '',
        choices: newQuestionObj.choices
      } as Question)
    }}
    label={'Freetext'}
    labelClassname={'mb-0'}
    infoContent={`Paste in question text to automatically generate a survey question. For text questions, 
    simply paste in the text. For radio and dropdown questions, paste the question text followed by
    a new option on each line.`}
  />
}
