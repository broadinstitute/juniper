import React, { useState } from 'react'
import { faPencil } from '@fortawesome/free-solid-svg-icons'

import Api, { AddressValidationResult, Enrollee, MailingAddress, Profile } from 'api/api'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate } from 'util/timeUtils'
import { cloneDeep, isEmpty } from 'lodash'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import JustifyChangesModal from '../JustifyChangesModal'
import { findDifferencesBetweenObjects } from 'util/objectUtils'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from '../../../util/LoadingSpinner'
import SuggestBetterAddressModal from '../../../address/SuggestBetterAddressModal'
import { explainAddressValidationResults, isAddressFieldValid } from '@juniper/ui-core'

/**
 * Shows the enrollee profile and allows editing from the admin side
 */
export default function EnrolleeProfile({ enrollee, studyEnvContext, onUpdate }:
                                          {
                                            enrollee: Enrollee,
                                            studyEnvContext: StudyEnvContextT,
                                            onUpdate: () => void
                                          }) {
  const [editMode, setEditMode] = useState<boolean>(false)
  const [isDirty, setIsDirty] = useState<boolean>(false)
  const [editedProfile, setEditedProfile] = useState<Profile>(cloneDeep(enrollee.profile))
  const [showJustifyAndSaveModal, setShowJustifyAndSaveModal] = useState<boolean>(false)

  const saveProfile = async (justification: string) => {
    await doApiLoad(async () => {
      await Api.updateProfileForEnrollee(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        { justification, profile: editedProfile })

      Store.addNotification(successNotification('Successfully updated enrollee profile.'))

      onUpdate()
    }, {})
  }

  const onProfileUpdate = (profileUpdate: React.SetStateAction<Profile>) => {
    setEditedProfile(profileUpdate)
    if (!isDirty) {
      setIsDirty(true)
    }
  }

  return <div>
    <div className="card w-75 border shadow-sm mb-3">
      <div className="card-header border-bottom bg-white d-flex flex-row align-items-center">
        <div className="fw-bold lead">{editMode ? 'Edit Profile' : 'Profile'}</div>
        {!editMode && <button className="mx-2 btn btn-light text-primary" onClick={() => setEditMode(true)}>
          <FontAwesomeIcon icon={faPencil} className={'me-2'}/>
            Edit
        </button>}
        {editMode && <div className="flex-grow-1 d-flex justify-content-end">
          <button className="btn btn-primary mx-2 " disabled={!isDirty}
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
        <JustifyChangesModal
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
 * Read only view of the profile.
 */
function ReadOnlyProfile(
  { profile }: {
    profile: Profile
  }
) {
  const mailingAddress = profile.mailingAddress
  return <>
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
  </>
}

/**
 * Editable profile, providing the update to `setProfile` on each change.
 */
function EditableProfile(
  { profile, setProfile }: {
    profile: Profile,
    setProfile: (value: React.SetStateAction<Profile>) => void
  }
) {
  const onFieldChange = (field: string, value: string | boolean) => {
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

  return <>
    <FormRow title={'Name'}>
      <div className='row'>
        <div className='col'>
          <input className="form-control" type="text" value={profile.givenName || ''}
            placeholder={'Given Name'}
            onChange={e => onFieldChange('givenName', e.target.value)}/>
        </div>
        <div className='col'>
          <input className="form-control" type="text" value={profile.familyName || ''}
            placeholder={'Family Name'}
            onChange={e => onFieldChange('familyName', e.target.value)}/>
        </div>
      </div>
    </FormRow>
    <FormRow title={'Birthdate'}>
      <div className='row'>
        <div className="col">
          <input className="form-control" type="date"
            defaultValue={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0] || ''}
            placeholder={'Birth Date'} max={'9999-12-31'}
            onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>
        </div>
        <div className="col"/>
      </div>
    </FormRow>
    <EditMailingAddressRow
      title={'Primary Address'} mailingAddress={profile.mailingAddress}
      onMailingAddressFieldChange={onMailingAddressFieldChange}
      setMailingAddress={mailingAddr => setProfile(profile => {
        return { ...profile, mailingAddress: mailingAddr }
      })}
    />
    <FormRow title={'Email'}>
      <input className="form-control" type="text" value={profile.contactEmail || ''}
        placeholder={'Contact Email'}
        onChange={e => onFieldChange('contactEmail', e.target.value)}/>
    </FormRow>
    <FormRow title={'Phone'}>
      <input className="form-control" type="text" value={profile.phoneNumber || ''}
        placeholder={'Phone Number'}
        onChange={e => onFieldChange('phoneNumber', e.target.value)}/>
    </FormRow>
    <FormRow title={'Notifications'}>
      <div className='row mt-2'>
        <div className="col-auto">
          <div className="form-check">

            <input className="form-check-input" type="checkbox" checked={profile.doNotEmail} id="doNotEmailCheckbox"
              onChange={e => onFieldChange('doNotEmail', e.target.checked)}/>
            <label className="form-check-label" htmlFor="doNotEmailCheckbox">
              Do Not Email
            </label>
          </div>
        </div>
        <div className='col-auto'>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" checked={profile.doNotEmailSolicit}
              id="doNotSolicitCheckbox"
              onChange={e => onFieldChange('doNotEmailSolicit', e.target.checked)}/>
            <label className="form-check-label" htmlFor="doNotSolicitCheckbox">
              Do Not Solicit
            </label>
          </div>
        </div>
      </div>
    </FormRow>
  </>
}

/**
 * Row of readonly data, where the title takes the leftmost portion and the values are on the rightmost.
 */
function ReadOnlyRow(
  { title, values }: {
    title: string,
    values: string[]
  }
) {
  return <FormRow title={title}>
    {(isEmpty(values) || values.every(isEmpty)) && <p className="fst-italic mb-0 mt-2 text-muted">None provided</p>}
    {
      values.filter(val => !isEmpty(val)).map((val, idx) => (
        <p key={idx} className={`mb-0 ${idx == 0 ? 'mt-2' : ''}`}>{val}</p>
      ))
    }
  </FormRow>
}

/**
 * One row of the profile's data.
 */
function FormRow(
  { title, children }: {
    title: string,
    children: React.ReactNode
  }
) {
  return <>
    <div className="w-25 fw-bold mb-4 mt-2">
      {title}
    </div>
    <div className="w-75 mb-4">
      {children}
    </div>
  </>
}


function EditMailingAddressRow(
  {
    title, mailingAddress, onMailingAddressFieldChange, setMailingAddress
  }: {
    title: string,
    mailingAddress: MailingAddress,
    onMailingAddressFieldChange: (field: string, value: string) => void,
    setMailingAddress: (update: MailingAddress) => void
  }
) {
  const [addressValidationResults, setAddressValidationResults] = useState<AddressValidationResult | undefined>()
  const [isLoadingValidation, setIsLoadingValidation] = useState<boolean>(false)
  const [hasChangedSinceValidation, setHasChangedSinceValidation] = useState<string[]>([])

  const onFieldChange = (field: string, value: string) => {
    if (!hasChangedSinceValidation.includes(field)) {
      setHasChangedSinceValidation(val => val.concat([field]))
    }

    onMailingAddressFieldChange(field, value)
  }

  const validateAddress = () => {
    doApiLoad(async () => {
      const results = await Api.validateAddress(mailingAddress, addressValidationResults?.sessionId)
      setAddressValidationResults(results)
      setHasChangedSinceValidation([])
    }, { setIsLoading: setIsLoadingValidation })
  }

  const formatClassName = (field: keyof MailingAddress) => {
    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(addressValidationResults, field, mailingAddress[field])) {
      return 'form-control is-invalid'
    }

    return 'form-control'
  }


  // TODO: make missing components highlight only the appropriate fields
  // TODO: make invalid tokens highlight only the appropriate fields
  // could do the ^ with a utility helper - isAddressFieldInvalid(AddressValidationDto, field string, val string)
  // TODO: make a utility function which will create a human-readable message for why it didn't work
  return <FormRow title={title}>
    <div className="">
      <div className='row mb-2'>
        <div className="col">
          <input
            className={formatClassName('street1')}
            type="text" value={mailingAddress.street1 || ''} placeholder={'Street 1'}
            onChange={e => onFieldChange('street1', e.target.value)}/>
        </div>
      </div>
      <div className='row mb-2'>
        <div className="col">
          <input
            className={formatClassName('street2')}
            type="text" value={mailingAddress.street2 || ''}
            placeholder={'Street 2'}
            onChange={e => onFieldChange('street2', e.target.value)}/>
        </div>
      </div>
      <div className='row mb-2'>
        <div className="col">
          <input
            className={formatClassName('city')}
            type="text" value={mailingAddress.city || ''}
            placeholder={'City'}
            onChange={e => onFieldChange('city', e.target.value)}/>
        </div>
        <div className='col'>
          <input
            className={formatClassName('state')}
            type="text" value={mailingAddress.state || ''} placeholder={'State'}
            onChange={e => onFieldChange('state', e.target.value)}/>
        </div>
      </div>
      <div className='row'>
        <div className="col">
          <input
            className={formatClassName('postalCode')}
            type="text" value={mailingAddress.postalCode || ''} placeholder={'Postal Code'}
            onChange={e => onFieldChange('postalCode', e.target.value)}/>
        </div>
        <div className='col'>
          <input
            className={formatClassName('country')}
            type="text" value={mailingAddress.country || ''} placeholder={'Country'}
            onChange={e => onFieldChange('country', e.target.value)}/>
        </div>
      </div>
      <LoadingSpinner isLoading={isLoadingValidation}>
        <button className="btn btn-link" onClick={validateAddress}>Validate</button>
      </LoadingSpinner>
      {addressValidationResults?.suggestedAddress &&
          <SuggestBetterAddressModal
            inputtedAddress={mailingAddress}
            improvedAddress={addressValidationResults.suggestedAddress}
            accept={() => {
              if (addressValidationResults && addressValidationResults.suggestedAddress) {
                setMailingAddress({
                  ...mailingAddress,
                  ...addressValidationResults.suggestedAddress
                })
              }
              setAddressValidationResults(undefined)
            }}
            deny={() => {
              setAddressValidationResults(undefined)
              // do nothing on deny - keep the old address
            }}
            onDismiss={() => {
              setAddressValidationResults(undefined)
            }}
          />}
      {!addressValidationResults?.valid && explainAddressValidationResults(addressValidationResults)
        .map((explanation, idx) =>
          <p key={idx} className="text-danger-emphasis">{explanation}</p>
        )}
    </div>
  </FormRow>
}
