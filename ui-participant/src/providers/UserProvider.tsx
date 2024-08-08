import React, {
  useContext,
  useEffect,
  useState
} from 'react'
import { useAuth } from 'react-oidc-context'
import Api, {
  LoginResult,
  PortalParticipantUser
} from 'api/api'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import {
  Enrollee,
  EnrolleeRelation,
  ParticipantUser,
  Profile
} from '@juniper/ui-core'
import envVars from 'util/envVars'
import mixpanel from 'mixpanel-browser'

/**
 * The user provide contains the _raw_ user context, which is more or less directly derived
 * from the login state. If you are trying to access the currently active user, accounting
 * for proxy, you should use the ActiveUserProvider.
 */

export type UserContextT = {
  user: ParticipantUser | null,
  // these are the portal participant users and enrollees that you have access to,
  // including proxied users. The user object is the person that is actually currently
  // logged in.
  ppUsers: PortalParticipantUser[]
  enrollees: Enrollee[],
  proxyRelations: EnrolleeRelation[],
  profile?: Profile,
  loginUser: (result: LoginResult, accessToken: string) => void,
  loginUserInternal: (result: LoginResult) => void,
  logoutUser: () => void,
  updateEnrollee: (enrollee: Enrollee, updateWithoutRerender?: boolean) => Promise<void>
  updateProfile: (profile: Profile, updateWithoutRerender?: boolean) => Promise<void>
  refreshLoginState: () => Promise<void>
}

/** current user object context */
const UserContext = React.createContext<UserContextT>({
  user: null,
  ppUsers: [],
  enrollees: [],
  proxyRelations: [],
  loginUser: () => {
    throw new Error('context not yet initialized')
  },
  loginUserInternal: () => {
    throw new Error('context not yet initialized')
  },
  logoutUser: () => {
    throw new Error('context not yet initialized')
  },
  updateEnrollee: () => {
    throw new Error('context not yet initialized')
  },
  updateProfile: () => {
    throw new Error('context not yet initialized')
  },
  refreshLoginState: () => {
    throw new Error('context not yet initialized')
  }
})
const INTERNAL_LOGIN_TOKEN_KEY = 'internalLoginToken'
const OAUTH_ACCESS_TOKEN_KEY = 'oauthAccessToken'

export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [loginState, setLoginState] = useState<LoginResult | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const auth = useAuth()

  /**
   * Sign in to the UI based on the result of signing in to the API.
   * Internal and OAuth sign-in are a little different. With OAuth sign-in, we get the token from B2C and use that for
   * an API call, so the API token must already be set. However, with internal sign-in, we get the token back from the
   * unauthedLogin API call. Therefore, unless unauthedLogin has a post-condition that the API token will be set, we
   * need to set it now.
   */
  const loginUser = (loginResult: LoginResult, accessToken: string) => {
    setLoginState(loginResult)
    mixpanel.identify(loginResult.user.id)
    localStorage.setItem(OAUTH_ACCESS_TOKEN_KEY, accessToken)
  }

  const loginUserInternal = (loginResult: LoginResult) => {
    setLoginState(loginResult)
    mixpanel.identify(loginResult.user.id)
    localStorage.setItem(INTERNAL_LOGIN_TOKEN_KEY, loginResult.user.token)
  }

  /** Sign out of the UI. Does not invalidate any tokens, but maybe it should... */
  const logoutUser = async () => {
    mixpanel.reset()
    localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
    localStorage.removeItem(OAUTH_ACCESS_TOKEN_KEY)
    await Api.logout()
    if (!envVars.unauthedLogin) {
      // eslint-disable-next-line camelcase
      auth.signoutRedirect({ post_logout_redirect_uri: window.location.origin })
    } else {
      window.location.href = '/'
    }
  }

  /** updates a single enrollee in the list of enrollees -- the enrollee object should contain an updated task list */
  function updateEnrollee(enrollee: Enrollee, updateWithoutRerender = false): Promise<void> {
    if (updateWithoutRerender && loginState) {
      // update the underlying value, but don't call setLoginState, so no refresh
      // this should obviously be used with great care
      const matchIndex = loginState.enrollees.findIndex(exEnrollee => exEnrollee.shortcode === enrollee.shortcode)
      loginState.enrollees[matchIndex] = enrollee
    } else {
      setLoginState(oldState => {
        if (oldState == null) {
          return oldState
        }
        const updatedEnrollees = oldState.enrollees.filter(exEnrollee => exEnrollee.shortcode != enrollee.shortcode)
        updatedEnrollees.push(enrollee)
        return {
          ...oldState,
          enrollees: updatedEnrollees
        }
      })
    }
    return new Promise(resolve => {
      window.setTimeout(resolve, 0)
    })
  }

  function updateProfile(profile: Profile, updateWithoutRerender = false): Promise<void> {
    const isPrimaryUser = (
      loginState?.ppUsers.find(ppUser => ppUser.profileId === profile.id)?.participantUserId === loginState?.user?.id
    )

    if (updateWithoutRerender && loginState) {
      // update the underlying value, but don't call setLoginState, so no refresh
      // this should obviously be used with great care
      if (isPrimaryUser) {
        loginState.profile = profile
      }
      loginState?.enrollees.forEach(enrollee => {
        if (enrollee.profileId === profile.id) {
          enrollee.profile = profile
        }
      })
    } else {
      setLoginState(oldState => {
        if (oldState == null) {
          return oldState
        }
        return {
          ...oldState,
          enrollees: oldState.enrollees.map(enrollee => {
            if (enrollee.profileId === profile.id) {
              enrollee.profile = profile
            }
            return enrollee
          }),
          profile
        }
      })
    }
    return new Promise(resolve => {
      window.setTimeout(resolve, 0)
    })
  }

  const refreshLoginState = async () => {
    const oauthAccessToken = localStorage.getItem(OAUTH_ACCESS_TOKEN_KEY)
    const internalLogintoken = localStorage.getItem(INTERNAL_LOGIN_TOKEN_KEY)
    if (oauthAccessToken) {
      try {
        const loginResult = await Api.refreshLogin(oauthAccessToken)
        loginUser(loginResult, oauthAccessToken)
        setIsLoading(false)
      } catch (e) {
        setIsLoading(false)
        localStorage.removeItem(OAUTH_ACCESS_TOKEN_KEY)
      }
    } else if (internalLogintoken) {
      try {
        const loginResult = await Api.unauthedRefreshLogin(internalLogintoken)
        loginUserInternal(loginResult)
        setIsLoading(false)
      } catch (e) {
        setIsLoading(false)
        localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
      }
    } else {
      setIsLoading(false)
    }
  }

  const userContext: UserContextT = {
    user: loginState ? loginState.user : null,
    enrollees: loginState ? loginState.enrollees : [],
    proxyRelations: loginState ? loginState.proxyRelations : [],
    ppUsers: loginState?.ppUsers ? loginState.ppUsers : [],
    profile: loginState?.profile,
    loginUser,
    loginUserInternal,
    logoutUser,
    updateEnrollee,
    updateProfile,
    refreshLoginState
  }

  useEffect(() => {
    auth.events.addUserLoaded(user => {
      Api.setBearerToken(user.access_token)
      localStorage.setItem(OAUTH_ACCESS_TOKEN_KEY, user.access_token)
    })

    // Recover state for a signed-in user (internal) that we might have lost due to a full page load
    refreshLoginState()
  }, [])

  return (
    <UserContext.Provider value={userContext}>
      {isLoading
        ? <PageLoadingIndicator />
        : children}
    </UserContext.Provider>
  )
}
