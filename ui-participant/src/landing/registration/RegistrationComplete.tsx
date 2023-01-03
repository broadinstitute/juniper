import React from 'react'
import { Link } from 'react-router-dom'

/** Placeholder page for after reigstration is complete -- this will be obsoleted by B2C */
export default function RegistrationComplete() {
  return <div className="text-center">
    <h3>Thanks</h3>
    <p>Your registration is complete</p>

    <p><Link className="btn btn-primary" to={'/login'}>Login</Link></p>
  </div>
}
