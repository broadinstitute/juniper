import React from 'react'
import Api from 'api/api'
import {
  paramsFromContext,
  StudyEnvContextT
} from '../../StudyEnvironmentRouter'
import ParticipantNotesView from './ParticipantNotesView'
import {
  dateToDefaultString,
  Enrollee,
  EnrolleeRelation,
  instantToDefaultString,
  ParticipantUser,
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
import { Link } from 'react-router-dom'
import { useUser } from 'user/UserProvider'
import { UsernameEditor } from 'study/participants/enrolleeView/UsernameEditor'

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
  const { user } = useUser()

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage
  const proxyForRelations = relations.filter(relation =>
    relation.relationshipType === 'PROXY' && relation.enrolleeId === enrollee.id)
  const proxyRelations = relations.filter(relation =>
    relation.relationshipType === 'PROXY' && relation.enrolleeId !== enrollee.id)
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
        {user?.superuser && participantUser
          ? <UsernameEditor participantUser={participantUser} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
          : <InfoCardValue
            title={'Username'}
            condensed={true}
            values={[participantUser?.username || '']}
          />}
        <InfoCardValue
          title={'Research ID'}
          condensed={true}
          values={[enrollee.researchId || 'n/a']}
          info={
            'Research IDs are never sent to the participant. They are safe to use ' +
            'in published research without risk of reidentification.'
          }
        />
        <InfoCardValue
          title={'Last login'}
          condensed={true}
          values={[instantToDefaultString(participantUser?.portalParticipantUsers?.[0]?.lastLogin)]}
        />
        <InfoCardValue
          title={'Enrolled'}
          condensed={true}
          values={[instantToDefaultString(enrollee.createdAt)]}/>
        {!enrollee.subject &&
            <InfoCardValue
              title={'Account Type'}
              condensed={true}
              values={['Non-subject (proxy only)']}
            />}
      </InfoCardBody>
    </InfoCard>

    {isLoadingRelations && <LoadingSpinner/>}
    { proxyRelations.length > 0 && <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={'Proxies'}/>
      </InfoCardHeader>
      <InfoCardBody>
        <ul>
          {proxyRelations.map(relation => <li className="mt-2" key={relation.id}>
            <span className="fw-bold me-3">{formatName(relation.enrollee!.profile) }</span>
            <span className="me-3">{relation.enrollee?.profile?.contactEmail}</span>
              (<Link to={studyEnvParticipantPath(paramsFromContext(studyEnvContext), relation.enrolleeId)}>
              {relation.enrollee!.shortcode}
            </Link>)
          </li>
          )}
        </ul>
      </InfoCardBody>
    </InfoCard>
    }
    { proxyForRelations.length > 0 && <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={'Proxy for'}/>
      </InfoCardHeader>
      <InfoCardBody>
        <ul>
          {proxyForRelations.map(relation => <li className="mt-2" key={relation.id}>
            <span className="fw-bold me-3">{formatName(relation.targetEnrollee!.profile) }</span>
              (<Link to={studyEnvParticipantPath(paramsFromContext(studyEnvContext),
                  relation.targetEnrollee!.shortcode)}>
              {relation.targetEnrollee!.shortcode}
            </Link>)
          </li>
          )}
        </ul>
      </InfoCardBody>
    </InfoCard>
    }
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

const formatName = (profile: Profile | undefined): React.ReactNode => {
  if (!profile || (!profile.givenName && !profile.familyName)) {
    return <span className="text-muted fst-italic">(name not provided)</span>
  }
  return `${profile.givenName || ''} ${profile.familyName || ''}`.trim()
}
