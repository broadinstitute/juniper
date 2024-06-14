import classNames from 'classnames'
import React, { useId } from 'react'
import InfoPopup from './InfoPopup'

export type TextareaProps = Omit<JSX.IntrinsicElements['textarea'], 'onChange'> & {
  description?: string
  infoContent?: React.ReactNode
  label: string
  required?: boolean
  onChange?: (value: string) => void
}

/** A textarea with label and description. */
export const Textarea = (props: TextareaProps) => {
  const { description, label, required, infoContent, ...inputProps } = props
  const { className, disabled, value, id, onChange } = inputProps

  const generatedId = useId()
  const inputId = id || generatedId
  const descriptionId = `${generatedId}-help`

  return (
    <>
      <label className="form-label fw-semibold mb-0" htmlFor={inputId}>
        {label}
        {required && <span className="text-danger">*</span>}
      </label>
      {infoContent && <InfoPopup content={infoContent}/>}
      <textarea
        {...inputProps}
        aria-describedby={description ? descriptionId : undefined}
        aria-disabled={disabled}
        className={classNames('form-control', { disabled }, className, { 'is-invalid': required && value === '' })}
        disabled={undefined}
        id={inputId}
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
