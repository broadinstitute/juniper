import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useUser } from 'providers/UserProvider'
import Api from 'api/api'
import { usePreEnrollResponseId, usePreRegResponseId, useReturnToStudy } from 'state'

export const RedirectFromOAuth = () => {
  const auth = useAuth()
  const { loginUser, updateEnrollee, user } = useUser()
  const navigate = useNavigate()
  const [preRegResponseId, setPreRegResponseId] = usePreRegResponseId()
  const [preEnrollResponseId, setPreEnrollResponseId] = usePreEnrollResponseId()
  const [returnToStudy, setReturnToStudy] = useReturnToStudy()

  useEffect(() => {
    const handleRedirectFromOauth = async () => {
      // RedirectFromOAuth may be rendered before react-oidc-context's AuthProvider has finished doing its thing. If so,
      // do nothing and wait until a render after AuthProvider is done.
      // Also, we'll be manipulating state, so we may get rendered more than once before we navigate away, so make sure
      // we only process the return from OAuth once (when the user is still "anonymous")

      if (auth.user && user.isAnonymous) {
        // react-oidc-context's AuthProvider has done its thing, exchanging the OAuth code for a token.
        // Now we need to:
        //   * handle possible new user registration
        //   * handle possible study enrollment
        //   * navigate to the hub
        // TODO: remember where the user was trying to go and navigate there instead of hard-coding /hub

        if (auth.user.profile.newUser) {
          const loginResult = await Api.register({
            preRegResponseId,
            email: auth.user.profile.email as string,
            accessToken: auth.user.access_token
          })
          loginUser(loginResult, auth.user.access_token)
          setPreRegResponseId(null)
        } else {
          const loginResult = await Api.tokenLogin(auth.user.access_token)
          loginUser(loginResult, auth.user.access_token)
        }

        if (preEnrollResponseId && returnToStudy) {
          try {
            const response = await Api.createEnrollee({ studyShortcode: returnToStudy, preEnrollResponseId })
            updateEnrollee(response.enrollee)
            navigate('/hub', {
              replace: true,
              state: { message: { content: 'Welcome to the study!', messageType: 'success' } }
            })
          } catch {
            alert('an error occurred, please try again, or contact support')
          }

          setPreEnrollResponseId(null)
          setReturnToStudy(null)
        }

        // If we haven't already navigated somewhere, navigate to the hub (or wherever they were trying to go to) now
        navigate('/hub', { replace: true })
      }
    }

    handleRedirectFromOauth()
  })

  return <div>Loading...</div>
}
