import React from 'react'
import Navbar from '../Navbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation } from '@fortawesome/free-solid-svg-icons'

/**
 * Displays when there is an error from b2c.
 */
export default function AuthError() {
  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <Navbar aria-label="Primary"/>
      <main className="flex-grow-1 p-2 d-flex flex-column justify-content-center">
        <div className="fs-1 fw-bold d-flex justify-content-center">
          <div>
            <FontAwesomeIcon className="me-2" icon={faCircleExclamation}/>
            <span>Oops!</span>
          </div>
        </div>
        <div className="fs-2 fw-light d-flex justify-content-center text-center">
            Something went wrong with our authentication service. Please try again later.
        </div>
      </main>
    </div>
  )
}
