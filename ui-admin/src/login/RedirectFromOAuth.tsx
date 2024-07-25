import React, { useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { useUser } from 'user/UserProvider'
import Api from 'api/api'

export const RedirectFromOAuth = () => {
  const auth = useAuth()
  const { user } = useUser()

  useEffect(() => {
    const handleRedirectFromOauth = async () => {
      if (auth.error) {
        alert(`Auth error: ${auth.error}`)
      }

      if (auth.user && !user) {
        const accessToken = auth.user.access_token
        Api.setBearerToken(accessToken)
        // Record the login event, but since this is in a popup, we don't need to do anything with the resulting user.
        // The main application window will handle that by calling refreshLogin in response to user loaded events.
        Api.tokenLogin(accessToken).catch(() => {
          alert('We could not find an authorized user matching that login')
        })
      }
    }

    handleRedirectFromOauth()
  })

  return <div>Loading...</div>
}
