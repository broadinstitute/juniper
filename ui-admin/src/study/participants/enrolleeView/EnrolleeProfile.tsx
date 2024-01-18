import React, { useEffect, useState } from 'react'
import Api, { Enrollee, MailingAddress, Profile } from 'api/api'
import { dateToUSLocaleString, usLocalStringToDate } from 'util/timeUtils'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { isNil } from 'lodash'

/**
 * shows the enrollee profile and allows editing from the admin side
 */
export default function EnrolleeProfile({ enrollee, studyEnvContext, onUpdate }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [profile, setProfile] = useState<Profile>(enrollee.profile)
  const [editMode, setEditMode] = useState<boolean>(false)

  const [birthDateStr, setBirthDateStr] = useState<string>(dateToUSLocaleString(profile.birthDate))
  const [birthDateStrValid, setBirthDateStrValid] = useState<boolean>(true)

  useEffect(() => {
    if (birthDateStr === '') {
      setProfile(prevProfile => {
        return {
          ...prevProfile,
          birthDate: undefined
        }
      })
      setBirthDateStrValid(true)
      return
    }

    const date: number[] | undefined = usLocalStringToDate(birthDateStr)

    if (isNil(date)) {
      setBirthDateStrValid(false)
    } else {
      setProfile(prevProfile => {
        return {
          ...prevProfile,
          birthDate: date
        }
      })
      setBirthDateStrValid(true)
    }
  }, [birthDateStr])

  const updateProfile = async () => {
    const updatedProfile = await Api.updateProfile(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode, profile)

    setProfile(updatedProfile)
  }

  const updateMailingAddress = (mailingAddress: MailingAddress) => {
    // use callback to avoid infinitely recursive updates
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        mailingAddress
      }
    })
  }

  const onStringFieldChange = (field: string, value: string) => {
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: value
      }
    })
  }

  const onBooleanFieldChange = (field: string, value: boolean) => {
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: value
      }
    })
  }

  return <div>
    <input type={'checkbox'}
      checked={editMode}
      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditMode(e.target.checked)}
    />
    <form className="mb-3">
      <div>
        <label className="form-label">
          Given name: <input className="form-control" type="text" disabled={!editMode}
            value={profile.givenName}
            onChange={e => onStringFieldChange('givenName', e.target.value)}/>
        </label>
        <label className="form-label ms-2">
          Family name: <input className="form-control" type="text" disabled={!editMode}
            value={profile.familyName}
            onChange={e => onStringFieldChange('familyName', e.target.value)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Contact email:
          <input className="form-control" type="text" disabled={!editMode} value={profile.contactEmail}
            onChange={e => onStringFieldChange('contactEmail', e.target.value)}/>
        </label>
        <label className="form-label ms-3">
          Do not email:
          <input type="checkbox" className="ms-1" disabled={!editMode} checked={profile.doNotEmail}
            onChange={e => onBooleanFieldChange('doNotEmail', e.target.checked)}/>
        </label>
        <label className="form-label ms-3">
          Do not solicit:
          <input type="checkbox" className="ms-1" disabled={!editMode} checked={profile.doNotEmailSolicit}
            onChange={e => onBooleanFieldChange('doNotEmailSolicit', e.target.checked)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Birthdate (MM/DD/YYYY):
          <input className={`form-control ${birthDateStrValid ? '' : 'is-invalid'}`} type="text"
            disabled={!editMode} value={birthDateStr} onChange={e => setBirthDateStr(e.target.value)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Phone:
          <input className="form-control" type="text" disabled={!editMode} value={profile.phoneNumber}
            onChange={e => onStringFieldChange('phoneNumber', e.target.value)}/>
        </label>
      </div>
      <h3 className="h6">Mailing address</h3>
      {profile.mailingAddress &&
          <MailingAddressView mailingAddress={profile.mailingAddress} editMode={editMode}
            updateMailingAddress={updateMailingAddress}/>}
      {!profile.mailingAddress && <span className="detail">none</span>}
      <button className="btn btn-primary" onClick={updateProfile}>Save</button>
    </form>
    <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
      studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

/** displays the mailing list -- does not yet support editing */
export function MailingAddressView(
  { mailingAddress, editMode, updateMailingAddress }: {
    mailingAddress: MailingAddress,
    editMode: boolean,
    updateMailingAddress: (val: MailingAddress) => void
  }
) {
  const onStringFieldChange = (field: string, value: string) => {
    updateMailingAddress({
      ...mailingAddress,
      [field]: value
    })
  }

  return <div>
    <div>
      <label className="form-label">
        Street 1: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.street1}
          onChange={e => onStringFieldChange('street1', e.target.value)}/>
      </label>
    </div><div>
      <label className="form-label">
        Street 2: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.street2}
          onChange={e => onStringFieldChange('street2', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        City: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.city}
          onChange={e => onStringFieldChange('city', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        State: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.state}
          onChange={e => onStringFieldChange('state', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        Country: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.country}
          onChange={e => onStringFieldChange('country', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        Postal code: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.postalCode}
          onChange={e => onStringFieldChange('postalCode', e.target.value)}/>
      </label>
    </div>
  </div>
}

