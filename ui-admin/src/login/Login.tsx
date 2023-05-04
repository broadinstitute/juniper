import React, { SyntheticEvent, useState } from 'react'
import microsoftLogo from 'images/microsoft_logo.png'
import googleLogo from 'images/googleLogo.png'
import Api, { AdminUser } from 'api/api'
import { useUser } from 'user/UserProvider'
import { useAuth } from 'react-oidc-context'
import { useConfig } from 'providers/ConfigProvider'
import { Link } from 'react-router-dom'

/** component for showing a login dialog that hides other content on the page */
function Login() {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)
  const [showDevLogin, setShowDevLogin] = useState(false)
  const { loginUserUnauthed } = useUser()
  const auth = useAuth()
  const config = useConfig()

  const signIn = async () => {
    const user = await auth.signinPopup()
    return user
  }
  // dev login is prevented in non-localhost envs by the apache proxy, so don't bother showing the UI for it
  const enableDevLogin = config.adminUiHostname.includes('localhost')

  /** log in with just an email, ignoring auth */
  function unauthedLogin(event: SyntheticEvent) {
    event.preventDefault()
    Api.unauthedLogin(emailAddress).then((adminUser: AdminUser) => {
      loginUserUnauthed(adminUser)
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
    <div className="Login-dialog position-absolute top-50 start-50 translate-middle p-4 text-white" style={{
      borderRadius: '10px',
      zIndex: 2,
      minWidth: '300px',
      backgroundColor: '#333F52'
    }}>
      <h1 className="h5 text-center mb-4">Juniper</h1>
      <button type="button" className="btn btn-primary border-white text-white
         fw-bold d-flex w-100 align-items-center justify-content-center fs-5 mb-3"
      style={{ backgroundColor: '#4e617e' }}
      onClick={() => signIn()}>
        Login <img className="ms-3" style={{ maxHeight: '1em' }} src={microsoftLogo} alt="Microsoft logo"/>
        <img className="ms-1" style={{ maxHeight: '1em' }} src={googleLogo} alt="Google logo"/>
      </button>
      {enableDevLogin && <form onSubmit={unauthedLogin} className="d-flex flex-column justify-content-center">
        <hr className="mt-2"/>
        <button type="button" className="btn btn-secondary text-white" onClick={() => setShowDevLogin(!showDevLogin)}>
          developer login
        </button>
        { showDevLogin && <div className="mb-3">
          <label className="form-label w-100">email
            <input type="email" className="form-control" id="inputLoginEmail" value={emailAddress}
              onChange={event => setEmailAddress(event.target.value)}/>
          </label>
          <button type="submit" className="btn btn-secondary-outline border-white text-white mt-2 w-100">Log in</button>
        </div> }
        { isError && <div className="text-danger text-center">Login failed</div> }
      </form> }
      <hr className="mt-2"/>
      <div className="text-center">
        <Link className="link-light" to="/terms">Terms of Use</Link> |{' '}
        <Link className="link-light" to="/privacy">Privacy Policy</Link>
      </div>
    </div>
  </div>
}

export default Login
