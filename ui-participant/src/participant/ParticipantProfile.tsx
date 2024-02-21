import React, { useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil } from '@fortawesome/free-solid-svg-icons'
import { useUser } from 'providers/UserProvider'
import { dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate } from '@juniper/ui-core'
import Api, { Profile } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from 'components/ThemedModal'

/**
 *
 */
export function ParticipantProfile(
) {
  const [showEditFieldModal, setShowEditFieldModal] = useState<keyof Profile | undefined>()

  const { ppUser, profile, updateProfile } = useUser()

  // todo: modal hooks for fns
  if (!ppUser || !profile) {
    return null
  }

  // profile should already be up-to-date, but this
  // fetches the latest on page load just in case
  const loadProfile = async () => {
    const profile = await Api.findProfile({ ppUserId: ppUser.id, alertErrors: true })
    await updateProfile(profile)
  }

  useEffect(() => {
    loadProfile()
  }, [ppUser.profileId])

  const save = async (editedProfile: Profile) => {
    const profile = await Api.updateProfile({
      ppUserId: ppUser.id, profile: editedProfile
    })
    await updateProfile(profile)
  }

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'linear-gradient(270deg, #D5ADCC 0%, #E5D7C3 100%' }} // todo: don't hardcode, see jn-902
  >
    <div className="row mx-0 justify-content-center py-5">
      <div className="col-12 col-sm-10 col-lg-6">
        {/*Readonly profile view*/}
        <ProfileCard title="Profile">
          <ProfileRow title={'Name'} onEdit={() => setShowEditFieldModal('givenName')}>
            <p className="m-0">{profile.givenName || ''} {profile.familyName || ''}</p>
          </ProfileRow>
          <ProfileRow title={'Birthday'} onEdit={() => setShowEditFieldModal('birthDate')}>
            {profile.birthDate && <p className="m-0">{dateToDefaultString(profile.birthDate)}</p>}
          </ProfileRow>
        </ProfileCard>

        <ProfileCard title="Mailing Address">
          <ProfileRow title={'Name'} onEdit={() => console.log('clicked!')}>
            <p className="m-0">{profile.givenName || ''} {profile.familyName || ''}</p>
          </ProfileRow>
        </ProfileCard>

        {/*Edit modals*/}
        <EditProfileFieldModals
          profile={profile}
          showFieldModal={showEditFieldModal}
          dismissModal={() => setShowEditFieldModal(undefined)} save={save}/>
      </div>
    </div>
  </div>
}

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

  const onFieldChange = (field: keyof Profile, value: string | boolean) => {
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

function EditProfileFieldModals(
  {
    profile,
    showFieldModal,
    dismissModal,
    save
  }: {
    profile: Profile,
    showFieldModal: keyof Profile | undefined,
    dismissModal: () => void,
    save: (p: Profile) => void
  }) {
  const modalProps: EditModalProps = {
    profile,
    dismissModal,
    save
  }

  switch (showFieldModal) {
    case 'givenName':
    case 'familyName':
      return <EditNameModal {...modalProps}/>
    case 'birthDate':
      return <EditBirthDateModal {...modalProps}/>
    case 'doNotEmailSolicit':
    case 'doNotEmail':
    case 'contactEmail':
      return <EditNotificationPreferencesModal {...modalProps}/>
    case 'mailingAddress':
      return <p>mailing address</p>
    case 'phoneNumber':
      return <EditPhoneNumberModal {...modalProps}/>
    default:
      return <></>
  }
}

type EditModalProps = {
  profile: Profile,
  dismissModal: () => void,
  save: (p: Profile) => void

}

function EditNameModal(props: EditModalProps) {
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

function EditBirthDateModal(props: EditModalProps) {
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

function EditPhoneNumberModal(props: EditModalProps) {
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
    <label htmlFor={'phoneNumber'} className={'fs-6 fw-bold'}>
      Phone Number
    </label>
    <input
      className={'form-control'}
      id={'phoneNumber'}
      value={editedProfile.phoneNumber}
      onChange={e => onFieldChange('phoneNumber', e.target.value)}
      placeholder={'Phone Number'}/>
  </ProfileRowEditModal>
}

function EditNotificationPreferencesModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)


  return <ProfileRowEditModal
    title={'Notification Preferences'}
    onSave={onSave}
    onDismiss={onDismiss}>
    <div className='row mt-2'>
      <div className="col-auto">
        <div className="form-check">

          <input className="form-check-input" type="checkbox" checked={editedProfile.doNotEmail} id="doNotEmailCheckbox"
            aria-label={'Do Not Email'}
            onChange={e => onFieldChange('doNotEmail', e.target.checked)}/>
          <label className="form-check-label" htmlFor="doNotEmailCheckbox">
            Do Not Email
          </label>
        </div>
      </div>
      <div className='col-auto'>
        <div className="form-check">
          <input className="form-check-input" type="checkbox" checked={editedProfile.doNotEmailSolicit}
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

function ProfileCard({ title, children }: { title: string, children: React.ReactNode }) {
  return <div className="card mb-3">
    <div className="card-body p-4">
      <h2 className="fw-bold pb-3">{title}</h2>
      {children}
    </div>
  </div>
}

const Bar = () => {
  return <div className="w-100 border-bottom border-1"/>
}

function ProfileRow(
  { title, children, onEdit }: { title: string, children: React.ReactNode, onEdit: () => void }
) {
  return <>
    <Bar/>
    <div className="d-flex w-100 align-content-center">
      <p className="w-25 m-0 pb-3 pt-3 fw-bold">{title}</p>
      <div className="flex-grow-1 pb-3 pt-3">
        {children}
      </div>
      <div className="flex-shrink m-0 pb-3 pt-3">
        <button className="btn btn-outline-primary float-end" onClick={onEdit}>
          <FontAwesomeIcon icon={faPencil} className={''}/>
        </button>
      </div>
    </div>
  </>
}

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
        <h2 className="fw-bold pb-3">Edit {title}</h2>
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
