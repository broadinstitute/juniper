import classNames from 'classnames'
import React, { useId } from 'react'

export type TextInputProps = Omit<JSX.IntrinsicElements['input'], 'onChange'> & {
  description?: string
  label: string
  labelClassname?: string,
  onChange?: (value: string) => void
}

/** A text input with label and description. */
export const TextInput = (props: TextInputProps) => {
  const { description, label, labelClassname, ...inputProps } = props
  const { className, disabled, id, onChange } = inputProps

  const generatedId = useId()
  const inputId = id || generatedId
  const descriptionId = `${generatedId}-help`

  return (
    <>
      <label
        className={classNames('form-label', labelClassname)}
        htmlFor={inputId}
      >
        {label}
      </label>
      <input
        {...inputProps}
        aria-describedby={description ? descriptionId : undefined}
        aria-disabled={disabled}
        className={classNames('form-control', { disabled }, className)}
        disabled={undefined}
        id={inputId}
        type="text"
        onChange={
          disabled
            // Noop because providing a value without an onChange handler causes a React warning
            ? () => { /* noop */ }
            : e => { onChange?.(e.target.value) }
        }
      />
      {description && (
        <p
          className="form-text"
          id={descriptionId}
        >
          {description}
        </p>
      )}
    </>
  )
}
