import React, {useState} from 'react'
import {faPencil} from '@fortawesome/free-solid-svg-icons'

import Api, {Enrollee, Profile} from 'api/api'
import ParticipantNotesView from './ParticipantNotesView'
import {StudyEnvContextT} from '../../StudyEnvironmentRouter'
import {dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate} from '../../../util/timeUtils'
import {cloneDeep, isEmpty} from 'lodash'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import JustifyAndSaveModal from '../JustifyAndSaveModal'
import {findDifferencesBetweenObjects} from '../../../util/objectUtils'

/**
 * shows the enrollee profile and allows editing from the admin side
 */
export default function EnrolleeProfile({enrollee, studyEnvContext, onUpdate}:
                                          {
                                            enrollee: Enrollee,
                                            studyEnvContext: StudyEnvContextT,
                                            onUpdate: () => void
                                          }) {
  const [editMode, setEditMode] = useState<boolean>(false)
  const [hasBeenEdited, setHasBeenEdited] = useState<boolean>(false)
  const [editedProfile, setEditedProfile] = useState<Profile>(cloneDeep(enrollee.profile))
  const [showJustifyAndSaveModal, setShowJustifyAndSaveModal] = useState<boolean>(false)

  const saveProfile = async (justification: string) => {
    await Api.updateProfileForEnrollee(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode,
      {justification, profile: editedProfile})

    onUpdate()
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
        {editMode && <div className="flex-grow-1 d-flex justify-content-end">
            <button className="btn btn-primary mx-2 " disabled={!hasBeenEdited}
                    onClick={() => setShowJustifyAndSaveModal(true)}>
                Next: Add Justification
            </button>
            <button className="btn btn-outline-primary" onClick={() => setEditMode(false)}>Cancel</button>
        </div>}
      </div>
      <div className="card-body d-flex flex-row flex-wrap">
        {editMode
          ? <EditableProfile profile={editedProfile} setProfile={onProfileUpdate}/>
          : <ReadOnlyProfile profile={enrollee.profile}/>}
      </div>
    </div>

    {
      showJustifyAndSaveModal && (
        <JustifyAndSaveModal
          onDismiss={() => setShowJustifyAndSaveModal(false)}
          saveWithJustification={saveProfile}
          changes={findDifferencesBetweenObjects(enrollee.profile, editedProfile)}
        />
      )
    }

    <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
                          studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

/**
 *
 */
export function ReadOnlyProfile(
  {profile}: {
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
  {profile, setProfile}: {
    profile: Profile,
    setProfile: (value: React.SetStateAction<Profile>) => void
  }
) {
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

  const onMailingAddressFieldChange = (field: string, value: string) => {
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        mailingAddress: {
          ...oldVal.mailingAddress,
          [field]: value
        }
      }
    })
  }

  return <React.Fragment>
    <CardRow title={'Name'}>
      <div className='row'>
        <div className='col'>
          <input className="form-control" type="text" value={profile.givenName || ''}
                 placeholder={'Given Name'}
                 onChange={e => onStringFieldChange('givenName', e.target.value)}/>
        </div>
        <div className='col'>
          <input className="form-control" type="text" value={profile.familyName || ''}
                 placeholder={'Family Name'}
                 onChange={e => onStringFieldChange('familyName', e.target.value)}/>
        </div>
      </div>
    </CardRow>
    <CardRow title={'Birthdate'}>
      <div className='row'>
        <div className="col">
          <input className="form-control" type="date"
                 value={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0] || ''}
                 placeholder={'Birth Date'}
                 onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>
        </div>
        <div className="col"/>
      </div>
      {/*onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>*/}
    </CardRow>
    <CardRow title={'Primary Address'}>
      <div className="">
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.street1 || ''}
                   placeholder={'Street 1'}
                   onChange={e => onMailingAddressFieldChange('street1', e.target.value)}/>
          </div>
        </div>
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.street2 || ''}
                   placeholder={'Street 2'}
                   onChange={e => onMailingAddressFieldChange('street2', e.target.value)}/>
          </div>
        </div>
        <div className='row mb-2'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.city || ''}
                   placeholder={'City'}
                   onChange={e => onMailingAddressFieldChange('city', e.target.value)}/>
          </div>
          <div className='col'>
            <input className="form-control" type="text" value={profile.mailingAddress.state || ''}
                   placeholder={'State'}
                   onChange={e => onMailingAddressFieldChange('state', e.target.value)}/>
          </div>
        </div>
        <div className='row'>
          <div className="col">
            <input className="form-control" type="text" value={profile.mailingAddress.postalCode || ''}
                   placeholder={'Postal Code'}
                   onChange={e => onMailingAddressFieldChange('postalCode', e.target.value)}/>
          </div>
          <div className='col'>
            <input className="form-control" type="text" value={profile.mailingAddress.country || ''}
                   placeholder={'Country'}
                   onChange={e => onMailingAddressFieldChange('country', e.target.value)}/>
          </div>
        </div>
      </div>
    </CardRow>
    <CardRow title={'Email'}>
      <input className="form-control" type="text" value={profile.contactEmail || ''}
             placeholder={'Contact Email'}
             onChange={e => onStringFieldChange('contactEmail', e.target.value)}/>
    </CardRow>
    <CardRow title={'Phone'}>
      <input className="form-control" type="text" value={profile.phoneNumber || ''}
             placeholder={'Phone Number'}
             onChange={e => onStringFieldChange('phoneNumber', e.target.value)}/>
    </CardRow>
    <CardRow title={'Notifications'}>
      <div className='row mt-2'>
        <div className="col-auto">
          <div className="form-check">

            <input className="form-check-input" type="checkbox" checked={profile.doNotEmail} id="doNotEmailCheckbox"
                   onChange={e => onBooleanFieldChange('doNotEmail', e.target.checked)}/>
            <label className="form-check-label" htmlFor="doNotEmailCheckbox">
              Do Not Email
            </label>
          </div>
        </div>
        <div className='col-auto'>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" checked={profile.doNotEmailSolicit}
                   id="doNotSolicitCheckbox"
                   onChange={e => onBooleanFieldChange('doNotEmailSolicit', e.target.checked)}/>
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
  {title, values}: {
    title: string,
    values: string[]
  }
) {
  return <CardRow title={title}>
    {(isEmpty(values) || values.every(isEmpty)) && <p className="fst-italic mb-0 mt-2 text-muted">None provided</p>}
    {
      values.filter(val => !isEmpty(val)).map((val, idx) => (
        <p key={idx} className={`mb-0 ${idx == 0 ? 'mt-2' : ''}`}>{val}</p>
      ))
    }
  </CardRow>
}

/**
 *
 */
export function CardRow(
  {title, children}: {
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

