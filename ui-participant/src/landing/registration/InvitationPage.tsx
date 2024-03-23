import React, { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { PageLoadingIndicator } from 'util/LoadingSpinner'

/** Page for participants who already have enrollee data in Juniper (from a migration or admin action), and need
 * to join to link their account */
export default function InvitationPage() {
  const auth = useAuth()
  const [searchParams] = useSearchParams()
  const accountName = searchParams.get('accountName') || ''
  const navigate = useNavigate()

  const register = () => {
    if (process.env.REACT_APP_UNAUTHED_LOGIN) {
      /**
       * this is a no-op redirect to hub -- since we're in Unauthed mode,
       * there is no b2c account that needs to be created
       */
      navigate('/hub')
      return
    }
    auth.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        option: 'signup2',
        // eslint-disable-next-line camelcase
        login_hint: accountName
      }
    })
  }

  useEffect(() => {
    register()
  }, [])

  return <PageLoadingIndicator />
}
