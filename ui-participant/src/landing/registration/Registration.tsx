import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useReturnToStudy } from 'browserPersistentState'
import LoadingSpinner from 'util/LoadingSpinner'

/** Show the B2C participant registration page */
export default function Registration() {
  const auth = useAuth()
  const studyShortcode = useParams().studyShortcode || null
  const [, setReturnToStudy] = useReturnToStudy()

  const register = () => {
    // Remember study for when we come back from B2C,
    // at which point RedirectFromOAuth will complete the registration
    setReturnToStudy(studyShortcode)
    auth.signinRedirect({ extraQueryParams: { option: 'signup' } })
  }

  useEffect(() => {
    register()
  }, [])

  return <div>
    <LoadingSpinner/>
  </div>
}
