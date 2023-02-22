import React, { useContext, useEffect, useState } from 'react'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { AdminUser } from 'api/api'
import { AuthProvider, useAuth } from 'react-oidc-context'
import { useConfig } from 'providers/ConfigProvider'
import { getOidcConfig } from 'authConfig'

export type User = {
    accessToken: string | null,
    isAnonymous: boolean,
    email: string | null
}

const anonymousUser: User = {
  accessToken: null,
  isAnonymous: true,
  email: null
}

export type UserContextT = {
  user: User,
  loginUser: (user: AdminUser) => void,
  logoutUser: () => void
}

/** current user object context */
const UserContext = React.createContext<UserContextT>({
  user: anonymousUser,
  loginUser: () => { throw new Error('context not yet initialized') },
  logoutUser: () =>  { throw new Error('context not yet initialized') }
})
const STORAGE_TOKEN_PROP = 'loginToken'

export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [userState, setUserState] = useState<User>(anonymousUser)
  const [isLoading, setIsLoading] = useState(true)
  const auth = useAuth()

  const loginUser = (user: AdminUser) => {
    setUserState({
      email: user.username,
      accessToken: user.token,
      isAnonymous: false
    })
    Api.setBearerToken(user.token)
    localStorage.setItem(STORAGE_TOKEN_PROP, user.token)
  }

  const logoutUser = () => {
    setUserState(anonymousUser)
    Api.setBearerToken(null)
    localStorage.removeItem(STORAGE_TOKEN_PROP)
    window.location.href = '/'
  }

  const userContext: UserContextT = {
    user: userState,
    loginUser,
    logoutUser
  }

  useEffect(() => {
    auth.events.addUserLoaded(user => {
      console.log(user)
      const adminUser = {
        username: user?.profile?.email as string,
        token: user.id_token as string
      }
      loginUser(adminUser)
    })

    const token = localStorage.getItem(STORAGE_TOKEN_PROP)
    if (token) {
      Api.tokenLogin(token).then(user => {
        loginUser(user)
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
        { children }
      </LoadingSpinner>
    </UserContext.Provider>
  )
}
