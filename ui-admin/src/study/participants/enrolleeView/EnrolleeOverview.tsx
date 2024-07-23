import React from 'react'
import Api from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import ParticipantNotesView from './ParticipantNotesView'
import {
  dateToDefaultString,
  Enrollee,
  EnrolleeRelation,
  Profile
} from '@juniper/ui-core'
import KitRequests from '../KitRequests'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardTitle,
  InfoCardValue
} from 'components/InfoCard'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import Families from 'study/participants/Families'

/** Shows minimal identifying information, and then kits and notes */
export default function EnrolleeOverview({ enrollee, studyEnvContext, onUpdate }:
        {enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [relations, setRelations] = React.useState<EnrolleeRelation[]>([])

  const { isLoading: isLoadingRelations } = useLoadingEffect(async () => {
    const relations = await Api.findRelationsByTargetShortcode(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode)
    setRelations(relations)
  })

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  return <>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={'Basic Information'}/>
      </InfoCardHeader>
      <InfoCardBody>
        <InfoCardValue
          title={'Name'}
          values={[formatName(enrollee.profile)]}
        />
        <InfoCardValue
          title={'Birthdate'}
          values={[dateToDefaultString(enrollee.profile.birthDate) || '']}
        />
      </InfoCardBody>
    </InfoCard>

    {isLoadingRelations && <LoadingSpinner/>}
    {
      relations
        .filter(relation => relation.relationshipType === 'PROXY')
        .map(relation => {
          return <InfoCard key={relation.id}>
            <InfoCardHeader>
              <InfoCardTitle title={'Proxy'}/>
            </InfoCardHeader>
            <InfoCardBody>
              <InfoCardValue
                title={'Name'}
                values={
                  [formatName(relation.enrollee?.profile)]
                }
              />
              <InfoCardValue
                title={'Contact Email'}
                values={[relation.enrollee?.profile?.contactEmail || '']}
              />
            </InfoCardBody>
          </InfoCard>
        })}

    <div>
      <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
        studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
    </div>

    {
      familyLinkageEnabled && <div>
        <Families enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
      </div>
    }


    <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </>
}

const formatName = (profile: Profile | undefined) => {
  if (!profile) {
    return ''
  }
  return `${profile.givenName || ''} ${profile.familyName || ''}`.trim()
}
