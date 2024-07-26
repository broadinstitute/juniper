import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from './providers/PortalProvider'
import Navbar from './Navbar'
import { Link } from 'react-router-dom'
import { useI18n } from '@juniper/ui-core'

/**
 * Displays when there is an unmatched participant route.
 */
export default function PageNotFound() {
  const portalEnv = usePortalEnv()
  const studyContactEmail = portalEnv.portalEnv.portalEnvironmentConfig.emailSourceAddress || ''
  const { i18n } = useI18n()

  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <Navbar aria-label="Primary"/>
      <main className="flex-grow-1 p-2 d-flex flex-column justify-content-center">
        <div className="fs-1 fw-bold d-flex justify-content-center">
          <div>
            <FontAwesomeIcon className="me-2" icon={faCircleExclamation}/>
            <span>{i18n('pageNotFoundTitle')}</span>
          </div>
        </div>
        <div className="fs-2 fw-light d-flex justify-content-center text-center">
          <div>
            <span>
              {i18n('pageNotFoundMessage', {
                substitutions: {
                  studyContactEmail
                }
              })}
            </span>
            <div className="d-flex justify-content-center mt-3">
              <Link className="btn btn-outline-primary" to={'/'}>
                {i18n('pageNotFoundReturnHome')}
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
