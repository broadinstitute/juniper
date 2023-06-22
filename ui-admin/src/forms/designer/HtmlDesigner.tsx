import React from 'react'

import { HtmlElement } from '@juniper/ui-core'

export type HtmlDesignerProps = {
  readOnly: boolean
  value: HtmlElement
  onChange: (newValue: HtmlElement) => void
}

/** UI for editing an HTML element in a form. */
export const HtmlDesigner = (props: HtmlDesignerProps) => {
  const { readOnly, value, onChange } = props

  return (
    <div className="d-flex flex-column h-100">
      <h2>{value.name}</h2>
      <textarea
        className="w-100 flex-grow-1 font-monospace"
        readOnly={readOnly}
        style={{
          overflowX: 'auto',
          resize: 'none',
          // @ts-ignore TS thinks this isn't a valid style property
          textWrap: 'nowrap'
        }}
        value={value.html}
        onChange={e => {
          onChange({
            ...value,
            html: e.target.value
          })
        }}
      />
    </div>
  )
}
