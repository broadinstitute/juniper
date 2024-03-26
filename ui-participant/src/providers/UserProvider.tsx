import React, { useContext, useEffect, useState } from 'react'
import { useAuth } from 'react-oidc-context'
import { useNavigate } from 'react-router-dom'
import Api, {
  Enrollee,
  EnrolleeRelationDto,
  LoginResult,
  ParticipantUser,
  PortalParticipantUser,
  Profile
} from 'api/api'
import { PageLoadingIndicator } from 'util/LoadingSpinner'

export type User = ParticipantUser & {
  isAnonymous: boolean
}

const anonymousUser: User = {
  token: '',
  isAnonymous: true,
  username: 'anonymous'
}

export type UserContextT = {
  user: User,
  enrollees: Enrollee[],
  relations: EnrolleeRelationDto[], // this data is included to speed initial hub rendering.  it is NOT kept current
  activeEnrollee?: Enrollee,
  activeEnrolleeProfile?: Profile,
  ppUser?: PortalParticipantUser,
  profile?: Profile,
  loginUser: (result: LoginResult, accessToken: string) => void,
  loginUserInternal: (result: LoginResult) => void,
  logoutUser: () => void,
  updateEnrollee: (enrollee: Enrollee, updateWtihoutRerender?: boolean) => Promise<void>
  updateProfile: (profile: Profile, updateWithoutRerender?: boolean) => Promise<void>
  setActiveEnrollee: (enrollee: Enrollee) => void
  setActiveEnrolleeProfile: (profile: Profile) => void
}

/** current user object context */
const UserContext = React.createContext<UserContextT>({
  user: anonymousUser,
  enrollees: [],
  relations: [],
  activeEnrollee: undefined,
  activeEnrolleeProfile: undefined,
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
  setActiveEnrollee: () => {
    throw new Error('context not yet initialized')
  },
  setActiveEnrolleeProfile: () => {
    throw new Error('context not yet initialized')
  }
})
const INTERNAL_LOGIN_TOKEN_KEY = 'internalLoginToken'
const OAUTH_ACCRESS_TOKEN_KEY = 'oauthAccessToken'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [loginState, setLoginState] = useState<LoginResult | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [activeEnrollee, setActiveEnrollee] = useState<Enrollee | null>(null)
  const [activeEnrolleeProfile, setActiveEnrolleeProfile] = useState<Profile | null>(null)
  const auth = useAuth()
  const navigate = useNavigate()

  /**
   * Sign in to the UI based on the result of signing in to the API.
   * Internal and OAuth sign-in are a little different. With OAuth sign-in, we get the token from B2C and use that for
   * an API call, so the API token must already be set. However, with internal sign-in, we get the token back from the
   * unauthedLogin API call. Therefore, unless unauthedLogin has a post-condition that the API token will be set, we
   * need to set it now.
   */
  const loginUser = (loginResult: LoginResult, accessToken: string) => {
    setLoginState(loginResult)
    localStorage.setItem(OAUTH_ACCRESS_TOKEN_KEY, accessToken)
  }

  const loginUserInternal = (loginResult: LoginResult) => {
    setLoginState(loginResult)
    localStorage.setItem(INTERNAL_LOGIN_TOKEN_KEY, loginResult.user.token)
  }

  /** Sign out of the UI. Does not invalidate any tokens, but maybe it should... */
  const logoutUser = () => {
    localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
    localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
    if (process.env.REACT_APP_UNAUTHED_LOGIN) {
      Api.logout().then(() => {
        setLoginState(null)
        navigate('/')
      })
    } else {
      // eslint-disable-next-line camelcase
      auth.signoutRedirect({ post_logout_redirect_uri: window.location.origin })
    }
  }

  /** updates a single enrollee in the list of enrollees -- the enrollee object should contain an updated task list */
  function updateEnrollee(enrollee: Enrollee, updateWithoutRerender = false): Promise<void> {
    if (updateWithoutRerender && loginState) {
      // update the underlying value, but don't call setLoginState, so no refresh
      // this should obviously be used with great care
      const matchIndex = loginState.enrollees.findIndex(exEnrollee => exEnrollee.shortcode === enrollee.shortcode)
      if (matchIndex != -1) {
        loginState.enrollees[matchIndex] = enrollee
        setActiveEnrollee(enrollee)
        setActiveEnrolleeProfile(enrollee.profile)
      } else {
        const index = loginState.relations.findIndex(relation =>
          relation.relation.targetEnrollee.shortcode == enrollee.shortcode)
        if (index != -1) {
          loginState.relations[index].relation.targetEnrollee = enrollee
          setActiveEnrollee(enrollee)
          setActiveEnrolleeProfile(loginState.relations[index].profile)
        }
      }
    } else {
      setLoginState(oldState => {
        if (oldState == null) {
          return oldState
        }
        let updatedEnrollees = oldState.enrollees
        const updatedRelations = oldState.relations
        if (oldState.enrollees.findIndex(exEnrollee => exEnrollee.shortcode === enrollee.shortcode) != -1) {
          updatedEnrollees = oldState.enrollees.filter(exEnrollee => exEnrollee.shortcode != enrollee.shortcode)
          updatedEnrollees.push(enrollee)
          setActiveEnrollee(enrollee)
          setActiveEnrolleeProfile(enrollee.profile)
        } else {
          const index = oldState.relations.findIndex(relation =>
            relation.relation.targetEnrollee.shortcode == enrollee.shortcode)
          index != -1 ? updatedRelations[index].relation.targetEnrollee = enrollee : null
          setActiveEnrollee(enrollee)
          setActiveEnrolleeProfile(updatedRelations[index].profile)
        }
        return {
          ...oldState,
          enrollees: updatedEnrollees,
          relations: updatedRelations,
          activeEnrollee,
          activeEnrolleeProfile
        }
      })
    }
    return new Promise(resolve => {
      window.setTimeout(resolve, 0)
    })
  }

  function updateProfile(profile: Profile, updateWithoutRerender = false): Promise<void> {
    if (updateWithoutRerender && loginState) {
      // update the underlying value, but don't call setLoginState, so no refresh
      // this should obviously be used with great care
      loginState.profile = profile
    } else {
      setLoginState(oldState => {
        if (oldState == null) {
          return oldState
        }
        return {
          ...oldState,
          profile
        }
      })
    }
    return new Promise(resolve => {
      window.setTimeout(resolve, 0)
    })
  }

  const userContext: UserContextT = {
    user: loginState ? { ...loginState.user, isAnonymous: false } : anonymousUser,
    enrollees: loginState ? loginState.enrollees : [],
    relations: loginState ? loginState.relations : [],
    activeEnrollee: activeEnrollee || loginState?.enrollees[0] || undefined,
    activeEnrolleeProfile: activeEnrolleeProfile || loginState?.profile || undefined,
    ppUser: loginState?.ppUser,
    profile: loginState?.profile,
    loginUser,
    loginUserInternal,
    logoutUser,
    updateEnrollee,
    updateProfile,
    setActiveEnrollee,
    setActiveEnrolleeProfile
  }

  useEffect(() => {
    auth.events.addUserLoaded(user => {
      Api.setBearerToken(user.access_token)
      localStorage.setItem(OAUTH_ACCRESS_TOKEN_KEY, user.access_token)
    })

    // Recover state for a signed-in user (internal) that we might have lost due to a full page load
    const oauthAccessToken = localStorage.getItem(OAUTH_ACCRESS_TOKEN_KEY)
    const internalLogintoken = localStorage.getItem(INTERNAL_LOGIN_TOKEN_KEY)
    if (oauthAccessToken) {
      Api.refreshLogin(oauthAccessToken).then(loginResult => {
        loginUser(loginResult, oauthAccessToken)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
      })
    } else if (internalLogintoken) {
      Api.unauthedRefreshLogin(internalLogintoken).then(loginResult => {
        loginUserInternal(loginResult)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
      })
    } else {
      setIsLoading(false)
    }
  }, [])

  return (
    <UserContext.Provider value={userContext}>
      {isLoading
        ? <PageLoadingIndicator />
        : children}
    </UserContext.Provider>
  )
}
