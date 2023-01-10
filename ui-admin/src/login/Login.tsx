import React, { SyntheticEvent, useState } from 'react'
import microsoftLogo from 'images/microsoft_logo.png'
import Api, { AdminUser } from 'api/api'
import { useUser } from 'user/UserProvider'
import { useAuth } from 'react-oidc-context'

/** component for showing a login dialog that hides other content on the page */
function Login() {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)
  const { loginUser } = useUser()
  const auth = useAuth()

  const signIn = async () => {
    const user = await auth.signinPopup()
    return user
  }

  /** log in with just an email, ignoring auth */
  function unauthedLogin(event: SyntheticEvent) {
    event.preventDefault()
    Api.unauthedLogin(emailAddress).then((adminUser: AdminUser) => {
      loginUser(adminUser)
    }).catch(() => {
      setIsError(true)
    })
  }

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
    <div className="Login-dialog position-absolute top-50 start-50 translate-middle bg-white p-4" style={{
      borderRadius: '10px',
      zIndex: 2
    }}>
      <form onSubmit={unauthedLogin}>
        <button type="button" className="btn btn-secondary" onClick={() => signIn()}>
          <img src={microsoftLogo}/> Login with Microsoft
        </button>
        <hr/>
        <div className="mb-3">
          <label htmlFor="inputLoginEmail" className="form-label">Email address</label>
          <input type="email" className="form-control" id="inputLoginEmail" aria-describedby="emailHelp"
            value={emailAddress}
            onChange={event => setEmailAddress(event.target.value)}/>
          <div id="emailHelp" className="form-text">development login only</div>
        </div>
        <button type="submit" className="btn btn-primary w-100">Login</button>
        { isError && <span className="text-danger">Login failed</span> }
      </form>
    </div>
  </div>
}

export default Login
