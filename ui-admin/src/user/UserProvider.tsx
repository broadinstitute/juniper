import React, {useContext, useEffect, useState} from 'react'
import Login from '../login/Login'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { AdminUser } from 'api/api'
import { EventType, PublicClientApplication } from "@azure/msal-browser"
import { msalConfig } from "authConfig"
import { MsalProvider } from "@azure/msal-react";
import {AuthenticationResult} from "@azure/msal-common";

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
export const UserContext = React.createContext<UserContextT>({
  user: anonymousUser,
  loginUser: () => { throw new Error('context not yet initialized') },
  logoutUser: () =>  { throw new Error('context not yet initialized') }
})
const STORAGE_TOKEN_PROP = 'loginToken'

export const useUser = () => React.useContext(UserContext)

export const msalInstance = new PublicClientApplication(msalConfig)

// msalInstance.addEventCallback((event) => {
//   console.log(event)
//   const payload = event.payload as AuthenticationResult
//   if (
//     (event.eventType === EventType.LOGIN_SUCCESS ||
//       event.eventType === EventType.ACQUIRE_TOKEN_SUCCESS ||
//       event.eventType === EventType.SSO_SILENT_SUCCESS) &&
//     payload?.account
//   ) {
//     msalInstance.setActiveAccount(payload?.account);
//   }
// })
// msalInstance.addEventCallback((event) => console.log(event))

/** Provider for the current logged-in user. */
export default function UserProvider({  children }: { children: React.ReactNode}) {
  const [userState, setUserState] = useState<User>(anonymousUser)
  const [isLoading, setIsLoading] = useState(true)

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

  const context: UserContextT = {
    user: userState,
    loginUser,
    logoutUser
  }

  useEffect(() => {
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
    <UserContext.Provider value={context}>
      <MsalProvider instance={msalInstance}>
        <LoadingSpinner isLoading={isLoading}>
          { children }
          {/*{ userState.isAnonymous && <Login loginUser={loginUser}/>}*/}
        </LoadingSpinner>
      </MsalProvider>
    </UserContext.Provider>
  )
}
