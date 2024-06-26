import React from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Enrollee } from '@juniper/ui-core'
import { isEmpty } from 'lodash'
import { Link } from 'react-router-dom'

/**
 * Renders a link to an enrollee, showing their name if available, otherwise just the shortcode
 */
export const EnrolleeLink = ({ studyEnvContext, enrollee }: {
  studyEnvContext: StudyEnvContextT,
  enrollee: Enrollee
}) => {
  const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`.trim()
  const path = `${studyEnvContext.currentEnvPath}/participants/${enrollee.shortcode}`
  if (isEmpty(name)) {
    return <Link to={path}>{enrollee.shortcode}</Link>
  }
  return <span>
    {name} <Link
      to={path}>
      <span className=" fst-italic">({enrollee.shortcode})</span>
    </Link>
  </span>
}
