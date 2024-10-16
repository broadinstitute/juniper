import React, { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useUser } from 'providers/UserProvider'
import Api, { getEnvSpec } from 'api/api'
import { useInvitationType } from 'browserPersistentState'
import envVars from 'util/envVars'

/** Page for participants who already have enrollee data in Juniper (from a migration or admin action), and need
 * to join to link their account */
export default function InvitationPage() {
  const auth = useAuth()
  const [searchParams] = useSearchParams()
  const accountName = searchParams.get('accountName') || ''
  const navigate = useNavigate()
  const { loginUser } = useUser()
  const [, setInvitationType] = useInvitationType()
  const envSpec = getEnvSpec()

  const createAccount = async () => {
    if (envVars.unauthedLogin) {
      // we don't need to create a b2c account, just log the user in
      const result = await Api.unauthedLogin(accountName)
      loginUser(result, result.user.token)
      navigate('/hub')
      return
    }

    setInvitationType('directLink')
    auth.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        option: 'signup',
        originUrl: window.location.origin,
        portalEnvironment: envSpec.envName,
        portalShortcode: envSpec.shortcode as string,
        // eslint-disable-next-line camelcase
        login_hint: accountName
      }
    })
  }

  useEffect(() => {
    createAccount()
  }, [])

  return <PageLoadingIndicator />
}
