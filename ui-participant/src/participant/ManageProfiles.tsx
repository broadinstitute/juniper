import React from 'react'
import { useUser } from '../providers/UserProvider'
import { useName } from '../util/enrolleeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import { useI18n } from '@juniper/ui-core'
import classNames from 'classnames'

/**
 * Intermediate page for proxies to select which profile (whether it be their own or a proxied user) to edit.
 */
export default function ManageProfiles() {
  const { ppUsers } = useUser()

  const { i18n } = useI18n()

  return (
    <div
      className="hub-dashboard-background flex-grow-1 mb-2"
      style={{ background: 'var(--dashboard-background-color)' }}
    >

      <div className="my-md-4 my-5 mx-auto" style={{ maxWidth: 768 }}>
        <div className='m-2'>
          <Link to='/hub'>
            <FontAwesomeIcon icon={faChevronLeft}/>
            <span className="ms-2">{i18n('dashboard')}</span>
          </Link>
        </div>
        <main
          className="hub-dashboard shadow-sm"
          style={{ background: '#fff' }}
        >
          <div className='w-100 border-1 border-bottom p-3 text-center'>
            <span className='fs-4 fw-bold'>{i18n('manageProfiles')}</span>
          </div>
          {ppUsers
            .map((ppUser, idx) => {
              const name = useName(ppUser)

              return (
                <Link
                  to={`/hub/profile/${ppUser.id}`}
                  key={ppUser.id}
                  className={
                    classNames(
                      'btn', 'rounded-0', 'p-3', 'border-1',
                      'w-100', 'd-flex', 'w-100', 'justify-content-between',
                      'align-items-center',
                      { 'border-bottom border-1': idx != ppUsers.length - 1 })
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
