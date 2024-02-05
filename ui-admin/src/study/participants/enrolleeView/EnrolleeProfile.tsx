import React, { useState } from 'react'
import { faPencil } from '@fortawesome/free-solid-svg-icons'

import Api, { AddressValidationResult, Enrollee, MailingAddress, Profile } from 'api/api'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate } from 'util/timeUtils'
import { cloneDeep, isEmpty, isNil } from 'lodash'
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
    <ReadOnlyRow title={'Name'}
      values={[
                   `${profile.givenName || ''} ${profile.familyName || ''}`.trim()
      ]}/>
    <ReadOnlyRow title={'Birthdate'} values={[dateToDefaultString(profile.birthDate)]}/>
    <ReadOnlyMailingAddress title={'Primary Address'} mailingAddress={mailingAddress}/>
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
  const onFieldChange = (field: keyof Profile, value: string | boolean) => {
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: value
      }
    })
  }

  const onDateFieldChange = (field: keyof Profile, date: Date | null) => {
    const asJavaLocalDate: number[] | undefined = date ? jsDateToJavaLocalDate(date) : undefined

    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: asJavaLocalDate
      }
    })
  }

  const onMailingAddressFieldChange = (field: keyof MailingAddress, value: string) => {
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
            placeholder={'Given Name'} aria-label={'Given Name'}
            onChange={e => onFieldChange('givenName', e.target.value)}/>
        </div>
        <div className='col'>
          <input className="form-control" type="text" value={profile.familyName || ''}
            placeholder={'Family Name'} aria-label={'Family Name'}
            onChange={e => onFieldChange('familyName', e.target.value)}/>
        </div>
      </div>
    </FormRow>
    <FormRow title={'Birthdate'}>
      <div className='row'>
        <div className="col">
          <input className="form-control" type="date"
            defaultValue={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0] || ''}
            placeholder={'Birth Date'} max={'9999-12-31'} aria-label={'Birth Date'}
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
        placeholder={'Contact Email'} aria-label={'Contact Email'}
        onChange={e => onFieldChange('contactEmail', e.target.value)}/>
    </FormRow>
    <FormRow title={'Phone'}>
      <input className="form-control" type="text" value={profile.phoneNumber || ''}
        placeholder={'Phone Number'} aria-label={'Phone'}
        onChange={e => onFieldChange('phoneNumber', e.target.value)}/>
    </FormRow>
    <FormRow title={'Notifications'}>
      <div className='row mt-2'>
        <div className="col-auto">
          <div className="form-check">

            <input className="form-check-input" type="checkbox" checked={profile.doNotEmail} id="doNotEmailCheckbox"
              aria-label={'Do Not Email'}
              onChange={e => onFieldChange('doNotEmail', e.target.checked)}/>
            <label className="form-check-label" htmlFor="doNotEmailCheckbox">
              Do Not Email
            </label>
          </div>
        </div>
        <div className='col-auto'>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" checked={profile.doNotEmailSolicit}
              id="doNotSolicitCheckbox" aria-label={'Do Not Solicit'}
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
 * Displays the given mailing address as a row within the profile
 */
export function ReadOnlyMailingAddress(
  {
    title,
    mailingAddress
  }: {
    title: string,
    mailingAddress: MailingAddress
  }
) {
  // creates the last row, formatted like: 'City, State 12345'
  // If city or state is not present, it will format like: 'State 12345' or 'City 12345'
  const createCityStatePostalRow = () => {
    const outPieces: string[] = []
    if (!isEmpty(mailingAddress.city) && !isEmpty(mailingAddress.state)) {
      outPieces.push(`${mailingAddress.city}, ${mailingAddress.state}`)
    } else if (!isEmpty(mailingAddress.city)) {
      outPieces.push(mailingAddress.city)
    } else if (!isEmpty(mailingAddress.state)) {
      outPieces.push(mailingAddress.state)
    }

    outPieces.push(mailingAddress.postalCode || '')

    return outPieces.join(' ').trim()
  }
  return <ReadOnlyRow title={title} values={[
    mailingAddress.street1 || '',
    mailingAddress.street2 || '',
    createCityStatePostalRow(),
    mailingAddress.country || ''
  ]}/>
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
    <div className="w-25 fw-bold mb-4 mt-2" aria-label={title}>
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
    onMailingAddressFieldChange: (field: keyof MailingAddress, value: string) => void,
    setMailingAddress: (update: MailingAddress) => void
  }
) {
  const [addressValidationResults, setAddressValidationResults] = useState<AddressValidationResult | undefined>()
  const [isLoadingValidation, setIsLoadingValidation] = useState<boolean>(false)
  const [hasChangedSinceValidation, setHasChangedSinceValidation] = useState<string[]>([])

  const onFieldChange = (field: keyof MailingAddress, value: string) => {
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
    if (addressValidationResults?.valid === true && isNil(addressValidationResults.suggestedAddress)) {
      return 'form-control is-valid'
    }

    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(addressValidationResults, field, mailingAddress[field])) {
      return 'form-control is-invalid'
    }

    return 'form-control'
  }

  const clearSuggestedAddress = () => {
    setAddressValidationResults(val => {
      return {
        ...(val || {}), // technically it could be undefined but should be impossible when this is called
        valid: true,
        suggestedAddress: undefined
      }
    })
  }


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
            type="text" value={mailingAddress.state || ''} placeholder={'State/Province'}
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
              // since we saved the address, we should clear it
              // from the address validation results so the modal
              // goes away
              clearSuggestedAddress()
            }}
            deny={() => {
              clearSuggestedAddress()
            }}
            onDismiss={() => {
              clearSuggestedAddress()
            }}
          />}
      {!addressValidationResults?.valid && explainAddressValidationResults(addressValidationResults)
        .map((explanation, idx) =>
          <p key={idx} className="text-danger-emphasis">{explanation}</p>
        )}
    </div>
  </FormRow>
}
