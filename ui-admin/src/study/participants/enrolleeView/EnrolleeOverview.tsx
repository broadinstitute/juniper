import React from 'react'
import Api from 'api/api'
import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import ParticipantNotesView from './ParticipantNotesView'
import {
  dateToDefaultString,
  Enrollee,
  EnrolleeRelation, instantToDefaultString, ParticipantUser,
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
import { studyEnvParticipantPath } from '../ParticipantsRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faLink } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'

/** Shows minimal identifying information, and then kits and notes */
export default function EnrolleeOverview({ enrollee, studyEnvContext, onUpdate }:
        {enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [relations, setRelations] = React.useState<EnrolleeRelation[]>([])
  const [participantUser, setParticipantUser] = React.useState<ParticipantUser>()
  const { isLoading: isLoadingRelations } = useLoadingEffect(async () => {
    const [relations, participantUser] = await Promise.all([
      Api.findRelationsByShortcode(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode),
      Api.fetchParticipantUser(studyEnvContext.portal.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.participantUserId)
    ])
    setRelations(relations)
    setParticipantUser(participantUser)
  }, [enrollee.shortcode])

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  return <>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={'Basic Information'}/>
      </InfoCardHeader>
      <InfoCardBody>
        <InfoCardValue
          title={'Name'}
          condensed={true}
          values={[formatName(enrollee.profile)]}
        />
        <InfoCardValue
          title={'Birthdate'}
          condensed={true}
          values={[dateToDefaultString(enrollee.profile.birthDate) || '']}
        />
        <InfoCardValue
          title={'Username'}
          condensed={true}
          values={[participantUser?.username || '']}
        />
        <InfoCardValue
          title={'Last login'}
          condensed={true}
          values={[instantToDefaultString(participantUser?.portalParticipantUsers?.[0]?.lastLogin)]}
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
                  [<Link to={studyEnvParticipantPath(paramsFromContext(studyEnvContext), relation.enrolleeId)}>
                    {formatName(relation.enrollee?.profile)} <FontAwesomeIcon icon={faLink} className="ms-3" />
                  </Link>]
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
