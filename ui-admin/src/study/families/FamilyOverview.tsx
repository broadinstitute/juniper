import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { Family } from '@juniper/ui-core'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardRow,
  InfoCardTitle
} from 'components/InfoCard'
import { FamilyStructureTable } from 'study/families/FamilyStructureTable'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'

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
        <InfoCardTitle title={`Family`}/>
      </InfoCardHeader>
      <div className={'w-100'}>
        <FamilyStructureTable
          family={family}
          studyEnvContext={studyEnvContext}/>
      </div>
    </InfoCard>
  </div>
}
