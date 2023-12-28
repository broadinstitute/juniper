import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import Api from 'api/api'
import { HubUpdate } from 'hub/hubUpdates'
import { usePreEnrollResponseId, usePreRegResponseId, useReturnToStudy } from 'browserPersistentState'
import { userHasJoinedPortalStudy } from 'util/enrolleeUtils'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { AlertLevel, alertDefaults } from '@juniper/ui-core'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const RedirectFromOAuth = () => {
  const auth = useAuth()
  const { loginUser, updateEnrollee, user } = useUser()
  const navigate = useNavigate()
  const [preRegResponseId, setPreRegResponseId] = usePreRegResponseId()
  const [preEnrollResponseId, setPreEnrollResponseId] = usePreEnrollResponseId()
  const [returnToStudy, setReturnToStudy] = useReturnToStudy()
  const { portal } = usePortalEnv()

  // Select a study to enroll in based on a previously saved session storage property
  const findReturnToStudy = () =>
    portal.portalStudies.find(portalStudy => portalStudy.study.shortcode === returnToStudy)

  // Select the portal's single study if there is only one; otherwise return null
  const getSingleStudy = () => portal.portalStudies.length === 1 ? portal.portalStudies[0] : null


  useEffect(() => {
    const handleRedirectFromOauth = async () => {
      // RedirectFromOAuth may be rendered before react-oidc-context's AuthProvider has finished doing its thing. If so,
      // do nothing and wait until a render after AuthProvider is done.
      // Also, we'll be manipulating state, so we may get rendered more than once before we navigate away, so make sure
      // we only process the return from OAuth once (when the user is still "anonymous")

      if (auth.error) {
        navigate('/')
      }

      if (auth.user) {
        if (!user.isAnonymous) {
          // TODO: detect returning from change password and show a confirmation message
          navigate('/hub', { replace: true })
        } else {
          // react-oidc-context's AuthProvider has done its thing, exchanging the OAuth code for a token.
          // Now we need to:
          //   * handle possible new user registration
          //   * handle possible study enrollment
          //   * navigate to the hub
          // TODO: remember where the user was trying to go and navigate there instead of hard-coding /hub

          const email = auth.user.profile.email as string
          const accessToken = auth.user.access_token

          // Register or login
          try {
            const loginResult = auth.user.profile.newUser
              ? await Api.register({ preRegResponseId, email, accessToken })
              : await Api.tokenLogin(accessToken)

            loginUser(loginResult, accessToken)

            // Decide if there's a study that has either been explicitly selected
            // or is implicit because it's the only one
            const portalStudy = findReturnToStudy() || getSingleStudy() || null

            // Enroll in the study if not already enrolled
            if (portalStudy && !userHasJoinedPortalStudy(portalStudy, loginResult.enrollees)) {
              const response = await Api.createEnrollee({
                studyShortcode: portalStudy.study.shortcode,
                preEnrollResponseId
              })
              const hubUpdate: HubUpdate = {
                message: {
                  title: `Welcome to ${portalStudy.study.name}`,
                  detail: alertDefaults['WELCOME'].detail,
                  type: alertDefaults['WELCOME'].type as AlertLevel
                }
              }
              updateEnrollee(response.enrollee).then(() => {
                navigate('/hub', { replace: true, state: hubUpdate })
              })
            } else {
              navigate('/hub', { replace: true })
            }
          } catch {
            navigate('/hub', { replace: true })
          }

          setPreRegResponseId(null)
          setPreEnrollResponseId(null)
          setReturnToStudy(null)
        }
      }
    }

    handleRedirectFromOauth()
    /**
     * only process redirect logic if the auth token has changed.  Previously, we were redirecting
     * on all rerenders, which was leading to multiple redirects if, for example, the useUser() was
     * updated in response to the loginUser() call
     */
  }, [auth.user?.access_token])

  return <PageLoadingIndicator />
}
