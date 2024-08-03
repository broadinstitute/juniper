import classNames from 'classnames'
import React, { useCallback, useState } from 'react'

import { FormContent } from '@juniper/ui-core'

import { validateFormContent, validateFormJson } from './formContentValidation'
import { OnChangeFormContent } from './formEditorTypes'
import { isEmpty } from 'lodash'

type FormContentJsonEditorProps = {
  initialValue: FormContent
  readOnly?: boolean
  onChange: OnChangeFormContent
}

export const FormContentJsonEditor = (props: FormContentJsonEditorProps) => {
  const { initialValue, readOnly = false, onChange } = props
  const [editorValue, _setEditorValue] = useState(() => JSON.stringify(initialValue, null, 2))
  const [validationErrors, setValidationErrors] = useState<string[]>([])
  const setEditorValue = useCallback((newEditorValue: string) => {
    _setEditorValue(newEditorValue)
    try {
      const validJsonFormContent = validateFormJson(newEditorValue)
      const validationErrors = validateFormContent(validJsonFormContent)
      setValidationErrors(validationErrors)
      onChange(validationErrors, validJsonFormContent)
      //@ts-ignore
    } catch (e: Error) {
      setValidationErrors([e.message])
      onChange([e.message], undefined)
    }
  }, [])

  return (
    <div className="d-flex flex-column flex-grow-1">
      <textarea
        className={classNames('w-100 flex-grow-1 form-control font-monospace',
          { 'is-invalid': !isEmpty(validationErrors) })}
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
