import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useReturnToLanguage, useReturnToStudy } from 'browserPersistentState'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useI18n } from '@juniper/ui-core'
import { getEnvSpec } from '../../api/api'

/** Show the B2C participant registration page */
export default function Registration() {
  const auth = useAuth()
  const { selectedLanguage } = useI18n()
  const studyShortcode = useParams().studyShortcode || null
  const [, setReturnToStudy] = useReturnToStudy()
  const [, setReturnToLanguage] = useReturnToLanguage()
  const envSpec = getEnvSpec()

  const register = () => {
    // Remember study for when we come back from B2C,
    // at which point RedirectFromOAuth will complete the registration
    setReturnToStudy(studyShortcode)
    setReturnToLanguage(selectedLanguage)
    auth.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        option: 'signup',
        originUrl: window.location.origin,
        portalEnvironment: envSpec.envName,
        portalShortcode: envSpec.shortcode as string,
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
