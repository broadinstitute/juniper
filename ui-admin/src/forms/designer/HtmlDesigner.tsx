import React from 'react'

import { HtmlElement } from '@juniper/ui-core'

import { Textarea } from 'components/forms/Textarea'

export type HtmlDesignerProps = {
  element: HtmlElement
  readOnly: boolean
  onChange: (newValue: HtmlElement) => void
}

/** UI for editing an HTML element in a form. */
export const HtmlDesigner = (props: HtmlDesignerProps) => {
  const { element, readOnly, onChange } = props

  return (
    <div className="d-flex flex-column h-100">
      <h2>{element.name}</h2>
      <label className="form-label fs-4 mb-0" htmlFor="html-element-html">HTML</label>
      <Textarea
        className="w-100 flex-grow-1 font-monospace"
        disabled={readOnly}
        id="html-element-html"
        style={{
          overflowX: 'auto',
          resize: 'none',
          // @ts-ignore TS thinks this isn't a valid style property
          textWrap: 'nowrap'
        }}
        value={element.html}
        onChange={newHtml => {
          onChange({
            ...element,
            html: newHtml
          })
        }}
      />
    </div>
  )
}
