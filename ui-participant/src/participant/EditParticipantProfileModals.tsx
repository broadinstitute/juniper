import React, { useEffect, useState } from 'react'
import { Profile } from '../api/api'
import { javaLocalDateToJsDate, jsDateToJavaLocalDate, MailingAddress } from '@juniper/ui-core'
import ThemedModal from '../components/ThemedModal'
import Modal from 'react-bootstrap/Modal'
import EditAddress from '@juniper/ui-core/build/components/EditAddress'


// skeleton for all profile edit modals
function ProfileRowEditModal(
  {
    title, children, onSave, onDismiss
  }: {
    title: string, children: React.ReactNode, onSave: () => void, onDismiss: () => void
  }
) {
  return <ThemedModal show={true} onHide={onDismiss} size={'lg'}>
    <Modal.Header>
      <Modal.Title>
        <h2 className="fw-bold pb-0 mb-0">Edit {title}</h2>
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      {children}
    </Modal.Body>
    <Modal.Footer>
      <div className={'d-flex w-100'}>
        <button className={'btn btn-primary m-2'} onClick={onSave}>Save</button>
        <button className={'btn btn-outline-secondary m-2'} onClick={onDismiss}>Cancel</button>
      </div>
    </Modal.Footer>
  </ThemedModal>
}

// common props for all profile edit modals
type EditModalProps = {
  profile: Profile,
  dismissModal: () => void,
  save: (p: Profile) => void

}

// internal profile editing methods
const useProfileEditMethods = (props: EditModalProps) => {
  const {
    profile,
    dismissModal,
    save
  } = props

  const [editedProfile, setEditedProfile] = useState<Profile>(profile)

  const onDismiss = () => {
    dismissModal()
    // reset profile back to original state so that
    // you don't close a modal then open another
    // and accidentally commit changes from both
    setEditedProfile(profile)
  }

  const onFieldChange = (field: keyof Profile, value: string | boolean | MailingAddress) => {
    setEditedProfile(oldProfile => {
      return {
        ...oldProfile,
        [field]: value
      }
    })
  }

  const onDateFieldChange = (field: keyof Profile, date: Date | null) => {
    const asJavaLocalDate: number[] | undefined = date ? jsDateToJavaLocalDate(date) : undefined

    setEditedProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: asJavaLocalDate
      }
    })
  }

  const onSave = () => {
    save(editedProfile)
    dismissModal()
  }

  return {
    onDismiss,
    onSave,
    onDateFieldChange,
    onFieldChange,
    editedProfile
  }
}

/**
 * Modal for editing the givenName and familyName properties on a profile.
 */
export function EditNameModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Name'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <div className={'d-flex w-100'}>
      <div className={'w-50 p-1'}>
        <label htmlFor={'givenName'} className={'fs-6 fw-bold'}>
          Given Name
        </label>
        <input
          className={'form-control'}
          id={'givenName'}
          value={editedProfile.givenName}
          onChange={e => onFieldChange('givenName', e.target.value)}
          placeholder={'Given Name'}/>
      </div>
      <div className={'w-50 p-1'}>
        <label htmlFor={'familyName'} className={'fs-6 fw-bold'}>
          Family Name
        </label>
        <input
          className={'form-control'}
          id={'familyName'}
          value={editedProfile.familyName}
          onChange={e => onFieldChange('familyName', e.target.value)}
          placeholder={'Family Name'}/>
      </div>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the birthDate property on a profile.
 */
export function EditBirthDateModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onDateFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Birthday'}
    onSave={onSave}
    onDismiss={onDismiss}>

    <label htmlFor={'birthDate'} className={'fs-6 fw-bold'}>
      Birthday
    </label>
    <input className="form-control" type="date" id='birthDate'
      defaultValue={javaLocalDateToJsDate(editedProfile.birthDate)?.toISOString().split('T')[0] || ''}
      placeholder={'Birth Date'} max={'9999-12-31'} aria-label={'Birth Date'}
      onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>

  </ProfileRowEditModal>
}

/**
 * Modal for editing the phoneNumber property on a profile.
 */
export function EditPhoneNumber(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Phone Number'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <div>
      <label htmlFor={'phoneNumber'} className={'fs-6 fw-bold'}>
        Phone Number
      </label>
      <input
        className={'form-control'}
        type={'tel'} // on mobile, brings up numpad instead of keyboard
        id={'phoneNumber'}
        value={editedProfile.phoneNumber}
        onChange={e => onFieldChange('phoneNumber', e.target.value)}
        placeholder={'Phone Number'}/>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the contactEmail property on a profile.
 */
export function EditContactEmail(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Contact Email'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <div>
      <p className="fst-italic">
        Press &quot;Save&quot; to update the email used for communication. Note that your login information will not
        change.
      </p>
      <label htmlFor={'contactEmail'} className={'fs-6 fw-bold'}>
        Email
      </label>
      <input
        className={'form-control'}
        id={'contactEmail'}
        value={editedProfile.contactEmail}
        onChange={e => onFieldChange('contactEmail', e.target.value)}
        placeholder={'Email'}/>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the doNotEmail and doNotSolicit properties on a profile.
 */
export function EditCommunicationPreferences(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Communication Preferences'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <div className='row mt-2'>
      <div className="col-auto">
        <div className="form-check">
          <input className="form-check-input" type="checkbox"
            checked={editedProfile.doNotEmail} id="doNotEmailCheckbox"
            aria-label={'Do Not Email'}
            onChange={e => onFieldChange('doNotEmail', e.target.checked)}/>
          <label className="form-check-label" htmlFor="doNotEmailCheckbox">
            Do Not Email
          </label>
        </div>
      </div>
      <div className='col-auto'>
        <div className="form-check">
          <input className="form-check-input" type="checkbox"
            checked={editedProfile.doNotEmailSolicit}
            id="doNotSolicitCheckbox" aria-label={'Do Not Solicit'}
            onChange={e => onFieldChange('doNotEmailSolicit', e.target.checked)}/>
          <label className="form-check-label" htmlFor="doNotSolicitCheckbox">
            Do Not Solicit
          </label>
        </div>
      </div>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the mailing address properties on a profile.
 */
export function EditMailingAddressModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const [mailingAddress, setMailingAddress] = useState<MailingAddress>(
    editedProfile.mailingAddress || {
      street1: '',
      street2: '',
      city: '',
      state: '',
      postalCode: '',
      country: ''
    }
  )

  useEffect(() => {
    onFieldChange('mailingAddress', mailingAddress)
  }, [mailingAddress])

  return <ProfileRowEditModal
    title={'Mailing Address'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <EditAddress
      mailingAddress={mailingAddress}
      setMailingAddress={setMailingAddress}
      language={'en'}
      showLabels={true}
    />
  </ProfileRowEditModal>
}