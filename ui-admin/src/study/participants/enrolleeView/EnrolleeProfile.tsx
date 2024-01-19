import React, { useState } from 'react'
import { faPencil } from '@fortawesome/free-solid-svg-icons'

import Api, { Enrollee, MailingAddress, Profile } from 'api/api'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate } from '../../../util/timeUtils'
import { cloneDeep, isEmpty } from 'lodash'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/**
 * shows the enrollee profile and allows editing from the admin side
 */
export default function EnrolleeProfile({ enrollee, studyEnvContext, onUpdate }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [editMode, setEditMode] = useState<boolean>(false)
  const [hasBeenEdited, setHasBeenEdited] = useState<boolean>(false)
  const [editedProfile, setEditedProfile] = useState<Profile>(cloneDeep(enrollee.profile))

  const saveProfile = () => {
    console.log(editedProfile)
  }

  const onProfileUpdate = (profileUpdate: React.SetStateAction<Profile>) => {
    setEditedProfile(profileUpdate)
    if (!hasBeenEdited) {
      setHasBeenEdited(true)
    }
  }

  return <div>
    <div className="card w-75 border shadow-sm mb-3">
      <div className="card-header border-bottom bg-white d-flex flex-row align-items-center">
        <div className="fw-bold lead">Profile</div>
        {!editMode && <button className="mx-2 btn btn-light text-primary" onClick={() => setEditMode(true)}>
          <FontAwesomeIcon icon={faPencil} className={'me-2'}/>
            Edit
        </button>}
        {editMode && <React.Fragment>
          <button className="btn btn-primary mx-2" disabled={!hasBeenEdited} onClick={saveProfile}>Save</button>
          <button className="btn btn-outline-primary" onClick={() => setEditMode(false)}>Cancel</button>
        </React.Fragment>}
      </div>
      <div className="card-body d-flex flex-row flex-wrap">
        {editMode
          ? <EditableProfile profile={editedProfile} setProfile={onProfileUpdate}/>
          : <ReadOnlyProfile profile={enrollee.profile}/>}
      </div>
    </div>

    <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
      studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

/**
 *
 */
export function ReadOnlyProfile(
  { profile }: {
    profile: Profile
  }
) {
  const mailingAddress = profile.mailingAddress
  return <React.Fragment>
    <ReadOnlyRow title={'Name'} values={[`${profile.givenName} ${profile.familyName}`]}/>
    <ReadOnlyRow title={'Birthdate'} values={[dateToDefaultString(profile.birthDate)]}/>
    <ReadOnlyRow title={'Primary Address'} values={[
      mailingAddress.street1,
      mailingAddress.street2,
      `${mailingAddress.city}, ${mailingAddress.state} ${mailingAddress.postalCode}`
    ]}/>
    <ReadOnlyRow title={'Email'} values={[profile.contactEmail]}/>
    <ReadOnlyRow title={'Phone'} values={[profile.phoneNumber]}/>
    <ReadOnlyRow title={'Notifications'} values={[profile.doNotEmail ? 'On' : 'Off']}/>
    <ReadOnlyRow title={'Do Not Solicit'} values={[profile.doNotEmailSolicit ? 'On' : 'Off']}/>
  </React.Fragment>
}

/**
 *
 */
export function EditableProfile(
  { profile, setProfile }: {
    profile: Profile,
    setProfile: (value: React.SetStateAction<Profile>) => void
  }
) {
  return <React.Fragment>
    <CardRow title={'Name'}>
      <div className='row'>
        <div className='col'>
          <input className="form-control" type="text" value={profile.givenName}/>
        </div>
        <div className='col'>
          <input className="form-control" type="text" value={profile.familyName}/>
        </div>
      </div>
    </CardRow>
    <CardRow title={'Birthdate'}>
      <div className='row'>
        <div className="col">
          <input className="form-control" type="date"
            value={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0]}
          />
        </div>
        <div className="col"/>
      </div>
      {/*onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>*/}
    </CardRow>
    <CardRow title={'Primary Address'}>
      <div className="">
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.street1}/>
          </div>
        </div>
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.street2}/>
          </div>
        </div>
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.city}/>
          </div>
          <div className='col'>
            <input className="form-control" type="text" value={profile.mailingAddress.state}/>
          </div>
        </div>
        <div className='row'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.postalCode}/>
          </div>
          <div className='col'>
            <input className="form-control" type="text" value={profile.mailingAddress.country}/>
          </div>
        </div>
      </div>
    </CardRow>
    <CardRow title={'Email'}>
      <input className="form-control" type="text" value={profile.contactEmail}/>
    </CardRow>
    <CardRow title={'Phone'}>
      <input className="form-control" type="text" value={profile.phoneNumber}/>
    </CardRow>
    <CardRow title={'Notifications'}>
      <div className='row mt-2'>
        <div className="col-auto">
          <div className="form-check">

            <input className="form-check-input" type="checkbox" value="" id="doNotEmailCheckbox"/>
            <label className="form-check-label" htmlFor="doNotEmailCheckbox">
              Do Not Email
            </label>
          </div>
        </div>
        <div className='col-auto'>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" value="" id="doNotSolicitCheckbox"/>
            <label className="form-check-label" htmlFor="doNotSolicitCheckbox">
              Do Not Solicit
            </label>
          </div>
        </div>
      </div>
    </CardRow>
  </React.Fragment>
}

/**
 *
 */
export function ReadOnlyRow(
  { title, values }: {
    title: string,
    values: string[]
  }
) {
  return <CardRow title={title}>
    {(isEmpty(values) || values.every(isEmpty)) && <p className="fst-italic mb-0 mt-2 text-muted">None provided</p>}
    {
      values.filter(val => !isEmpty(val)).map((val, idx) => <p className={`mb-0 ${idx == 0 ? 'mt-2' : ''}`}>{val}</p>)
    }
  </CardRow>
}

/**
 *
 */
export function CardRow(
  { title, children }: {
    title: string,
    children: React.ReactNode
  }
) {
  return <React.Fragment>
    <div className="w-25 fw-bold mb-4 mt-2">
      {title}
    </div>
    <div className="w-75 mb-4">
      {children}
    </div>
  </React.Fragment>
}

/** displays the mailing list */
export function EditableMailingAddress(
  { mailingAddress, updateMailingAddress }: {
    mailingAddress: MailingAddress,
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
        Street 1: <input className="form-control" type="text" value={mailingAddress.street1}
          onChange={e => onStringFieldChange('street1', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        Street 2: <input className="form-control" type="text" value={mailingAddress.street2}
          onChange={e => onStringFieldChange('street2', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        City: <input className="form-control" type="text" value={mailingAddress.city}
          onChange={e => onStringFieldChange('city', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        State: <input className="form-control" type="text" value={mailingAddress.state}
          onChange={e => onStringFieldChange('state', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        Country: <input className="form-control" type="text" value={mailingAddress.country}
          onChange={e => onStringFieldChange('country', e.target.value)}/>
      </label>
      <label className="form-label ms-2">
        Postal code: <input className="form-control" type="text" value={mailingAddress.postalCode}
          onChange={e => onStringFieldChange('postalCode', e.target.value)}/>
      </label>
    </div>
  </div>
}


/**
 *
 */
export function EditableProfileOld({ enrollee, studyEnvContext }:
                                     { enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const [profile, setProfile] = useState<Profile>(enrollee.profile)

  const saveProfile = async () => {
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

  const onDateFieldChange = (field: string, date: Date | null) => {
    const asJavaLocalDate: number[] | undefined = date ? jsDateToJavaLocalDate(date) : undefined

    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: asJavaLocalDate
      }
    })
  }

  return <div>
    <form className="mb-3">
      <div>
        <label className="form-label">
          Given name: <input className="form-control" type="text"
            value={profile.givenName}
            onChange={e => onStringFieldChange('givenName', e.target.value)}/>
        </label>
        <label className="form-label ms-2">
          Family name: <input className="form-control" type="text"
            value={profile.familyName}
            onChange={e => onStringFieldChange('familyName', e.target.value)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Contact email:
          <input className="form-control" type="text" value={profile.contactEmail}
            onChange={e => onStringFieldChange('contactEmail', e.target.value)}/>
        </label>
        <label className="form-label ms-3">
          Do not email:
          <input type="checkbox" className="ms-1" checked={profile.doNotEmail}
            onChange={e => onBooleanFieldChange('doNotEmail', e.target.checked)}/>
        </label>
        <label className="form-label ms-3">
          Do not solicit:
          <input type="checkbox" className="ms-1" checked={profile.doNotEmailSolicit}
            onChange={e => onBooleanFieldChange('doNotEmailSolicit', e.target.checked)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Birthdate:
          <input className="form-control" type="date"
            value={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0]}
            onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Phone:
          <input className="form-control" type="text" value={profile.phoneNumber}
            onChange={e => onStringFieldChange('phoneNumber', e.target.value)}/>
        </label>
      </div>
      <h3 className="h6">Mailing address</h3>
      {profile.mailingAddress &&
          <EditableMailingAddress mailingAddress={profile.mailingAddress}
            updateMailingAddress={updateMailingAddress}/>}
      {!profile.mailingAddress && <span className="detail">none</span>}
      <button className="btn btn-primary" onClick={saveProfile}>Save</button>
    </form>
  </div>
}
