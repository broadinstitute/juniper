import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import Api, { PortalStudy } from 'api/api'
import {
  useInvitationType,
  usePreEnrollResponseId,
  usePreRegResponseId,
  useReturnToLanguage,
  useReturnToStudy
} from 'browserPersistentState'
import { enrollCurrentUserInStudy } from 'util/enrolleeUtils'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import {
  filterUnjoinableStudies,
  useI18n
} from '@juniper/ui-core'
import {
  log,
  logError
} from 'util/loggingUtils'
import { handleNewStudyEnroll } from '../studies/enroll/StudyEnrollRouter'

export const RedirectFromOAuth = () => {
  const auth = useAuth()
  const { loginUser, user, refreshLoginState } = useUser()
  const navigate = useNavigate()
  const [preRegResponseId, setPreRegResponseId] = usePreRegResponseId()
  const [preEnrollResponseId, setPreEnrollResponseId] = usePreEnrollResponseId()
  const [returnToStudy, setReturnToStudy] = useReturnToStudy()
  const [, setInvitationType] = useInvitationType()
  const [returnToLanguage, setReturnToLanguage] = useReturnToLanguage()
  const { portal } = usePortalEnv()
  const { i18n } = useI18n()

  const defaultEnrollStudy = findDefaultEnrollmentStudy(returnToStudy, portal.portalStudies)


  useEffect(() => {
    const handleRedirectFromOauth = async () => {
      // RedirectFromOAuth may be rendered before react-oidc-context's AuthProvider has finished doing its thing. If so,
      // do nothing and wait until a render after AuthProvider is done.
      // Also, we'll be manipulating state, so we may get rendered more than once before we navigate away, so make sure
      // we only process the return from OAuth once (when the user is still "anonymous")

      if (auth.error && user) {
        // This case can happen if the user is already logged in and tries to log in again with a consumed oauth state.
        // The user already has a valid session, so we'll log that this happened but navigate to the hub without hassle.
        // Note: this logs as INFO because we want to know that this happened, but it's a non-fatal error.
        log({
          eventType: 'INFO', eventName: 'oauth-error',
          stackTrace: auth.error.stack, eventDetail: auth.error.message
        })
        navigate('/hub', { replace: true })
        return
      }

      if (auth.error) {
        logError({ message: auth.error.message || 'error' }, auth.error.stack || 'stack', 'oauth-error')
        navigate('/redirect-from-oauth/error')
        return
      }

      if (auth.user) {
        if (user) {
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
            /**
             * we attempt to either register or login, since detecting whether this is a new signup is
             * occasionally unreliable due to network events, etc...
             * If we later add portals with very strict pre-registration criteria, we may want to update this code
             */
            const loginResult = await Api.registerOrLogin({
              preRegResponseId, email, accessToken, preferredLanguage: returnToLanguage
            })

            loginUser(loginResult, accessToken)

            // Enroll in the study if not already enrolled in any other study
            if (defaultEnrollStudy && !loginResult.enrollees.length) {
              const hubResponse = await enrollCurrentUserInStudy(
                defaultEnrollStudy.shortcode, preEnrollResponseId, refreshLoginState)

              handleNewStudyEnroll(hubResponse, defaultEnrollStudy.shortcode, navigate, i18n, defaultEnrollStudy.name)
            } else {
              navigate('/hub', { replace: true })
            }
          } catch (e) {
            logError({ message: 'Error on OAuth redirect' }, (e as ErrorEvent)?.error?.stack)
            navigate('/hub', { replace: true })
          }

          setPreRegResponseId(null)
          setPreEnrollResponseId(null)
          setReturnToStudy(null)
          setInvitationType(null)
          setReturnToLanguage(null)
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
export function findDefaultEnrollmentStudy(returnToStudy: string | null, portalStudies: PortalStudy[]) {
  const joinableStudies = filterUnjoinableStudies(portalStudies)
  // Select a study to enroll in based on a previously saved session storage property
  const findReturnToStudy = () => joinableStudies.find(portalStudy => portalStudy.study.shortcode === returnToStudy)

  // Select the portal's single study if there is only one; otherwise return null
  const getSingleStudy = () => joinableStudies.length === 1 ? joinableStudies[0] : null
  return findReturnToStudy()?.study || getSingleStudy()?.study || null
}
