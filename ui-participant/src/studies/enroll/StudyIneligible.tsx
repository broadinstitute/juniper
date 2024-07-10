import React from 'react'
import { Link } from 'react-router-dom'

import { Portal } from 'api/api'
import { MailingListForm } from '@juniper/ui-core'

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
        <h1 className="h3 mb-3">Thank you for your interest in {studyName}</h1>
        <p>
          Unfortunately, you do not meet the eligibility criteria to participate in {studyName} at this time.
        </p>
        <div className="card mt-4">
          <div className="card-body py-5">
            <MailingListForm
              title='Stay informed'
              body={<p>
                Provide your email below and we’ll keep you up-to-date on developments. You can unsubscribe at any time.
              </p>}/>
          </div>
        </div>
        <p className="text-center mt-3">
          <Link to="/">Back to {portal.name} homepage</Link>
        </p>
      </div>
    </div>
  )
}
