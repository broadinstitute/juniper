import React, { useState } from 'react'
import Navbar from 'Navbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil } from '@fortawesome/free-solid-svg-icons'
import { useUser } from 'providers/UserProvider'
import { dateToDefaultString, javaLocalDateToJsDate, jsDateToJavaLocalDate } from '@juniper/ui-core'
import Api, { Profile } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from 'components/ThemedModal'
import { useNavigate } from 'react-router-dom'

/**
 *
 */
export function ParticipantProfile(
) {
  const userContext = useUser()
  const enrollee = userContext.enrollees[0]
  const profile = enrollee.profile


  const navigate = useNavigate()

  const [editedProfile, setEditedProfile] = useState<Profile>(profile)

  const [showEditFieldModal, setShowEditFieldModal] = useState<keyof Profile | undefined>()

  const onDismiss = () => {
    setShowEditFieldModal(undefined)
    // reset profile back to original state so that
    // you don't close a modal then open another
    // and accidentally commit changes from both
    setEditedProfile(profile)
  }

  const onSave = async () => {
    console.log('Fake save ')
    console.log(editedProfile)

    console.log(
      await Api.updateEnrolleeProfile({
        studyShortcode: 'asdf', enrolleeShortcode: enrollee.shortcode, profile: editedProfile
      })
    )
    navigate(0)
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

  return <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
    <Navbar aria-label="Primary"/>
    <main className="flex-grow-1 py-5">
      <div className="row mx-0 justify-content-center">
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
          {
            showEditFieldModal === 'givenName'
            && (
              <ProfileRowEditModal
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
            )
          }

          {
            showEditFieldModal === 'birthDate'
            && (
              <ProfileRowEditModal
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
            )
          }
        </div>
      </div>
    </main>
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
  return <ThemedModal show={true} onHide={onDismiss}>
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
