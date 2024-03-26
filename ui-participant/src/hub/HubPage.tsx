import React, { useEffect, useState } from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import Api, { Enrollee, Portal, Profile, Study } from '../api/api'
import { isTaskActive } from './TaskLink'
import { DocumentTitle } from 'util/DocumentTitle'

import { HubMessageAlert, HubUpdateMessage, useHubUpdate } from './hubUpdates'
import { ParticipantDashboardAlert, alertDefaults } from '@juniper/ui-core'
import KitBanner from './kit/KitBanner'
import StudyResearchTasks from './StudyResearchTasks'
import OutreachTasks from './OutreachTasks'
import HubPageParticipantSelector from './HubPageParticipantSelector'
import classNames from 'classnames'
import { useNavigate } from 'react-router-dom'


/** renders the logged-in hub page */
export default function HubPage() {
  const navigate = useNavigate()

  const {
    portal,
    portalEnv
  } = usePortalEnv()
  const {
    enrollees,
    activeEnrollee,
    activeEnrolleeProfile,
    relations,
    profile
  } = useUser()

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

  function getTotalTasks() {
    let totalTasks = 0
    enrollees.forEach(enrollee => {
      enrollee.participantTasks.forEach(participantTask => {
        if (participantTask.status != 'COMPLETE') {
          totalTasks++
        }
      })
    })

    relations?.forEach(enrolleeRelation => {
      enrolleeRelation.relation.targetEnrollee.participantTasks.forEach(participantTask => {
        if (participantTask.status != 'COMPLETE') {
          totalTasks++
        }
      })
    })
    return totalTasks
  }

  function addParticipant() {
    const matchedStudy = portal.portalStudies
      .find(pStudy => pStudy.study.studyEnvironments[0].id === activeEnrollee?.studyEnvironmentId)?.study as Study
    const studyShortCode = matchedStudy.shortcode
    navigate(`/studies/${studyShortCode}/addParticipant`, { replace: true })
  }

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
          <div className="dropdown hub-dashboard-background flex-grow-1">
            <button
              className="btn btn-outline-primary dropdown-toggle w-100 position-relative d-flex justify-content-center
              align-items-center" type="button"
              data-bs-toggle="dropdown" aria-expanded="false" id="dropdownMenuButton">
              Participants and Tasks
              <span className={` alert-circle position-absolute rounded-circle
                    ${getTotalTasks() == 0 ? 'bg-secondary-subtle' : 'bg-danger text-white'}`}
              style={{ right: '12px', top: '50%', transform: 'translateY(-50%)' }}>
                {getTotalTasks()}
              </span>
            </button>


            <ul className="dropdown-menu w-100" aria-labelledby="dropdownMenuButton">

              <HubPageParticipantSelector enrollee={enrollees[0]} profile={profile}
                relationshipType={undefined}/>

              {relations?.map(enrolleeRelation => (
                <HubPageParticipantSelector enrollee={enrolleeRelation.relation.targetEnrollee}
                  profile={enrolleeRelation.profile}
                  relationshipType={enrolleeRelation.relation.relationshipType}/>
              ))}
            </ul>
          </div>
          <br/>
          <br/>

          {activeEnrollee && activeEnrolleeProfile && <StudySection key={activeEnrollee.id}
            enrollee={activeEnrollee} portal={portal} profile={activeEnrolleeProfile}/>}

          <div>
            <button
              className={classNames(
                'btn btn-lg btn-outline-primary',
                'd-flex justify-content-center', 'w-100'
              )}
              onClick={() => { addParticipant() }}
            >
                Add Participant
            </button>
          </div>
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
  portal: Portal,
  profile: Profile
}

const StudySection = (props: StudySectionProps) => {
  const {
    enrollee,
    portal,
    profile
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
      <h4 className="mb-4">{getName(profile)}</h4>
      {enrollee.kitRequests.length > 0 && <KitBanner kitRequests={enrollee.kitRequests} />}
      <StudyResearchTasks enrollee={enrollee} studyShortcode={matchedStudy.shortcode}
        participantTasks={enrollee.participantTasks} />
    </>
  )
}

