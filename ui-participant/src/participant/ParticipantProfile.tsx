import React from 'react'
import Navbar from '../Navbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil } from '@fortawesome/free-solid-svg-icons'
import { useUser } from '../providers/UserProvider'
import { dateToDefaultString } from '@juniper/ui-core'

/**
 *
 */
export function ParticipantProfile(
) {
  const userContext = useUser()
  const profile = userContext.enrollees[0].profile

  return <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
    <Navbar aria-label="Primary"/>
    <main className="flex-grow-1 py-5">
      <div className="row mx-0 justify-content-center">
        <div className="col-12 col-sm-10 col-lg-6">
          <ProfileCard title="Profile">
            <ProfileRow title={'Name'} onEdit={() => console.log('clicked!')}>
              <p className="m-0">{profile.givenName || ''} {profile.familyName || ''}</p>
            </ProfileRow>
            <ProfileRow title={'Birthday'} onEdit={() => console.log('clicked!')}>
              {profile.birthDate && <p className="m-0">{dateToDefaultString(profile.birthDate)}</p>}
            </ProfileRow>
          </ProfileCard>

          <ProfileCard title="Mailing Address">
            <ProfileRow title={'Name'} onEdit={() => console.log('clicked!')}>
              <p className="m-0">{profile.givenName || ''} {profile.familyName || ''}</p>
            </ProfileRow>
          </ProfileCard>
        </div>
      </div>
    </main>
  </div>
}

function ProfileCard({ title, children } : {title: string, children: React.ReactNode}) {
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
        <button className="btn btn-outline-primary float-end">
          <FontAwesomeIcon icon={faPencil} className={''}/>
        </button>
      </div>
    </div>
  </>
}
