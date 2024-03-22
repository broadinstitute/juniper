import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useReturnToStudy } from 'browserPersistentState'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useI18n } from '@juniper/ui-core'

/** Show the B2C participant registration page */
export default function Registration() {
  const auth = useAuth()
  const { selectedLanguage } = useI18n()
  const studyShortcode = useParams().studyShortcode || null
  const [, setReturnToStudy] = useReturnToStudy()

  const register = () => {
    // Remember study for when we come back from B2C,
    // at which point RedirectFromOAuth will complete the registration
    setReturnToStudy(studyShortcode)
    auth.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        option: 'signup',
        // eslint-disable-next-line camelcase
        ui_locales: selectedLanguage
      }
    })
  }

  useEffect(() => {
    register()
  }, [])

  return <PageLoadingIndicator />
}
