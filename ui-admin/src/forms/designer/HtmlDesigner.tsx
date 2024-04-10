import React from 'react'

import { HtmlElement } from '@juniper/ui-core'

import { Textarea } from 'components/forms/Textarea'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { getI18nSurveyElement } from 'util/juniperSurveyUtils'

export type HtmlDesignerProps = {
  element: HtmlElement
  readOnly: boolean
  onChange: (newValue: HtmlElement) => void
  addNextQuestion: () => void
}

/** UI for editing an HTML element in a form. */
export const HtmlDesigner = (props: HtmlDesignerProps) => {
  const { element, readOnly, onChange, addNextQuestion } = props

  return (
    <div className="d-flex flex-column h-100">
      <div className="d-flex align-items-center justify-content-between">
        <h2>{element.name} (html)</h2>
        {addNextQuestion && <div>
          <Button variant="secondary" className="ms-auto" onClick={addNextQuestion}>
            <FontAwesomeIcon icon={faPlus}/> Add next question
          </Button>
        </div>}
      </div>
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
        value={getI18nSurveyElement(element.html)}
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
