import React from 'react'

import { FormContentPage } from '@juniper/ui-core'

import { ElementList } from './ElementList'

export type PageDesignerProps = {
  readOnly: boolean
  value: FormContentPage
  onChange: (newValue: FormContentPage) => void
}

/** UI for editing a page of a form. */
export const PageDesigner = (props: PageDesignerProps) => {
  const { readOnly, value, onChange } = props

  return (
    <div>
      <h2>Page</h2>
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
