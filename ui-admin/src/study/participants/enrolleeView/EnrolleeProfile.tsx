import React, { useEffect, useState } from 'react'
import { faPencil } from '@fortawesome/free-solid-svg-icons'

import Api, { MailingAddress, PortalEnvironment } from 'api/api'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  dateToDefaultString, Enrollee,
  findDifferencesBetweenObjects,
  javaLocalDateToJsDate,
  jsDateToJavaLocalDate,
  PortalEnvironmentLanguage, Profile
} from '@juniper/ui-core'
import { cloneDeep, isEmpty } from 'lodash'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import JustifyChangesModal from '../JustifyChangesModal'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import { doApiLoad } from 'api/api-utils'
import EditMailingAddress from 'address/EditMailingAddress'
import { InfoCard, InfoCardBody, InfoCardHeader, InfoCardRow, InfoCardTitle, InfoCardValue } from 'components/InfoCard'
import { Button } from 'components/forms/Button'

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
  const portalEnv = studyEnvContext.portal.portalEnvironments.find((env: PortalEnvironment) =>
    env.environmentName === studyEnvContext.currentEnv.environmentName)
  const supportedLanguages = portalEnv?.supportedLanguages || []

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
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <InfoCardTitle title={editMode ? 'Edit Profile' : 'Profile'}/>
          {!editMode && <Button variant="light" className="border m-1" onClick={() => setEditMode(true)}>
            <FontAwesomeIcon icon={faPencil} className={'fa-lg me-2'}/>
            Edit
          </Button>}
          {editMode && <div className="flex-grow-1 d-flex justify-content-end">
            <Button variant='primary' className="border m-1" disabled={!isDirty}
              onClick={() => setShowJustifyAndSaveModal(true)}>
              Next: Add Justification
            </Button>
            <Button variant='secondary' className="border m-1" onClick={() => setEditMode(false)}>Cancel</Button>
          </div>}
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        {editMode
          ? <EditableProfile profile={editedProfile} setProfile={onProfileUpdate}/>
          : <ReadOnlyProfile profile={enrollee.profile} supportedLanguages={supportedLanguages}/>}
      </InfoCardBody>
    </InfoCard>

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
  { profile, supportedLanguages }: {
    profile: Profile, supportedLanguages: PortalEnvironmentLanguage[]
  }
) {
  const mailingAddress = profile.mailingAddress

  return <>
    <InfoCardValue title={'Name'}
      values={[
                   `${profile.givenName || ''} ${profile.familyName || ''}`.trim()
      ]}/>
    <InfoCardValue title={'Birthdate'} values={[dateToDefaultString(profile.birthDate)]}/>
    <ReadOnlyMailingAddress title={'Primary Address'} mailingAddress={mailingAddress}/>
    <InfoCardValue title={'Email'} values={[profile.contactEmail || '']}/>
    <InfoCardValue title={'Phone'} values={[profile.phoneNumber || '']}/>
    <InfoCardValue title={'Notifications'} values={[profile.doNotEmail ? 'Off' : 'On']}/>
    <InfoCardValue title={'Do Not Solicit'} values={[profile.doNotEmailSolicit ? 'On' : 'Off']}/>
    { supportedLanguages.length > 0 &&
        <InfoCardValue title={'Preferred Language'} values={[supportedLanguages.find(lang =>
          lang.languageCode === profile.preferredLanguage)?.languageName || '']}/>
    }
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
  const onFieldChange = (field: keyof Profile, value: string | boolean | MailingAddress) => {
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

  return <>
    <InfoCardRow title={'Name'}>
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
    </InfoCardRow>
    <InfoCardRow title={'Birthdate'}>
      <div className='row'>
        <div className="col">
          <input className="form-control" type="date"
            defaultValue={javaLocalDateToJsDate(profile.birthDate)?.toISOString().split('T')[0] || ''}
            placeholder={'Birth Date'} max={'9999-12-31'} aria-label={'Birth Date'}
            onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>
        </div>
        <div className="col"/>
      </div>
    </InfoCardRow>
    <EditMailingAddressRow
      title={'Primary Address'} mailingAddress={profile.mailingAddress}
      onFieldChange={onFieldChange}
    />
    <InfoCardRow title={'Email'}>
      <input className="form-control" type="text" value={profile.contactEmail || ''}
        placeholder={'Contact Email'} aria-label={'Contact Email'}
        onChange={e => onFieldChange('contactEmail', e.target.value)}/>
    </InfoCardRow>
    <InfoCardRow title={'Phone'}>
      <input className="form-control" type="text" value={profile.phoneNumber || ''}
        placeholder={'Phone Number'} aria-label={'Phone'}
        onChange={e => onFieldChange('phoneNumber', e.target.value)}/>
    </InfoCardRow>
    <InfoCardRow title={'Notifications'}>
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
    </InfoCardRow>
  </>
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
  return <InfoCardValue title={title} values={[
    mailingAddress.street1 || '',
    mailingAddress.street2 || '',
    createCityStatePostalRow(),
    mailingAddress.country || ''
  ]}/>
}


function EditMailingAddressRow(
  {
    title, mailingAddress, onFieldChange
  }: {
    title: string,
    mailingAddress: MailingAddress,
    onFieldChange: (field: keyof Profile, value: string | boolean | MailingAddress) => void
  }
) {
  const [editableMailingAddress, setEditableMailingAddress] = useState(mailingAddress)

  useEffect(() => {
    onFieldChange('mailingAddress', editableMailingAddress)
  }, [editableMailingAddress])

  return <InfoCardRow title={title}>
    <EditMailingAddress
      mailingAddress={editableMailingAddress}
      setMailingAddress={setEditableMailingAddress}/>
  </InfoCardRow>
}
