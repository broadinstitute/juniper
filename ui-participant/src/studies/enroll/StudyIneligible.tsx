import React from 'react'
import { Link } from 'react-router-dom'

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible() {
  return <div className="container text-center mt-5 ">
    <p>
      Unfortunately, your responses to our eligibility questions do not match the requirements
      for participation in the study. The study can only accept participants who meet all of these conditions
    </p>
    <p>
      Over time, we may be able to expand the project.
      If you join our mailing list, we can contact you with updates.
    </p>
    <p>
      <Link to="/">Back to study home</Link>
    </p>
  </div>
}
