import React, { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { useUser } from 'providers/UserProvider'
import Login from 'login/Login'
import LoginUnauthed from './LoginUnauthed'

/* Inspired by https://www.robinwieruch.de/react-router-private-routes/ */
// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const ProtectedRoute = ({ children }: { children?: ReactNode }) => {
  const { user } = useUser()

  const loginComponent = import.meta.env.VITE_UNAUTHED_LOGIN ? <LoginUnauthed/> : <Login/>

  if (!user) {
    return loginComponent
  }

  return children ? <>{children}</> : <Outlet/>
}
