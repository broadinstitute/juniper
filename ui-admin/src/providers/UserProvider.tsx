import React, { useEffect, useState } from 'react'
import Login from '../login/Login'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { AdminUser } from 'api/api'

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
  loginUser: (user: AdminUser) => { throw new Error('context not yet initialized') },
  logoutUser: () =>  { throw new Error('context not yet initialized') }
})
const STORAGE_TOKEN_PROP = 'loginToken'


export default function UserProvider({  children }: { children: any}) {
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
      <LoadingSpinner isLoading={isLoading}>
        { children }
        { userState.isAnonymous && <Login loginUser={loginUser}/>}
      </LoadingSpinner>
    </UserContext.Provider>
  )
}
