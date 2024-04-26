import React from 'react'
import { useActiveUser } from '../providers/ActiveUserProvider'
import { useUser } from '../providers/UserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faAngleDown, faPlus, faUser, faUsers } from '@fortawesome/free-solid-svg-icons'

/**
 *
 */
export default function ParticipantSelector() {
  const { ppUsers, enrollees, user } = useUser()
  const { setActiveUser, profile, ppUser } = useActiveUser()

  return (
    <div className="dropdown" style={{ width: '300px' }}>
      <button
        className="w-100 btn btn-outline-dark border-0 bg-white d-flex align-items-center link-dark"
        type="button" data-bs-toggle="dropdown"
        aria-expanded="false">
        <FontAwesomeIcon icon={faUsers}/>
        <span className='mx-2 fs-5'>
          {profile?.givenName} {profile?.familyName}
          {ppUser?.participantUserId === user?.id ? ' (you)' : ''}
        </span>
        <div className='flex-grow-1 d-flex justify-content-end'>
          <FontAwesomeIcon icon={faAngleDown} />
        </div>
      </button>
      <ul className="dropdown-menu">
        {
          ppUsers.map(ppUser => {
            const userProfile = enrollees.find(enrollee => enrollee.profileId === ppUser.profileId)?.profile
            return (
              <li key={ppUser.id}>
                <button className="dropdown-item" onClick={() => setActiveUser(ppUser.id)}>
                  <FontAwesomeIcon icon={faUser}/>
                  <span className='ms-2'>
                    {userProfile?.givenName} {userProfile?.familyName}
                    {ppUser.participantUserId === user?.id ? ' (you)' : ''}
                  </span>
                </button>
              </li>
            )
          })
        }
        <li>
          <button className="dropdown-item" onClick={() => {
            // todo
          }}>
            <FontAwesomeIcon icon={faPlus}/>
            <span className='ms-2'>Add new participant</span>
          </button>
        </li>
        <li>
          <button className="dropdown-item" onClick={() => {
            // todo
          }}>
            <FontAwesomeIcon icon={faPlus} className='opacity-0'/>
            <span className='ms-2'>Manage Profiles</span>
          </button>
        </li>
      </ul>
    </div>
  )
}
