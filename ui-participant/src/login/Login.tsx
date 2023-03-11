import React, { useEffect } from 'react'
import { useAuth } from 'react-oidc-context'

/** component for showing a login dialog that hides other content on the page */
function Login() {
  const auth = useAuth()

  const signIn = async () => {
    auth.signinRedirect()
  }

  useEffect(() => {
    signIn()
  }, [])

  return <div className="Login">
    <div className="App-splash-background"/>
    <div className="Login-overlay h-100 w-100" style={{
      top: 0,
      left: 0,
      position: 'fixed',
      zIndex: 1,
      opacity: 0.4,
      backgroundColor: '#888'
    }}></div>
  </div>
}

export default Login
