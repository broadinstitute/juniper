import React from 'react'
import Api, { Enrollee, EnrolleeRelation, Profile } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import ParticipantNotesView from './ParticipantNotesView'
import { dateToDefaultString } from '@juniper/ui-core'
import KitRequests from '../KitRequests'
import { Card, CardBody, CardHeader, CardTitle, CardValueRow } from '../../../components/Card'
import { useLoadingEffect } from '../../../api/api-utils'

/** Shows minimal identifying information, and then kits and notes */
export default function EnrolleeOverview({ enrollee, studyEnvContext, onUpdate }:
        {enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [relations, setRelations] = React.useState<EnrolleeRelation[]>([])

  useLoadingEffect(async () => {
    const relations = await Api.findRelationsByTargetShortcode(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode)
    setRelations(relations)
  })

  return <div>
    <Card>
      <CardHeader>
        <CardTitle title={'Overview'}/>
      </CardHeader>
      <CardBody>
        <CardValueRow
          title={'Name'}
          values={[formatName(enrollee.profile)]}
        />
        <CardValueRow
          title={'Birthdate'}
          values={[dateToDefaultString(enrollee.profile.birthDate) || '']}
        />
      </CardBody>
    </Card>

    {
      relations
        .filter(relation => relation.relationshipType === 'PROXY')
        .map(relation => {
          return <Card>
            <CardHeader>
              <CardTitle title={'Proxy'}/>
            </CardHeader>
            <CardBody>
              <CardValueRow
                title={'Name'}
                values={
                  [formatName(relation.enrollee.profile)]
                }
              />
              <CardValueRow
                title={'Contact Email'}
                values={[relation.enrollee?.profile?.contactEmail || '']}
              />
            </CardBody>
          </Card>
        })}


    <div className="mb-5">
      <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
        studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
    </div>


    <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

const formatName = (profile: Profile) => {
  return `${profile.givenName || ''} ${profile.familyName || ''}`.trim()
}
