import React, { useEffect, useState } from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import Api, { Enrollee, EnrolleeRelation, Portal, Profile, Study } from '../api/api'
import { isTaskActive } from './TaskLink'
import { DocumentTitle } from 'util/DocumentTitle'

import { HubMessageAlert, HubUpdateMessage, useHubUpdate } from './hubUpdates'
import { alertDefaults, ParticipantDashboardAlert, useI18n } from '@juniper/ui-core'
import KitBanner from './kit/KitBanner'
import StudyResearchTasks from './StudyResearchTasks'
import OutreachTasks from './OutreachTasks'
import HubPageParticipantSelector from './HubPageParticipantSelector'


/** renders the logged-in hub page */
export default function HubPage() {
  const { portal, portalEnv } = usePortalEnv()

  const {
    enrollees,
    relations
  } = useUser()


  const [activeEnrollee, setActiveEnrollee] = useState<Enrollee | undefined>(findDefaultEnrollee(enrollees, relations))

  const { i18n } = useI18n()

  const [noActivitiesAlert, setNoActivitiesAlert] = useState<ParticipantDashboardAlert>()

  useEffect(() => {
    loadDashboardAlerts()
  }, [])

  const loadDashboardAlerts = async () => {
    if (!portalEnv) { return }
    const alerts = await Api.getPortalEnvDashboardAlerts(portal.shortcode, portalEnv.environmentName)
    setNoActivitiesAlert({
      ...alertDefaults['NO_ACTIVITIES_REMAIN'],
      ...alerts.find(msg => msg.trigger === 'NO_ACTIVITIES_REMAIN')
    })
  }

  const hubUpdate = useHubUpdate()
  const [showMessage, setShowMessage] = useState(true)
  const hasActiveTasks = activeEnrollee?.participantTasks.some(isTaskActive)

  return (
    <>
      <DocumentTitle title={i18n('navbarDashboard')} />
      <div
        className="hub-dashboard-background flex-grow-1 mb-2"
        style={{ background: 'var(--dashboard-background-color)' }}
      >
        {!hasActiveTasks && noActivitiesAlert && <HubMessageAlert
          message={{
            title: noActivitiesAlert.title,
            detail: noActivitiesAlert.detail,
            type: noActivitiesAlert.type
          } as HubUpdateMessage}
          className="mx-1 mx-md-auto my-1 my-md-5 shadow-sm"
          role="alert"
          style={{ maxWidth: 768 }}
        /> }
        {!!hubUpdate?.message && showMessage && (
          <HubMessageAlert
            message={hubUpdate.message}
            className="mx-1 mx-md-auto my-1 my-md-5 shadow-sm"
            role="alert"
            style={{ maxWidth: 768 }}
            onDismiss={() => {
              setShowMessage(false)
            }}
          />
        )}

        <main
          className="hub-dashboard py-4 px-2 px-md-5 my-md-4 mx-auto shadow-sm"
          style={{ background: '#fff', maxWidth: 768 }}
        >
          {relations.length > 0 && <HubPageParticipantSelector setActiveEnrollee={setActiveEnrollee}/>}
          {activeEnrollee && <StudySection
            key={activeEnrollee.id}
            enrollee={activeEnrollee}
            portal={portal}
            relations={relations}
            profile={activeEnrollee?.profile}/>}
        </main>
        <div className="hub-dashboard mx-auto"
          style={{ maxWidth: 768 }}>
          <OutreachTasks enrollees={enrollees} studies={portal.portalStudies.map(pStudy => pStudy.study)}/>
        </div>
      </div>
    </>
  )
}

type StudySectionProps = {
  enrollee: Enrollee,
  relations: EnrolleeRelation[]
  portal: Portal,
  profile: Profile
}

const StudySection = (props: StudySectionProps) => {
  const {
    enrollee,
    portal,
    profile,
    relations
  } = props

  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study

  function getName(profile: Profile) {
    if (!profile || !profile.givenName || !profile.familyName) {
      return ''
    }
    return (profile && (`${profile.givenName} ${profile.familyName}`)) || ''
  }

  return (
    <>
      <h1 className="mb-4">{matchedStudy.name}</h1>
      {relations.length > 0 && <h4 className="mb-4">{getName(profile)}</h4>}
      {enrollee.kitRequests.length > 0 && <KitBanner kitRequests={enrollee.kitRequests} />}
      <StudyResearchTasks enrollee={enrollee} studyShortcode={matchedStudy.shortcode}
        participantTasks={enrollee.participantTasks} />
    </>
  )
}


const findDefaultEnrollee = (enrollees: Enrollee[], relations: EnrolleeRelation[]): Enrollee | undefined => {
  const foundEnrollee = enrollees.find(e => e.subject)
  if (foundEnrollee) {
    return foundEnrollee
  }

  const foundRelationEnrollee = relations.find(r => r.targetEnrollee.subject)
  if (foundRelationEnrollee) {
    return foundRelationEnrollee.targetEnrollee
  }
}
