import React from "react"
import { Navigate, Outlet } from "react-router-dom";
import { useUser } from "user/UserProvider";
import Login from "login/Login";

type ProtectedRouteProps = {
  children?: any
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { user } = useUser()
  console.log('user:', user)

  if (user.isAnonymous) {
    return <Login/>
  }

  return children ? children : <Outlet />
}
