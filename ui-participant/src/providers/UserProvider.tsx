import React, { useContext, useEffect, useState } from 'react'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { Enrollee, LoginResult, ParticipantUser } from 'api/api'
import { useAuth } from 'react-oidc-context'

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
  enrollees: Enrollee[],  // this data is included to speed initial hub rendering.  it is NOT kept current
  loginUser: (result: LoginResult) => void,
  logoutUser: () => void,
  updateEnrollee: (enrollee: Enrollee) => void
}

/** current user object context */
const UserContext = React.createContext<UserContextT>({
  user: anonymousUser,
  enrollees: [],
  loginUser: () => {
    throw new Error('context not yet initialized')
  },
  logoutUser: () => {
    throw new Error('context not yet initialized')
  },
  updateEnrollee: () => {
    throw new Error('context not yet initialized')
  }
})
const STORAGE_TOKEN_PROP = 'loginToken'

export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [loginState, setLoginState] = useState<LoginResult | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const auth = useAuth()

  /** Sign in to the UI based on the result of signing in to the API. */
  const loginUser = (loginResult: LoginResult) => {
    Api.setBearerToken(loginResult.user.token as string)
    setLoginState(loginResult)
    localStorage.setItem(STORAGE_TOKEN_PROP, loginResult.user.token)
  }

  /** Sign out of the UI. Does not invalidate any tokens, but maybe it should... */
  const logoutUser = () => {
    setLoginState(null)
    Api.setBearerToken(null)
    localStorage.removeItem(STORAGE_TOKEN_PROP)
  }

  /** updates a single enrollee in the list of enrollees -- the enrollee object should contain an updated task list */
  function updateEnrollee(enrollee: Enrollee) {
    setLoginState(oldState => {
      if (oldState == null) {
        return oldState
      }
      const updatedEnrollees = oldState.enrollees.filter(exEnrollee => exEnrollee.shortcode != enrollee.shortcode)
      updatedEnrollees.push(enrollee)
      return {
        user: oldState?.user,
        enrollees: updatedEnrollees
      }
    })
  }

  const userContext: UserContextT = {
    user: loginState ? { ...loginState.user, isAnonymous: false } : anonymousUser,
    enrollees: loginState ? loginState.enrollees : [],
    loginUser,
    logoutUser,
    updateEnrollee
  }

  useEffect(() => {
    // Add listener to auth context to process return from B2C sign-in
    // TODO: consider only doing this for redirect-from-oauth
    auth.events.addUserLoaded(user => {
      if (user?.id_token && !user.profile.newUser) {
        Api.tokenLogin(user.id_token).then(loginResult => {
          loginUser(loginResult)
          setIsLoading(false)
        })
      }
    })

    // Recover state for a signed-in user that we might have lost due to a full page load
    const token = localStorage.getItem(STORAGE_TOKEN_PROP)
    if (token) {
      Api.refreshLogin(token).then(loginResult => {
        loginUser(loginResult)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(STORAGE_TOKEN_PROP)
      })
    } else {
      setIsLoading(false)
    }
  }, [])

  return (
    <UserContext.Provider value={userContext}>
      <LoadingSpinner isLoading={isLoading}>
        {children}
      </LoadingSpinner>
    </UserContext.Provider>
  )
}
