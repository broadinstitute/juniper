import React from 'react'
import { Family } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import {
  familyPath,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import { getFamilyNameString } from 'util/familyUtils'
import { isEmpty } from 'lodash'

/**
 * Links to a family, similar to the EnrolleeLink component.
 */
export const FamilyLink = ({
  studyEnvContext,
  family
}: {
  studyEnvContext: StudyEnvContextT,
  family: Family
}) => {
  const path = `${familyPath(
    studyEnvContext.portal.shortcode,
    studyEnvContext.study.shortcode,
    studyEnvContext.currentEnv.environmentName
  )}/${family.id}`
  const familyNameString = getFamilyNameString(family)
  if (isEmpty(familyNameString)) {
    return <Link to={path}>
      {family.shortcode}
    </Link>
  }
  return <span>
    {familyNameString} Family <Link
      to={path}>
      <span className="fst-italic">({family.shortcode})</span>
    </Link>
  </span>
}
