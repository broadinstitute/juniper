import React, { useId, useState } from 'react'
import Select from 'react-select'

/**
 *
 */
const QuestionTypeSelector = ({ questionType, onChange }: {
    questionType: string, onChange: (newType: string) => void
}) => {
  const [selectedQuestionType, setSelectedQuestionType] = useState<string | undefined>(questionType)

  const questionTypes = [
    { label: 'Text', value: 'text' },
    { label: 'Checkbox', value: 'checkbox' },
    { label: 'Dropdown', value: 'dropdown' },
    { label: 'Medications', value: 'medications' },
    { label: 'Radiogroup', value: 'radiogroup' },
    { label: 'Signature pad', value: 'signaturepad' },
    { label: 'HTML', value: 'html' }
  ]

  const labelId = useId()

  return (
    <div className="mb-3">
      <label className={'fw-semibold'} htmlFor={labelId}>Type</label>
      <span className="text-danger fw-semibold ms-1">*</span>
      <Select inputId={labelId} options={questionTypes}
        value={questionTypes.find(opt => opt.value === selectedQuestionType)}
        onChange={opt => {
          setSelectedQuestionType(opt?.value)
          if (opt?.value) {
            onChange(opt.value)
          }
        }}/>
    </div>
  )
}

export default QuestionTypeSelector
