import React from 'react'

import { FormElement, FormPanel } from '@juniper/ui-core'

import { PanelElementList } from './PanelElementList'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'

export type PanelDesignerProps = {
  readOnly: boolean
  value: FormPanel
  onChange: (newValue: FormPanel, removedElement?: FormElement) => void
    setSelectedElementPath: (path: string) => void
    selectedElementPath: string
    addNextQuestion?: () => void
}

/** UI for editing a panel in a form. */
export const PanelDesigner = (props: PanelDesignerProps) => {
  const {
    readOnly, value, onChange,
    setSelectedElementPath, selectedElementPath, addNextQuestion
  } = props

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between">
        <h2>Panel</h2>
        {addNextQuestion && <div>
          <Button variant="secondary" className="ms-auto" onClick={addNextQuestion}>
            <FontAwesomeIcon icon={faPlus}/> Add question
          </Button>
        </div>}
      </div>
      <PanelElementList
        readOnly={readOnly}
        value={value.elements}
        onChange={(newValue, removedElement) => {
          onChange({ ...value, elements: newValue }, removedElement)
        }}
        setSelectedElementPath={setSelectedElementPath}
        selectedElementPath={selectedElementPath}
      />
    </div>
  )
}
