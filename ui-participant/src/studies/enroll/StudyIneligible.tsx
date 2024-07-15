import React from 'react'
import { Link } from 'react-router-dom'

import { Portal } from 'api/api'
import {
  MailingListForm,
  useI18n
} from '@juniper/ui-core'

type StudyIneligibleProps = {
  portal: Portal
  studyName: string
}

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible(props: StudyIneligibleProps) {
  const { portal, studyName } = props

  const { i18n } = useI18n()
  return (
    <div className="flex-grow-1" style={{ background: '#f2f2f2' }}>
      <div className="container col-md-6 mt-5">
        <h1 className="h3 mb-3">{i18n('ineligibleThankYou', { substitutions: { studyName } })}</h1>
        <p>
          {i18n('ineligibleDescription', { substitutions: { studyName } })}
        </p>
        <div className="card mt-4">
          <div className="card-body py-5">
            <MailingListForm
              title={i18n('ineligibleStayInformed')}
              body={<p>
                {i18n('ineligibleProvideYourEmail')}
              </p>}/>
          </div>
        </div>
        <p className="text-center mt-3">
          <Link to="/">
            {i18n('backToPortalHomepage', { substitutions: { portalName: portal.name } })}
          </Link>
        </p>
      </div>
    </div>
  )
}
