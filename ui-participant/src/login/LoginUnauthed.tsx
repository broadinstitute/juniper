import React, { SyntheticEvent, useState } from 'react'
import Api, { LoginResult } from 'api/api'
import { useUser } from 'providers/UserProvider'

/** component for showing a login dialog that hides other content on the page */
export default function LoginUnauthed() {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)
  const { loginUser } = useUser()

  /** log in with just an email, ignoring auth */
  function unauthedLogin(event: SyntheticEvent) {
    event.preventDefault()
    Api.unauthedLogin(emailAddress).then((result: LoginResult) => {
      loginUser(result, result.user.token)
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
        <div className="mb-3">
          <label htmlFor="inputLoginEmail" className="form-label">Email address</label>
          <input type="email" className="form-control" id="inputLoginEmail" aria-describedby="emailHelp"
            value={emailAddress}
            onChange={event => setEmailAddress(event.target.value)}/>
          <div id="emailHelp" className="form-text">development login only</div>
        </div>
        <button type="submit" className="btn btn-primary w-100">Login</button>
        {isError && <span className="text-danger">Login failed</span>}
      </form>
    </div>
  </div>
}
