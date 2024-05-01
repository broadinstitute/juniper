import React from 'react'
import { useActiveUser } from '../providers/ActiveUserProvider'
import { useUser } from '../providers/UserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faAngleDown, faPlus, faUser, faUsers } from '@fortawesome/free-solid-svg-icons'
import { useName } from '../util/enrolleeUtils'
import { Link } from 'react-router-dom'
import { usePortalEnv } from '../providers/PortalProvider'
import { findDefaultEnrollmentStudy } from '../login/RedirectFromOAuth'
import { useI18n } from '@juniper/ui-core'

/**
 * Dropdown selector which globally changes the currently active participant. In addition, provides related options,
 * e.g. "Add new participant" and "Manage Profiles".
 */
export default function ParticipantSelector() {
  const { ppUsers } = useUser()
  const { setActiveUser, ppUser } = useActiveUser()

  const { i18n } = useI18n()

  const activeUserName = useName(ppUser || undefined)

  const { portal } = usePortalEnv()

  // in the future, this will need to be refactored to be multi-study compatible. possibly, it could
  // link to a separate page where you select the desired study to enroll in.
  const defaultStudyToEnroll = findDefaultEnrollmentStudy(null, portal.portalStudies)

  return (
    <div className="dropdown participant-selector">
      <button
        className="w-100 btn btn-outline-dark border-0 bg-white d-flex align-items-center link-dark"
        type="button" data-bs-toggle="dropdown" aria-label={i18n('selectParticipant')}
        aria-expanded="false">
        <FontAwesomeIcon icon={faUsers}/>
        <span className='mx-2 fs-5'>
          {activeUserName}
        </span>
        <div className='flex-grow-1 d-flex justify-content-end'>
          <FontAwesomeIcon icon={faAngleDown} />
        </div>
      </button>
      <ul className="dropdown-menu participant-selector-dropdown mx-1 ms-md-0" id="participant-dropdown">
        {
          ppUsers.map(ppUser => {
            const name = useName(ppUser)
            return (
              <li key={ppUser.id}>
                <button className="dropdown-item" onClick={() => setActiveUser(ppUser.id)}>
                  <FontAwesomeIcon icon={faUser}/>
                  <span className='ms-2'>
                    {name}
                  </span>
                </button>
              </li>
            )
          })
        }
        <li>
          <Link className="dropdown-item"
            to={
              defaultStudyToEnroll
                ? `/studies/${defaultStudyToEnroll.shortcode}/join?isProxyEnrollment=true`
                : '#'
            }>
            <FontAwesomeIcon icon={faPlus}/>
            <span className='ms-2'>{i18n('addNewParticipant')}</span>
          </Link>
        </li>
        <li>
          <Link className="dropdown-item" to='/hub/manageProfiles'>
            <FontAwesomeIcon icon={faPlus} className='opacity-0'/>
            <span className='ms-2'>{i18n('manageProfiles')}</span>
          </Link>
        </li>
      </ul>
    </div>
  )
}
