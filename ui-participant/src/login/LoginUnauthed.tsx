import React, { SyntheticEvent, useState } from 'react'
import Api from 'api/api'
import { useUser } from 'providers/UserProvider'
import { findDefaultEnrollmentStudy } from './RedirectFromOAuth'
import { enrollCurrentUserInStudy } from '../util/enrolleeUtils'
import { useNavigate } from 'react-router-dom'
import { usePortalEnv } from '../providers/PortalProvider'

/** component for showing a login dialog that hides other content on the page */
export default function LoginUnauthed() {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)
  const { loginUser, refreshLoginState } = useUser()
  const navigate = useNavigate()
  const { portal } = usePortalEnv()

  const defaultEnrollStudy = findDefaultEnrollmentStudy(null, portal.portalStudies)

  /** log in with just an email, ignoring auth */
  const unauthedLogin = async (event: SyntheticEvent) => {
    event.preventDefault()
    setIsError(false)
    try {
      const loginResult = await Api.unauthedLogin(emailAddress)
      loginUser(loginResult, loginResult.user.token)

      // Enroll in the default study if not already enrolled in any study
      if (defaultEnrollStudy && !loginResult.enrollees.length) {
        const hubUpdate = await enrollCurrentUserInStudy(defaultEnrollStudy.shortcode, null, refreshLoginState)
        navigate('/hub', { replace: true, state: hubUpdate })
      }
    } catch (e) {
      setIsError(true)
    }
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
