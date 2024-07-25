import React, { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { useUser } from 'user/UserProvider'
import Login from 'login/Login'

/* Inspired by https://www.robinwieruch.de/react-router-private-routes/ */
export const ProtectedRoute = ({ children }: { children?: ReactNode }) => {
  const { user } = useUser()

  if (!user) {
    return <Login/>
  }

  return children ? <>{ children }</> : <Outlet />
}
