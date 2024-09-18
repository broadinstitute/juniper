import { FormContent } from '@juniper/ui-core'
import React from 'react'
import { Button } from 'components/forms/Button'
import { baseQuestions } from 'forms/designer/questions/questionTypes'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'

export const InsertElementControls = ({ formContent, onChange, elementIndex, pageIndex }: {
    formContent: FormContent, onChange: (newContent: FormContent) => void, elementIndex: number, pageIndex: number
}) => {
  return (
    <div className='d-flex'>
      {renderInsertElementButton(formContent, onChange, elementIndex, pageIndex, 'question')}
      {renderInsertElementButton(formContent, onChange, elementIndex, pageIndex, 'panel')}
    </div>
  )
}

type ElementType = 'question' | 'panel'

const renderInsertElementButton = (formContent: FormContent, onChange: (newContent: FormContent) => void,
  elementIndex: number, pageIndex: number, elementType: ElementType) => {
  return <div className="my-2">
    <Button variant="light"
      className={'border m-1'}
      aria-label={`Insert a new ${elementType}`}
      tooltip={`Insert a new ${elementType}`}
      disabled={false}
      onClick={() => {
        const newContent = { ...formContent }
        newContent.pages[pageIndex].elements.splice(elementIndex + 1, 0, (elementType == 'panel') ? {
          title: '',
          type: 'panel',
          elements: []
        } :
          baseQuestions['text']
        )
        onChange(newContent)
      }}>
      <FontAwesomeIcon icon={faPlus}/> Insert {elementType}
    </Button>
  </div>
}
