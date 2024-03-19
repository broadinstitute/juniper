import React, { useEffect, useState } from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import Api, { Enrollee, Portal, Study } from '../api/api'
import { isTaskActive } from './TaskLink'
import { DocumentTitle } from 'util/DocumentTitle'

import { HubMessageAlert, HubUpdateMessage, useHubUpdate } from './hubUpdates'
import { ParticipantDashboardAlert, alertDefaults } from '@juniper/ui-core'
import KitBanner from './kit/KitBanner'
import StudyResearchTasks from './StudyResearchTasks'
import OutreachTasks from './OutreachTasks'


/** renders the logged-in hub page */
export default function HubPage() {
  const { portal, portalEnv } = usePortalEnv()
  const { enrollees, activeEnrollee, relations } = useUser()
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
  console.log('activeEnrollee', activeEnrollee)


  return (
    <>
      <DocumentTitle title="Dashboard" />
      <div
        className="hub-dashboard-background flex-grow-1"
        style={{ background: 'linear-gradient(270deg, #D5ADCC 0%, #E5D7C3 100%' }}
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
          {activeEnrollee && <StudySection key={activeEnrollee.id} enrollee={activeEnrollee} portal={portal} /> }
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
  enrollee: Enrollee
  portal: Portal
}

const StudySection = (props: StudySectionProps) => {
  const { enrollee, portal } = props

  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study

  return (
    <>
      <h1 className="mb-4">{matchedStudy.name}</h1>
      {enrollee.kitRequests.length > 0 && <KitBanner kitRequests={enrollee.kitRequests} />}
      <StudyResearchTasks enrollee={enrollee} studyShortcode={matchedStudy.shortcode}
        participantTasks={enrollee.participantTasks} />
    </>
  )
}

