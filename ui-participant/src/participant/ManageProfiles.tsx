import React from 'react'
import { useUser } from '../providers/UserProvider'
import { useName } from '../util/enrolleeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronRight } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'

/**
 *
 */
export default function ManageProfiles() {
  const { ppUsers } = useUser()

  return (
    <div
      className="hub-dashboard-background flex-grow-1 mb-2"
      style={{ background: 'var(--dashboard-background-color)' }}
    >
      <div className="my-md-4 mx-auto" style={{ maxWidth: 768 }}>
        <main
          className="hub-dashboard shadow-sm"
          style={{ background: '#fff' }}
        >
          <div className='w-100 border-1 border-bottom p-3'>
            <span className='fs-5'>Manage Profiles</span>
          </div>
          {ppUsers.map((ppUser, idx) => {
            const name = useName(ppUser)
            return (
              <Link
                to={`/hub/profile`}
                state={{ ppUserId: ppUser.id }}
                key={ppUser.id}
                className={
                  'btn rounded-0 p-3 border-1 w-100 '
                    +'d-flex w-100 justify-content-between align-items-center '
                    + `${idx != ppUsers.length - 1 ? 'border-bottom border-1' : ''}`
                }>
                <span className='fs-5'>{name}</span>
                <FontAwesomeIcon icon={faChevronRight}/>
              </Link>
            )
          })}
        </main>
      </div>


    </div>
  )
}
