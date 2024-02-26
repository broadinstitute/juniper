import React, { useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil } from '@fortawesome/free-solid-svg-icons'
import { useUser } from 'providers/UserProvider'
import { dateToDefaultString } from '@juniper/ui-core'
import Api, { MailingAddress, Profile } from 'api/api'
import { isEmpty } from 'lodash'
import {
  EditBirthDateModal,
  EditCommunicationPreferences,
  EditContactEmail,
  EditMailingAddressModal,
  EditNameModal,
  EditPhoneNumber
} from './EditParticipantProfileModals'

/**
 * Shows the Participant's profile as a series of cards. Each property is a row
 * with an edit button. This edit button opens a modal which allows editing and
 * saving changes to profile data.
 */
export function ParticipantProfile(
) {
  const [showEditFieldModal, setShowEditFieldModal] = useState<keyof Profile | undefined>()

  const { ppUser, profile, updateProfile } = useUser()


  // profile should already be up-to-date, but this
  // fetches the latest on page load just in case
  const loadProfile = async () => {
    if (ppUser) {
      const profile = await Api.findProfile({ ppUserId: ppUser.id, alertErrors: true })
      await updateProfile(profile)
    }
  }

  useEffect(() => {
    loadProfile()
  }, [ppUser?.profileId])

  const save = async (editedProfile: Profile) => {
    if (ppUser) {
      const profile = await Api.updateProfile({
        ppUserId: ppUser.id, profile: editedProfile
      })
      await updateProfile(profile)
    }
  }

  if (!ppUser || !profile) {
    return null
  }

  const findAppropriateEditModal = () => {
    const modalProps = {
      profile,
      dismissModal: () => setShowEditFieldModal(undefined),
      save
    }

    switch (showEditFieldModal) {
      case 'givenName':
      case 'familyName':
        return <EditNameModal {...modalProps}/>
      case 'birthDate':
        return <EditBirthDateModal {...modalProps}/>
      case 'doNotEmailSolicit':
      case 'doNotEmail':
        return <EditCommunicationPreferences {...modalProps}/>
      case 'contactEmail':
        return <EditContactEmail {...modalProps}/>
      case 'phoneNumber':
        return <EditPhoneNumber {...modalProps}/>
      case 'mailingAddress':
        return <EditMailingAddressModal {...modalProps}/>
      default:
        return null
    }
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
            <ProfileTextRow text={
              (profile.givenName || profile.familyName)
                ? `${profile.givenName || ''} ${profile.familyName || ''}`
                : undefined
            }/>
          </ProfileRow>
          <ProfileRow title={'Birthday'} onEdit={() => setShowEditFieldModal('birthDate')}>
            <ProfileTextRow text={
              profile.birthDate && dateToDefaultString(profile.birthDate)
            }/>
          </ProfileRow>
        </ProfileCard>

        <ProfileCard title="Mailing Address">
          <ProfileRow title={'Primary Address'} onEdit={() => setShowEditFieldModal('mailingAddress')}>
            <ReadOnlyAddress address={profile.mailingAddress}/>
          </ProfileRow>
        </ProfileCard>

        <ProfileCard title="Communication Preferences">
          <ProfileRow title={'Contact Email'} onEdit={() => setShowEditFieldModal('contactEmail')}>
            <ProfileTextRow text={profile.contactEmail}/>
          </ProfileRow>
          <ProfileRow title={'Phone Number'} onEdit={() => setShowEditFieldModal('phoneNumber')}>
            <ProfileTextRow text={profile.phoneNumber}/>
          </ProfileRow>
          <ProfileRow title={'Notifications'} onEdit={() => setShowEditFieldModal('doNotEmail')}>
            <ProfileTextRow text={profile.doNotEmail ? 'Off' : 'On'}/>
          </ProfileRow>
          <ProfileRow title={'Do Not Solicit'} onEdit={() => setShowEditFieldModal('doNotEmailSolicit')}>
            <ProfileTextRow text={profile.doNotEmailSolicit ? 'On' : 'Off'}/>
          </ProfileRow>
        </ProfileCard>

        {/*Edit modals*/}
        {showEditFieldModal && findAppropriateEditModal()}
      </div>
    </div>
  </div>
}

function ProfileCard({ title, children }: { title: string, children: React.ReactNode }) {
  return <div className="card mb-3">
    <div className="card-body p-4">
      <h2 className="fw-bold pb-3">{title}</h2>
      {children}
    </div>
  </div>
}

const ProfileTextRow = ({ text }: { text: string | undefined }) => {
  return (!isEmpty(text)
    ? <p className="m-0">{text}</p>
    : <p className="m-0 fst-italic text-secondary">Not provided</p>
  )
}

const ReadOnlyAddress = ({ address }: { address: MailingAddress | undefined }) => {
  if (
    !address || (
      isEmpty(address.city)
      && isEmpty(address.street1)
      && isEmpty(address.street2)
      && isEmpty(address.country)
      && isEmpty(address.postalCode)
      && isEmpty(address.state)
    )) {
    return <ProfileTextRow text={undefined}/>
  }

  return <>
    {address.street1 && <ProfileTextRow text={address.street1}/>}
    {address.street2 && <ProfileTextRow text={address.street2}/>}
    {(address.city || address.postalCode || address.state) &&
        <ProfileTextRow text={`${address.city} ${address.state} ${address.postalCode}`.split(/\s+/).join(' ')}/>}
    {address.country && <ProfileTextRow text={address.country}/>}
  </>
}

const HorizontalBar = () => {
  return <div className="w-100 border-bottom border-1"/>
}

function ProfileRow(
  {
    title, children, onEdit
  }: {
    title: string, children: React.ReactNode, onEdit: () => void
  }
) {
  return <>
    <HorizontalBar/>
    <div className="d-flex w-100 align-content-center">
      <div className="w-25">
        <p className="m-0 pb-3 pt-3 fw-bold">{title}</p>
      </div>
      <div className="flex-grow-1 pb-3 pt-3">
        {children}
      </div>
      <div className="flex-shrink m-0 pb-3 pt-3">
        <button
          className="btn btn-outline-primary float-end"
          onClick={onEdit}
          aria-label={`Edit ${title}`}
        >
          <FontAwesomeIcon icon={faPencil} className={''}/>
        </button>
      </div>
    </div>
  </>
}

