import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardRow,
  InfoCardTitle
} from 'components/InfoCard'
import { Link } from 'react-router-dom'
import { isEmpty } from 'lodash'

/**
 * Overall information about a family.
 */
export const FamilyOverview = (
  {
    family, studyEnvContext
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
  }) => {
  return <div>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={`Family Overview`}/>
      </InfoCardHeader>
      <InfoCardBody>
        {family.proband && <InfoCardRow title={'Proband'}>
          <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={family.proband}/>
        </InfoCardRow>}
        {family.members && <InfoCardRow title={'Members'}>
          {family.members.map((enrollee, index) => <div className="w-100">
            <EnrolleeLink
              key={index}
              enrollee={enrollee}
              studyEnvContext={studyEnvContext}
            />
          </div>)}
        </InfoCardRow>}
      </InfoCardBody>
    </InfoCard>
  </div>
}

const EnrolleeLink = ({ studyEnvContext, enrollee }: { studyEnvContext: StudyEnvContextT, enrollee: Enrollee }) => {
  const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`.trim()
  const path = `${studyEnvContext.currentEnvPath}/participants/${enrollee.shortcode}`
  if (isEmpty(name)) {
    return <Link to={path}>{enrollee.shortcode}</Link>
  }
  return <span>
    {name} <Link
      to={path}>
      ({enrollee.shortcode})
    </Link>
  </span>
}
