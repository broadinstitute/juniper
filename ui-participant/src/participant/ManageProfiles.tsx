import React from 'react'
import { useUser } from '../providers/UserProvider'
import { useName } from '../util/enrolleeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons'
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
      <Link to='/hub' className={'m-2 ms-3'} style={{ position: 'absolute' }}>
        <FontAwesomeIcon icon={faChevronLeft}/>
        <span className="ms-2">Dashboard</span>
      </Link>

      <div className="my-md-4 my-5 mx-auto" style={{ maxWidth: 768 }}>
        <main
          className="hub-dashboard shadow-sm"
          style={{ background: '#fff' }}
        >
          <div className='w-100 border-1 border-bottom p-3 text-center'>
            <span className='fs-4 fw-bold'>Manage Profiles</span>
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
