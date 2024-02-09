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
  const { enrollees } = useUser()
  const [noActivitiesAlert, setNoActivitiesAlert] = useState<ParticipantDashboardAlert>()
  const [activeEnrolleeId, setActiveEnrolleeId] = useState(enrollees[0]?.id)

  // Function to change the active enrollee
  const handleTabClick = (enrolleeId: string)  => {
    setActiveEnrolleeId(enrolleeId)
  }

  // Find the active enrollee based on activeEnrolleeId
  const activeEnrollee = enrollees.find(enrollee => enrollee.id === activeEnrolleeId)


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
  const hasActiveTasks = enrollees.some(enrollee => enrollee.participantTasks.some(task => isTaskActive(task)))

  return (
    <>
      <DocumentTitle title="Dashboard"/>
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
        />}
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
          style={{
            background: '#fff',
            maxWidth: 768
          }}
        >
          <div className=' w-100'>
            <ul className="nav nav-tabs" style={{
              overflowX: 'auto',
              whiteSpace: 'nowrap',
              display: 'flex', // Ensuring the items are in a flex container
              flexWrap: 'nowrap' // Preventing the items from wrapping
            }}>
              {enrollees.map(enrollee => (
                <>
                  <li key={enrollee.id} className="nav-item p-1">
                    <button className={`nav-link ${activeEnrolleeId === enrollee.id ? 'active' : ''}`}
                      onClick={() => handleTabClick(enrollee.id)}>
                      {enrollee.profile.givenName || ''} {enrollee.profile.familyName || ''}
                    </button>
                  </li>
                  <li key={enrollee.id} className="nav-item">
                    <button className={`nav-link ${activeEnrolleeId === '1' ? 'active' : ''}`}
                      onClick={() => handleTabClick('1')}>
                      Other Child
                      <span className="badge bg-danger ms-2">1</span>
                    </button>
                  </li>
                  <li key={enrollee.id} className="nav-item p-1">
                    <button
                      className={`nav-link ${activeEnrolleeId === '2' ? 'active' : ''}`}
                      onClick={() => handleTabClick('2')}
                      style={{
                        whiteSpace: 'nowrap',
                        position: 'relative',
                        padding: '10px'
                      }}
                    >
                      Child W/ Tasks
                      {/* Badge styled as a notification circle */}
                      <span
                        style={{
                          position: 'absolute',
                          top: 10,
                          right: 0,
                          transform: 'translate(50%, -50%)',
                          borderRadius: '50%',
                          backgroundColor: 'red',
                          color: 'white',
                          fontSize: '0.75rem',
                          width: '20px',
                          height: '20px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}
                      >
            2+
                      </span>
                    </button>
                  </li>
                  <li key={enrollee.id} className="nav-item">
                    <button className={`nav-link ${activeEnrolleeId === '3' ? 'active' : ''}`}
                      onClick={() => handleTabClick('3')}>
                      Second Child
                    </button>
                  </li>
                  <li key={enrollee.id} className="nav-item p-1">
                    <button className={`nav-link ${activeEnrolleeId === '4' ? 'active' : ''}`}
                      onClick={() => handleTabClick('4')}>
                      Third Child
                    </button>
                  </li>
                  <li key={enrollee.id} className="nav-item">
                    <button className={`nav-link ${activeEnrolleeId === '5' ? 'active' : ''}`}
                      onClick={() => handleTabClick('5')}>
                      Last Child
                    </button>
                  </li>
                </>
              ))}
            </ul>
          </div>
          {/* Optionally, display the StudySection for the active enrollee */}


          {activeEnrollee && <StudySection enrollee={activeEnrollee} portal={portal}/>}
          {activeEnrollee && <div className="hub-dashboard mx-auto"
            style={{ maxWidth: 768 }}>

            <OutreachTasks enrollees={enrollees} studies={portal.portalStudies.map(pStudy => pStudy.study)}
              activeEnrollee={activeEnrollee}/>
          </div>}
        </main>
      </div>
    </>
  )
}

type StudySectionProps = {
  enrollee: Enrollee
  portal: Portal
}

const StudySection = (props: StudySectionProps) => {
  const {
    enrollee,
    portal
  } = props

  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study

  return (
    <>
      <h1 className="mb-4">{matchedStudy.name}</h1>
      {enrollee.kitRequests.length > 0 && <KitBanner kitRequests={enrollee.kitRequests}/>}
      <StudyResearchTasks enrollee={enrollee} studyShortcode={matchedStudy.shortcode}
        participantTasks={enrollee.participantTasks} />
    </>
  )
}

