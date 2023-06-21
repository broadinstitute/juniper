import React from 'react'

import { FormPanel } from '@juniper/ui-core'

import { ElementList } from './ElementList'

export type PanelDesignerProps = {
  readOnly: boolean
  value: FormPanel
  onChange: (newValue: FormPanel) => void
}

/** UI for editing a panel in a form. */
export const PanelDesigner = (props: PanelDesignerProps) => {
  const { readOnly, value, onChange } = props

  return (
    <div>
      <h2>Panel</h2>
      <ElementList
        readOnly={readOnly}
        value={value.elements}
        onChange={newValue => {
          onChange({ ...value, elements: newValue })
        }}
      />
    </div>
  )
}
