import React from 'react'
import { Profile } from '@juniper/ui-core'
import InfoPopup from '../components/forms/InfoPopup'
import { TextInput } from '../components/forms/TextInput'

type FormPreviewOptions = {
  ignoreValidation: boolean
  showInvisibleElements: boolean
  locale: string
  profile?: Profile
  proxyProfile?: Profile
  isGovernedUser: boolean
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
      <div className="mt-4">
        <div data-testid="profileInfoFields">
          <h4 className="h6">
            Participant profile <InfoPopup content={<p>
            Change the values below to test how your survey appears to different participants
            due to branching logic or dynamic texts. The participant profile is the profile of the
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
          value={value.profile?.givenName ?? ''}/>
          <TextInput onChange={text => onChange({
            ...value,
            profile: {
              ...value.profile,
              familyName: text
            }
          })}
          label={'Family name'}
          unboldLabel={true}
          value={value.profile?.familyName ?? ''}/>
        </div>
        <div className="form-check mt-2">
          <label className="form-check-label" htmlFor="form-is-governed-user">
            <input
              checked={value.isGovernedUser}
              className="form-check-input"
              id="form-is-governed-user"
              type="checkbox"
              onChange={e => {
                onChange({ ...value, isGovernedUser: e.target.checked })
              }}
            />
            Is governed user
          </label>
        </div>
      </div>
      <div>
        <div data-testid="proxyInfoFields">
          <h4 className="h6 mt-3">
            Proxy profile <InfoPopup content={<p>
            Change the values below to test how your survey appears to different participants
            due to branching logic or dynamic texts. The proxy profile is the profile of the
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
