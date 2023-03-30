import React from 'react'
import { Link } from 'react-router-dom'

import { Portal } from 'api/api'
import MailingListForm from 'landing/MailingListForm'

type StudyIneligibleProps = {
  portal: Portal
  studyName: string
}

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible(props: StudyIneligibleProps) {
  const { portal, studyName } = props
  return (
    <div className="flex-grow-1" style={{ background: '#f2f2f2' }}>
      <div className="container col-md-6 mt-5">
        <h1 className="h3 mb-3">Sorry, you are not eligible to enroll right now</h1>
        <p>
          The {studyName} study can only accept participants who meet all of the conditions.
        </p>
        <p>
          Over time, we may be able to expand the participation requirements of this project.
          If you join our mailing list below, we can contact you with updates.
        </p>
        <div className="card mt-4">
          <div className="card-body py-5">
            <MailingListForm/>
          </div>
        </div>
        <p className="text-center mt-3">
          <Link to="/">Back to {portal.name} homepage</Link>
        </p>
      </div>
    </div>
  )
}
