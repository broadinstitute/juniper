import React from 'react'
import {Link} from 'react-router-dom'
import {Portal} from 'api/api'
import MailingListForm from "../../landing/MailingListForm";

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible({portal}: { portal: Portal }) {
  return <div className="container col-md-6 mt-5">
    <h1 className="h3 mb-3">Sorry, you are not eligible to enroll right now</h1>
    <p>
      The study can only accept participants who meet all of the conditions
    </p>
    <p>
      Over time, we may be able to expand the participant requirements of this project.
      If you join our mailing list below, we can contact you with updates.
    </p>
    <MailingListForm portal={portal}/>
    <p className="text-center mt-3">
      <Link to="/">Back to homepage</Link>
    </p>
  </div>
}
