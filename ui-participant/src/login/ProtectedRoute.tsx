import React, { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { useUser } from 'providers/UserProvider'
import Login from 'login/Login'
import LoginUnauthed from './LoginUnauthed'
import envVars from 'util/envVars'

/* Inspired by https://www.robinwieruch.de/react-router-private-routes/ */
export const ProtectedRoute = ({ children }: { children?: ReactNode }) => {
  const { user } = useUser()

  const loginComponent = envVars.unauthedLogin ? <LoginUnauthed/> : <Login/>

  if (!user) {
    return loginComponent
  }

  return children ? <>{children}</> : <Outlet/>
}
