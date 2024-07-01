import React from 'react'
import { Family } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { getFamilyNames } from 'util/familyUtils'
import { isEmpty } from 'lodash'

/**
 *
 */
export const FamilyLink = ({
  studyEnvContext,
  family
}: {
  studyEnvContext: StudyEnvContextT,
  family: Family
}) => {
  const path = `${studyEnvContext.currentEnvPath}/families/${family.shortcode}`
  const familyNames = getFamilyNames(family)
  if (isEmpty(familyNames)) {
    return <Link to={path}>
      {family.shortcode}
    </Link>
  }
  return <span>
    {familyNames} Family <Link
      to={path}>
      <span className=" fst-italic">({family.shortcode})</span>
    </Link>
  </span>
}
