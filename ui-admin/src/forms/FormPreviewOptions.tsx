import React from 'react'

type FormPreviewOptions = {
  ignoreValidation: boolean
}

type FormPreviewOptionsProps = {
  value: FormPreviewOptions
  onChange: (newValue: FormPreviewOptions) => void
}

export const FormPreviewOptions = (props: FormPreviewOptionsProps) => {
  const { value, onChange } = props
  return (
    <div>
      <div className="form-check">
        <label className="form-check-label" htmlFor="form-preview-ignore-validation">
          <input
            checked={value.ignoreValidation}
            className="form-check-input"
            id="form-preview-ignore-validation"
            type="checkbox"
            onChange={e => {
              onChange({ ...value, ignoreValidation: e.target.checked })
            }}
          />
          Ignore validation
        </label>
      </div>
      <p className="form-text">
        Ignore validation to preview all pages of a form without enforcing required fields.
        Enable validation to simulate a participant&apos;s experience.
      </p>
    </div>
  )
}
