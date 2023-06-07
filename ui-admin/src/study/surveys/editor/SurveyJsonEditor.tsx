import React, { useCallback, useState } from 'react'

import { FormContent } from '@juniper/ui-core'

import { OnChangeSurvey } from './surveyEditorTypes'

type SurveyJsonEditorProps = {
  initialValue: FormContent
  readOnly?: boolean
  onChange: OnChangeSurvey
}

export const SurveyJsonEditor = (props: SurveyJsonEditorProps) => {
  const { initialValue, readOnly = false, onChange } = props
  const [editorValue, _setEditorValue] = useState(() => JSON.stringify(initialValue, null, 2))
  const [, setIsValid] = useState(true)
  const setEditorValue = useCallback((newEditorValue: string) => {
    _setEditorValue(newEditorValue)
    try {
      const survey = JSON.parse(newEditorValue)
      setIsValid(true)
      onChange(true, survey)
    } catch (e) {
      setIsValid(false)
      onChange(false, undefined)
    }
  }, [])

  return (
    <div className="d-flex flex-column flex-grow-1">
      <textarea
        className="w-100 flex-grow-1 font-monospace"
        readOnly={readOnly}
        style={{
          overflowX: 'auto',
          resize: 'none',
          // @ts-ignore TS thinks this isn't a valid style property
          textWrap: 'nowrap'
        }}
        value={editorValue}
        onChange={e => { setEditorValue(e.target.value) }}
      />
    </div>
  )
}
