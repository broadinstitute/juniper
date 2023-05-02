import React from 'react'
import { Portal, PortalEnvironment, StudyEnvironment } from '../api/api'
import { StudyEnvContextT, studyEnvPath } from '../study/StudyEnvironmentRouter'
import ParticipantList from '../study/participants/ParticipantList'

/** show each study's participant list.  This will likely want to be updated as we grow */
export default function PortalParticipantsView({ portal, portalEnv }: {portal: Portal, portalEnv: PortalEnvironment}) {
  return <div className="row">
    <div className="col-12">
      {portal.portalStudies.map(portalStudy => {
        const studyEnv = portalStudy.study.studyEnvironments
          .find(env => env.environmentName === portalEnv.environmentName) as StudyEnvironment
        const studyContext = {
          study: portalStudy.study,
          currentEnv: studyEnv,
          currentEnvPath: studyEnvPath(portal.shortcode, portalStudy.study.shortcode, portalEnv.environmentName),
          portal
        }
        return <ParticipantList studyEnvContext={studyContext} key={portalStudy.study.shortcode}/>
      })}
    </div>
  </div>
}


