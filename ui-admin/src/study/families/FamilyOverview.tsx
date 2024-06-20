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
import { FamilyTreeTable } from 'study/families/FamilyTreeTable'

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
          <div className='row mt-2'>
            <div className="col">
              <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={family.proband}/>
            </div>
          </div>
        </InfoCardRow>}
      </InfoCardBody>
    </InfoCard>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={`Family Tree`}/>
      </InfoCardHeader>
      <div className={'w-100'}>
        <FamilyTreeTable
          family={family}
          studyEnvContext={studyEnvContext}/>
      </div>
    </InfoCard>
  </div>
}

/**
 *
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
