import React, { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { getEnvSpec } from 'api/api'
import { getB2CLocale } from '@juniper/ui-core'

/** Page for participants who already have enrollee data in Juniper (from a migration or admin action), and need
 * to join to link their account */
export default function ResetPasswordPage() {
  const auth = useAuth()
  const [searchParams] = useSearchParams()
  const accountName = searchParams.get('accountName') || ''
  const preferredLanguage = searchParams.get('preferredLanguage') || ''
  const envSpec = getEnvSpec()

  const createAccount = async () => {
    auth.signinRedirect({
      redirectMethod: 'replace',

      extraQueryParams: {
        option: 'resetpassword',
        originUrl: window.location.origin,
        portalEnvironment: envSpec.envName,
        portalShortcode: envSpec.shortcode as string,
        // eslint-disable-next-line camelcase
        login_hint: accountName,
        // eslint-disable-next-line camelcase
        ui_locales: getB2CLocale(preferredLanguage)
      }
    })
  }

  useEffect(() => {
    createAccount()
  }, [])

  return <PageLoadingIndicator />
}
