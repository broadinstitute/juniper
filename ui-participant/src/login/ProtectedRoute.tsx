import React, { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { useUser } from 'providers/UserProvider'
import Login from 'login/Login'

/* Inspired by https://www.robinwieruch.de/react-router-private-routes/ */
export const ProtectedRoute = ({ children }: { children?: ReactNode }) => {
  const { user } = useUser()

  if (user.isAnonymous) {
    return <Login/>
  }

  return children ? <>{children}</> : <Outlet/>
}
