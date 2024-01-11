import React, { useEffect, useState } from 'react'
import { Enrollee, MailingAddress, Profile } from 'api/api'
import { javaDateToIsoString } from 'util/timeUtils'
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

  const [birthDateStr, setBirthDateStr] = useState<string>(javaDateToIsoString(profile.birthDate))

  const [validBirthDate, setValidBirthDate] = useState<boolean>(true)

  useEffect(() => {
    if (birthDateStr.length === 0) {
      setProfile(profile => {
        return {
          ...profile,
          birthDate: undefined
        }
      })
      setValidBirthDate(true)
      return
    }

    const birthDate = defaultStringToDate(birthDateStr)

    if (!isNil(birthDate)) {
      setValidBirthDate(true)
      setProfile(profile => {
        return {
          ...profile,
          birthDate
        }
      })
      //setBirthDateStr(dateToDefaultString(birthDate))
    } else {
      setValidBirthDate(false)
      console.log('hi?')
    }
  }, [birthDateStr])

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
          Birthdate:
          <input className={`form-control ${validBirthDate ? '' : 'is-invalid'}`} type="text"
            disabled={!editMode} value={birthDateStr}
            onChange={e => setBirthDateStr(e.target.value)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Phone:
          <input className="form-control" type="text" disabled={!editMode} value={profile.phoneNumber}/>
        </label>
      </div>
      <h3 className="h6">Mailing address</h3>
      {profile.mailingAddress &&
          <MailingAddressView mailingAddress={profile.mailingAddress} editMode={editMode}
            updateMailingAddress={updateMailingAddress}/>}
      {!profile.mailingAddress && <span className="detail">none</span>}
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
  const onFieldChange = (field: string, value: string) => {
    updateMailingAddress({
      ...mailingAddress,
      [field]: value
    })
  }

  return <div>
    <div>
      <label className="form-label">
        Street 1: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.street1}
          onChange={e => onFieldChange('street1', e.target.value)}/>
      </label>
    </div><div>
      <label className="form-label">
        Street 2: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.street2}
          onChange={e => onFieldChange('street2', e.target.value)}/>
      </label>
    </div><div>
      <label className="form-label">
        City: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.city}
          onChange={e => onFieldChange('city', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        State: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.state}
          onChange={e => onFieldChange('state', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        Country: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.country}
          onChange={e => onFieldChange('country', e.target.value)}/>
      </label>
      <label className="form-label">
        Postal code: <input className="form-control" type="text" disabled={!editMode} value={mailingAddress.postalCode}
          onChange={e => onFieldChange('postalCode', e.target.value)}/>
      </label>
    </div>
  </div>
}

