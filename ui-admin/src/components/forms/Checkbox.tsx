import classNames from 'classnames'
import React, { useId } from 'react'
import InfoPopup from './InfoPopup'

export type CheckboxProps = Omit<JSX.IntrinsicElements['input'], 'onChange'> & {
  checked: boolean
  description?: string
  infoContent?: React.ReactNode
  label: string
  onChange?: (value: boolean) => void
}

/** A textarea with label and description. */
export const Checkbox = (props: CheckboxProps) => {
  const { description, label, infoContent, ...inputProps } = props
  const { checked, className, disabled, id, onChange } = inputProps

  const generatedId = useId()
  const inputId = id || generatedId
  const descriptionId = `${generatedId}-help`

  return (
    <>
      <div className="form-check">
        <input
          {...inputProps}
          type="checkbox"
          aria-describedby={description ? descriptionId : undefined}
          aria-disabled={disabled}
          checked={checked}
          className={classNames('form-check-input', { disabled }, className)}
          disabled={undefined}
          id={inputId}
          onChange={
            disabled
              // Noop because providing checked without an onChange handler causes a React warning
              ? () => { /* noop */ }
              : e => { onChange?.(e.target.checked) }
          }
        />
        <label className="form-check-label" htmlFor={inputId}>
          {label}
        </label>
        { infoContent && <InfoPopup content={infoContent}/> }
      </div>
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
