import React, { useContext, useEffect, useState } from 'react'
import LoadingSpinner from '../util/LoadingSpinner'
import Api from 'api/api'
import { AdminUser } from 'api/adminUser'
import { useAuth } from 'react-oidc-context'


export type UserContextT = {
  user: AdminUser | null,
  loginUser: (adminUser: AdminUser) => void,
  loginUserUnauthed: (adminUser: AdminUser) => void,
  logoutUser: () => void
}

/** current user object context */
export const UserContext = React.createContext<UserContextT>({
  user: null,
  loginUser: () => { throw new Error('context not yet initialized') },
  loginUserUnauthed: () => { throw new Error('context not yet initialized') },
  logoutUser: () =>  { throw new Error('context not yet initialized') }
})
const INTERNAL_LOGIN_TOKEN_KEY = 'internalLoginToken'
const OAUTH_ACCRESS_TOKEN_KEY = 'oauthAccessToken'

export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [userState, setUserState] = useState<AdminUser | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const auth = useAuth()

  /**
   * Sign in to the UI based on the result of signing in to the API.
   * Internal and OAuth sign-in are a little different. With OAuth sign-in, we get the token from B2C and use that for
   * an API call, so the API token must already be set. However, with internal sign-in, we get the token back from the
   * unauthedLogin API call. Therefore, unless unauthedLogin has a post-condition that the API token will be set, we
   * need to set it now.
   */
  const loginUser = (adminUser: AdminUser) => {
    setUserState(adminUser)
    Api.setBearerToken(adminUser.token)
    localStorage.setItem(OAUTH_ACCRESS_TOKEN_KEY, adminUser.token)
  }

  const loginUserUnauthed = (adminUser: AdminUser) => {
    Api.setBearerToken(adminUser.token)
    setUserState(adminUser)
    localStorage.setItem(INTERNAL_LOGIN_TOKEN_KEY, adminUser.token)
  }

  const logoutUser = () => {
    setUserState(null)
    Api.setBearerToken(null)
    const oauthAccessToken = localStorage.getItem(OAUTH_ACCRESS_TOKEN_KEY)
    localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
    localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
    if (oauthAccessToken) {
      auth.signoutRedirect()
    } else {
      window.location.href = '/'
    }
  }

  const userContext: UserContextT = {
    user: userState,
    loginUser,
    loginUserUnauthed,
    logoutUser
  }

  useEffect(() => {
    auth.events.addUserLoaded(user => {
      const token = user.access_token as string
      Api.setBearerToken(token)
      Api.tokenLogin(token).then(user => {
        user.token = token
        loginUser(user)
        setIsLoading(false)
      }).catch(() => {
        alert('We could not find an authorized user matching that login')
      })
    })

    const oauthAccessToken = localStorage.getItem(OAUTH_ACCRESS_TOKEN_KEY)
    const internalLogintoken = localStorage.getItem(INTERNAL_LOGIN_TOKEN_KEY)
    if (oauthAccessToken) {
      Api.setBearerToken(oauthAccessToken)
      Api.refreshLogin(oauthAccessToken).then(loginResult => {
        loginResult.token = oauthAccessToken
        loginUser(loginResult)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
      })
    } else if (internalLogintoken) {
      Api.setBearerToken(internalLogintoken)
      Api.refreshUnauthedLogin(internalLogintoken).then(loginResult => {
        loginUserUnauthed(loginResult)
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
      <LoadingSpinner isLoading={isLoading}>
        { children }
      </LoadingSpinner>
    </UserContext.Provider>
  )
}

/** true iff the logged in user is a superuser */
export const isSuperuser = () => {
  return !!useUser().user?.superuser
}

/**
 * true if the user has the permission for the associated portal.  We should probably refactor this
 * and the underlying endpoint to work with portal shortcodes rather than portalIds for ease of use.
 * We can tackle that when this gets updated to support study-level permissions
 */
export const userHasPermission = (user: AdminUser | null, portalId: string, permission: string): boolean => {
  if (!user) {
    return false
  }
  return user.superuser || (user.portalPermissions[portalId] && user.portalPermissions[portalId].includes(permission))
}
