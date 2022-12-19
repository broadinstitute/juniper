import React, { useState } from 'react'
import './Login.css'
import microsoftLogo from 'images/microsoft_logo.png'
import Api, { AdminUser } from 'api/api'

function Login({ loginUser }: {loginUser: (user: AdminUser) => void}) {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)

  function unauthedLogin(event?: any) {
    event.preventDefault()
    Api.unauthedLogin(emailAddress).then((adminUser: AdminUser) => {
      loginUser(adminUser)
    }).catch(() => {
      setIsError(true)
    })
  }

  return <div className="Login">
    <div className="Login-overlay"></div>
    <div className="Login-dialog position-absolute top-50 start-50 translate-middle">
      <form onSubmit={unauthedLogin}>
        <button type="button" className="btn btn-secondary">
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
