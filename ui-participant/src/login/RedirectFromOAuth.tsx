import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import Api from 'api/api'
import { usePreEnrollResponseId, usePreRegResponseId, useReturnToStudy } from 'browserPersistentState'
import { userHasJoinedStudy, enrollCurrentUserInStudy } from 'util/enrolleeUtils'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { filterUnjoinableStudies } from '../Navbar'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const RedirectFromOAuth = () => {
  const auth = useAuth()
  const { loginUser, updateEnrollee, user } = useUser()
  const navigate = useNavigate()
  const [preRegResponseId, setPreRegResponseId] = usePreRegResponseId()
  const [preEnrollResponseId, setPreEnrollResponseId] = usePreEnrollResponseId()
  const [, setReturnToStudy] = useReturnToStudy()


  const defaultEnrollStudy = useDefaultEnrollmentStudy()


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
          // Consider: detect returning from change password and show a confirmation message
          navigate('/hub', { replace: true })
        } else {
          // react-oidc-context's AuthProvider has done its thing, exchanging the OAuth code for a token.
          // Now we need to:
          //   * handle possible new user registration
          //   * handle possible study enrollment
          //   * navigate to the hub
          // Consider: remember where the user was trying to go and navigate there instead of hard-coding /hub

          const email = auth.user.profile.email as string
          const accessToken = auth.user.access_token
          // Register or login
          try {
            const loginResult = auth.user.profile.newUser
              ? await Api.register({ preRegResponseId, email, accessToken })
              : await Api.tokenLogin(accessToken)

            loginUser(loginResult, accessToken)

            // Enroll in the study if not already enrolled
            if (defaultEnrollStudy && !userHasJoinedStudy(defaultEnrollStudy, loginResult.enrollees)) {
              const hubUpdate = await enrollCurrentUserInStudy(defaultEnrollStudy.shortcode,
                defaultEnrollStudy.name, preEnrollResponseId, updateEnrollee)
              navigate('/hub', { replace: true, state: hubUpdate })
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

/**
 * hook for return a default study to enroll in, if one exists -- looks for either a study shortcode
 * in local storage or whether the portal only has a single joinable study
 */
export function useDefaultEnrollmentStudy() {
  const [returnToStudy] = useReturnToStudy()
  const { portal } = usePortalEnv()
  // Select a study to enroll in based on a previously saved session storage property
  const findReturnToStudy = () =>
    filterUnjoinableStudies(portal.portalStudies)
      .find(portalStudy => portalStudy.study.shortcode === returnToStudy)

  // Select the portal's single study if there is only one; otherwise return null
  const getSingleStudy = () => portal.portalStudies.length === 1 ? portal.portalStudies[0] : null
  return findReturnToStudy()?.study || getSingleStudy()?.study || null
}
