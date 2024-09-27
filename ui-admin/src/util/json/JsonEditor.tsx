import React from 'react'
import CodeMirror from '@uiw/react-codemirror'
import { json } from '@codemirror/lang-json'
import { githubLight } from '@uiw/codemirror-theme-github'
import './JsonEditor.css'

export type JsonEditorProps = {
  value: string,
  setValue: (newJson: string) => void,
  className?: string,
  height?: string,
  width?: string,
  readOnly?: boolean
}

export const JsonEditor = (
  {
    value,
    setValue,
    className,
    height,
    width,
    readOnly = false
  }: JsonEditorProps
) => {
  return <CodeMirror
    value={value}
    className={className}
    extensions={[json()]}
    theme={githubLight}
    height={height}
    width={width}
    readOnly={readOnly}
    onChange={value => {
      setValue(value)
    }}
  />
}

export default JsonEditor

