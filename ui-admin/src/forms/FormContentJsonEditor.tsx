import React, {
  useCallback,
  useState
} from 'react'

import { FormContent } from '@juniper/ui-core'

import {
  validateFormContent,
  validateFormJson
} from './formContentValidation'
import { OnChangeFormContent } from './formEditorTypes'
import classNames from 'classnames'
import { isEmpty } from 'lodash'
import { LazyJsonEditor } from 'util/json/LazyJsonEditor'

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
    <LazyJsonEditor
      value={editorValue}
      className={classNames('p-1', { 'is-invalid': !isEmpty(validationErrors) })}
      readOnly={readOnly}
      width={'80vw'}
      setValue={setEditorValue}
      height={'75vh'}/>
  )
}
