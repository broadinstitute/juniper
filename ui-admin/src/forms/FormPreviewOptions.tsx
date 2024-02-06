import React from 'react'

type FormPreviewOptions = {
  ignoreValidation: boolean
  showInvisibleElements: boolean
  locale: string
}

type FormPreviewOptionsProps = {
  value: FormPreviewOptions
  onChange: (newValue: FormPreviewOptions) => void
}

/** Controls for configuring the form editor's preview tab. */
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
      <div className="form-check">
        <label className="form-check-label" htmlFor="form-preview-show-invisible-elements">
          <input
            checked={value.showInvisibleElements}
            className="form-check-input"
            id="form-preview-show-invisible-elements"
            type="checkbox"
            onChange={e => {
              onChange({ ...value, showInvisibleElements: e.target.checked })
            }}
          />
          Show invisible questions
        </label>
      </div>
      <p className="form-text">
        Show all questions, regardless of their visibility. Use this to review questions that
        would be hidden by survey branching logic.
      </p>
      <div className="form-group">
        <label htmlFor="form-preview-locale">Locale</label>
        <input
          className="form-control"
          id="form-preview-locale"
          value={value.locale}
          onChange={e => {
            onChange({ ...value, locale: e.target.value })
          }}
        />
      </div>
      <p className="form-text">
        The locale code to use when rendering the form. If the locale is not supported, the
        form will default to English.
      </p>
    </div>
  )
}
