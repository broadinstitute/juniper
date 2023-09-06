import classNames from 'classnames'
import React, { useId } from 'react'
import InfoPopup from './InfoPopup'

export type TextInputProps = Omit<JSX.IntrinsicElements['input'], 'onChange'> & {
  infoContent?: React.ReactNode,
  description?: string
  label: string
  labelClassname?: string,
  onChange?: (value: string) => void
}

/** A text input with label and description. */
export const TextInput = (props: TextInputProps) => {
  const { infoContent, description, label, labelClassname, ...inputProps } = props
  const { className, disabled, id, value, onChange } = inputProps

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
      {infoContent &&
        //@ts-ignore
        <InfoPopup content={infoContent}/>}
      <input
        type="text"
        {...inputProps}
        aria-describedby={description ? descriptionId : undefined}
        aria-disabled={disabled}
        className={classNames('form-control', { disabled }, className)}
        disabled={undefined}
        id={inputId}
        // Allow value to be undefined without triggering a React warning about uncontrolled input.
        value={value ?? ''}
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
