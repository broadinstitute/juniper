import React from 'react'
import {StudyEnrollContext} from "./StudyEnrollRouter";

/** Shows a page indicating the person is ineligible to register for the portal */
export default function StudyIneligible({enrollContext}: { enrollContext: StudyEnrollContext }) {
  return <div className="container text-center mt-5">
    <p>
      You are not currently eligible to participate in this study.
    </p>
  </div>
}
