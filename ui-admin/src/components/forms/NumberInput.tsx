import React from 'react'

import { TextInput, TextInputProps } from './TextInput'

export type NumberInputProps = Omit<TextInputProps, 'value' | 'onChange'> & {
  value?: number
  onChange?: (newValue: number | undefined) => void
}

/**
 * A number input with label and description.
 */
export const NumberInput = (props: NumberInputProps) => {
  const { onChange } = props
  return (
    <TextInput
      {...props}
      type="number"
      onChange={newValue => {
        onChange?.(newValue === '' ? undefined : parseInt(newValue))
      }}
    />
  )
}
