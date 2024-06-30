import React, { useState } from 'react'
import { PortalEnvironmentLanguage, Profile } from '@juniper/ui-core'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'
import { usePortalLanguage } from 'portal/languages/usePortalLanguage'
import InfoPopup from '../components/forms/InfoPopup'
import { TextInput } from '../components/forms/TextInput'

type FormPreviewOptions = {
  ignoreValidation: boolean
  showInvisibleElements: boolean
  locale: string
  profile?: Profile
  proxyProfile?: Profile
}

type FormPreviewOptionsProps = {
  value: FormPreviewOptions
  supportedLanguages: PortalEnvironmentLanguage[]
  onChange: (newValue: FormPreviewOptions) => void
}

/** Controls for configuring the form editor's preview tab. */
export const FormPreviewOptions = (props: FormPreviewOptionsProps) => {
  const { value, supportedLanguages, onChange } = props
  const { defaultLanguage } = usePortalLanguage()
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(defaultLanguage)

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
      <h3 className="h6">Preview options</h3>
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
        <InfoPopup content={<p className="form-text">
          Ignore validation to preview all pages of a form without enforcing required fields.
          Enable validation to simulate a participant&apos;s experience.
        </p>}/>
      </div>
      <div className="form-check mt-3">
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
        <InfoPopup content={<p className="form-text">
          Show all questions, regardless of their visibility. Use this to review questions that
          would be hidden by survey branching logic.
        </p>}/>
      </div>
      <div className="form-group mt-3">
        <label htmlFor={selectLanguageInputId}>Language Preview</label>
        <InfoPopup content={<p><p>
          The language to use when rendering the form. If the language is not supported for this form, the default
          language for the form will be used. </p>
        <p>The values for this dropdown are taken from the supported languages
          configurable in &quot;Site Settings&quot;.
        </p></p>}/>
        <Select
          inputId={selectLanguageInputId}
          options={languageOptions}
          value={selectedLanguageOption}
          onChange={language => {
            languageOnChange(language)
            onChange({ ...value, locale: language?.value.languageCode ?? 'default' })
          }}/>
      </div>
      <div className="mt-4">
        <div data-testid="profileInfoFields">
          <h4 className="h6">
          Participant profile <InfoPopup content={<p>
          Change the values below to test how your survey appears to different participants
          due to branching logic or dynamic texts.  The participant profile is the profile of the
          user taking the survey.
            </p>}/>
          </h4>
          <TextInput onChange={text => onChange({
            ...value,
            profile: {
              ...value.profile,
              givenName: text
            }
          })}
          label={'Given name'}
          unboldLabel={true}
          value={value.profile?.givenName ?? ''} />
          <TextInput onChange={text => onChange({
            ...value,
            profile: {
              ...value.profile,
              familyName: text
            }
          })}
          label={'Family name'}
          unboldLabel={true}
          value={value.profile?.familyName ?? ''} />
        </div>
        <div data-testid="proxyInfoFields">
          <h4 className="h6 mt-3">
            Proxy profile <InfoPopup content={<p>
            Change the values below to test how your survey appears to different participants
            due to branching logic or dynamic texts.  The proxy profile is the profile of the
            person the user is taking the survey on behalf of.
            </p>}/>
          </h4>
          <TextInput onChange={text => onChange({
            ...value,
            proxyProfile: {
              ...value.proxyProfile,
              givenName: text
            }
          })}
          label={'Given name'}
          data-testid="proxyGivenName"
          unboldLabel={true}
          value={value.proxyProfile?.givenName ?? ''} />
          <TextInput onChange={text => onChange({
            ...value,
            proxyProfile: {
              ...value.proxyProfile,
              familyName: text
            }
          })}
          label={'Family name'}
          unboldLabel={true}
          value={value.proxyProfile?.familyName ?? ''} />
        </div>
      </div>

    </div>
  )
}
