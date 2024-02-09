import React, { useState } from 'react'
import { PortalEnvironmentLanguage } from '@juniper/ui-core'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'

type FormPreviewOptions = {
  ignoreValidation: boolean
  showInvisibleElements: boolean
  locale: string
}

type FormPreviewOptionsProps = {
  value: FormPreviewOptions
  supportedLanguages: PortalEnvironmentLanguage[]
  onChange: (newValue: FormPreviewOptions) => void
}

/** Controls for configuring the form editor's preview tab. */
export const FormPreviewOptions = (props: FormPreviewOptionsProps) => {
  const { value, supportedLanguages, onChange } = props
  // TODO (JN-863): Use the default language
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(supportedLanguages.find(lang =>
    lang.languageCode === 'en'))

  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
    useReactSingleSelect(
      supportedLanguages,
      (language: PortalEnvironmentLanguage) => ({ label: language.languageName, value: language }),
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
      { languageOptions.length > 1 && <><div className="form-group">
        <label htmlFor={selectLanguageInputId}>Language</label>
        <Select
          inputId={selectLanguageInputId}
          options={languageOptions}
          value={selectedLanguageOption}
          onChange={language => {
            languageOnChange(language)
            onChange({ ...value, locale: language?.value.languageCode ?? 'default' })
          }}/>
      </div>
      <p className="form-text">
        The language to use when rendering the form. If the language is not supported for this form, the default
          language for the form will be used.
      </p></> }
    </div>
  )
}
