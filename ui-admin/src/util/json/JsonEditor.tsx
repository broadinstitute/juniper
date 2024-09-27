import React from 'react'
import CodeMirror from '@uiw/react-codemirror'
import { json } from '@codemirror/lang-json'
import { okaidia } from '@uiw/codemirror-theme-okaidia'

export const JsonEditor = (
  {
    value,
    setValue,
    className,
    height,
    width,
    readOnly = false
  }:
    {
      value: string,
      setValue: (newJson: string) => void,
      className?: string,
      height?: string,
      width?: string,
      readOnly?: boolean
    }) => {
  return <CodeMirror
    value={value}
    className={className}
    extensions={[json()]}
    theme={okaidia}
    height={height}
    width={width}
    readOnly={readOnly}
    onChange={value => {
      setValue(value)
    }}
  />
}

export default JsonEditor

