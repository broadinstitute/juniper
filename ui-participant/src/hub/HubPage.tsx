import React, { useState } from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import { Enrollee, ParticipantTask, Portal, Study } from '../api/api'
import TaskLink, { getTaskPath, isTaskAccessible, isTaskActive } from './TaskLink'
import { Link, NavLink } from 'react-router-dom'
import { DocumentTitle } from 'util/DocumentTitle'
import { userHasJoinedPortalStudy } from 'util/enrolleeUtils'

import { HubMessageAlert, useHubUpdate } from './hubUpdates'
import { filterUnjoinableStudies } from '../Navbar'
import StudyResearchTasks from "./StudyResearchTasks";
import OutreachTasks from "./OutreachTasks";


/** renders the logged-in hub page */
export default function HubPage() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()

  const hubUpdate = useHubUpdate()
  const [showMessage, setShowMessage] = useState(true)

  const unjoinedStudies = filterUnjoinableStudies(portal.portalStudies)
    .filter(pStudy => !userHasJoinedPortalStudy(pStudy, enrollees))
  const hasUnjoinedStudies = unjoinedStudies.length > 0
  return (
    <>
      <DocumentTitle title="Dashboard" />
      <div
        className="hub-dashboard-background flex-grow-1"
        style={{ background: 'linear-gradient(270deg, #D5ADCC 0%, #E5D7C3 100%' }}
      >
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
          {enrollees.map(enrollee => <StudySection key={enrollee.id} enrollee={enrollee} portal={portal} />)}

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
      <StudyResearchTasks enrollee={enrollee} study={matchedStudy} />
    </>
  )
}

