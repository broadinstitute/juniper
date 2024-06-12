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
  InfoCardTitle,
  InfoCardValue
} from 'components/InfoCard'
import { Link } from 'react-router-dom'
import { isEmpty } from 'lodash'

/**
 *
 */
export const FamilyOverview = (
  {
    family, studyEnvContext, onUpdate
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
    onUpdate: () => void
  }) => {
  return <div>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={`Family Overview`}/>
      </InfoCardHeader>
      <InfoCardBody>
        <InfoCardValue title={'Shortcode'} values={[family.shortcode]}/>
        {family.proband && <InfoCardRow title={'Proband'}>
          <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={family.proband}/>
        </InfoCardRow>}
      </InfoCardBody>
    </InfoCard>

    {family.members && family.members.length > 0 &&
        <InfoCard>
          <InfoCardHeader>
            <InfoCardTitle title={`Family Members`}/>
          </InfoCardHeader>
          <InfoCardBody>
            <InfoCardRow title={'Members'}>
              {family.members.map((enrollee, index) => <div className="w-100">
                <EnrolleeLink
                  key={index}
                  enrollee={enrollee}
                  studyEnvContext={studyEnvContext}
                />
              </div>)}
            </InfoCardRow>
          </InfoCardBody>
        </InfoCard>
    }
  </div>
}

const EnrolleeLink = ({ studyEnvContext, enrollee }: { studyEnvContext: StudyEnvContextT, enrollee: Enrollee }) => {
  const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`.trim()
  return <Link
    to={`${studyEnvContext.currentEnvPath}/participants/${enrollee.shortcode}`}>
    {isEmpty(name) ? enrollee.shortcode : `${name} (${enrollee.shortcode})`}
  </Link>
}
