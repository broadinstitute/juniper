import React, { useEffect, useState } from 'react'
import { PortalLanguage } from '@juniper/ui-core'
import Select from 'react-select'
import useReactSingleSelect from '../util/react-select-utils'

type FormPreviewOptions = {
  ignoreValidation: boolean
  showInvisibleElements: boolean
  locale: string
}

type FormPreviewOptionsProps = {
  value: FormPreviewOptions
  supportedLanguages: PortalLanguage[]
  onChange: (newValue: FormPreviewOptions) => void
}

/** Controls for configuring the form editor's preview tab. */
export const FormPreviewOptions = (props: FormPreviewOptionsProps) => {
  const { value, supportedLanguages, onChange } = props
  const [selectedLanguage, setSelectedLanguage] = useState<PortalLanguage>()

  useEffect(() => {
    onChange({ ...value, locale: selectedLanguage?.languageCode ?? 'default' })
  }, [selectedLanguage])

  const {
    onChange: localeOnChange, options: localeOptions,
    selectedOption: selectedLocaleOption, selectInputId: selectLocaleInputId
  } =
    useReactSingleSelect(
      supportedLanguages,
      (language: PortalLanguage) => ({ label: language.languageName, value: language }),
      setSelectedLanguage,
      selectedLanguage
    )

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
        <label htmlFor={selectLocaleInputId}>Language</label>
        <Select
          inputId={selectLocaleInputId}
          options={localeOptions}
          value={selectedLocaleOption}
          onChange={localeOnChange}/>
      </div>
      <p className="form-text">
        The language to use when rendering the form. If the language is not supported for this form, the default
          language for the form will be used.
      </p>
    </div>
  )
}
