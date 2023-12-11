import React from 'react'

import { FormElement, FormPanel } from '@juniper/ui-core'

import { PanelElementList } from './PanelElementList'

export type PanelDesignerProps = {
  readOnly: boolean
  value: FormPanel
  onChange: (newValue: FormPanel, removedElement?: FormElement) => void
    setSelectedElementPath: (path: string) => void
    selectedElementPath: string
}

/** UI for editing a panel in a form. */
export const PanelDesigner = (props: PanelDesignerProps) => {
  const { readOnly, value, onChange, setSelectedElementPath, selectedElementPath } = props

  return (
    <div>
      <h2>Panel</h2>
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
