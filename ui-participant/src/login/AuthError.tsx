import React from 'react'
import Navbar from '../Navbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from '../providers/PortalProvider'
import { useI18n } from '@juniper/ui-core'

/**
 * Displays when there is an error from b2c.
 */
export default function AuthError() {
  const portalEnv = usePortalEnv()
  const supportEmail = portalEnv.portalEnv.portalEnvironmentConfig.emailSourceAddress || '2'
  const { i18n } = useI18n()

  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <Navbar aria-label="Primary"/>
      <main className="flex-grow-1 p-2 d-flex flex-column justify-content-center">
        <div className="fs-1 fw-bold d-flex justify-content-center">
          <div>
            <FontAwesomeIcon className="me-2" icon={faCircleExclamation}/>
            <span>
              {i18n('authErrorPageTitle')}
            </span>
          </div>
        </div>
        <div className="fs-2 fw-light d-flex justify-content-center text-center">
          <div>
            <span>
              {i18n('authErrorPageMessage', {
                substitutions: {
                  studyContactEmail: supportEmail
                }
              })}
            </span>
          </div>
        </div>
      </main>
    </div>
  )
}
