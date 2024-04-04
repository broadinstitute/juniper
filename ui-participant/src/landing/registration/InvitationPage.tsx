import React, { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useUser } from 'providers/UserProvider'
import Api from 'api/api'
import { useInvitationType } from 'browserPersistentState'

/** Page for participants who already have enrollee data in Juniper (from a migration or admin action), and need
 * to join to link their account */
export default function InvitationPage() {
  const auth = useAuth()
  const [searchParams] = useSearchParams()
  const accountName = searchParams.get('accountName') || ''
  const navigate = useNavigate()
  const { loginUser } = useUser()
  const [, setInvitationType] = useInvitationType()

  const createAccount = async () => {
    if (process.env.REACT_APP_UNAUTHED_LOGIN) {
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
