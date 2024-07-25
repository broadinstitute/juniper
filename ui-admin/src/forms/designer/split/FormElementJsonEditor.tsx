import { Question } from '@juniper/ui-core'
import React, { useState } from 'react'
import { Textarea } from 'components/forms/Textarea'

/**
 * A JSON editor for a form element.
 */
export const FormElementJsonEditor = ({ question, onChange }: {
    question: Question, onChange: (newQuestion: Question) => void
}) => {
  const [editedContent, setEditedContent] = useState(JSON.stringify(question, null, 2))
  return <Textarea
    className="form-control"
    value={editedContent}
    rows={15}
    onChange={updatedContent => {
      try {
        onChange(JSON.parse(updatedContent))
        setEditedContent(updatedContent)
      } catch (e) {
        setEditedContent(updatedContent)
      }
    }}
    label={'Element JSON'}
    infoContent={'Edit the element JSON directly. Learn more about SurveyJS JSON here.'}
  />
}
