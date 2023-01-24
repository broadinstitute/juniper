import React from 'react'

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible() {
  return <div className="container text-center mt-5">
    <p>
      You are not currently eligible to participate in this study.
    </p>
    <p>
      Use the Mailing List link above if you would like to be notified about any new studies or other news.
    </p>
  </div>
}
