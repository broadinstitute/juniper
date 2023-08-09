import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEdit } from '@fortawesome/free-solid-svg-icons'
import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { LoadedPortalContextT } from '../portal/PortalProvider'
import PortalEnvConfigView from '../portal/PortalEnvConfigView'
import { PortalEnvironment } from '@juniper/ui-core'

/** shows settings for both a study and its containing portal */
export default function StudySettings({ studyEnvContext, portalContext }:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const envConfig = studyEnvContext.currentEnv.studyEnvironmentConfig
  const portalEnv = portalContext.portal.portalEnvironments
    .find(env =>
      env.environmentName === studyEnvContext.currentEnv.environmentName) as PortalEnvironment
  return <div className="container bg-white">

    <div className="p-3">
      <h2 className="h5">Study Configuration</h2>
      <div className="form-group">
        <div className="form-group-item">
          <label>Accepting enrollment: </label> { envConfig.acceptingEnrollment ? 'Yes' : 'No'}
          <br/>
          <label>Enrollment Password protected:</label> { envConfig.passwordProtected ? 'Yes' : 'No'}
          <br/>
          <label>Enrollment Password:</label> { envConfig.password }
        </div>
        <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
          <FontAwesomeIcon icon={faEdit}/> Edit
        </button>
      </div>
    </div>
    <div className="p-3">
      <h2 className="h5">Website configuration ({portalContext.portal.name})</h2>
      <PortalEnvConfigView portalEnv={portalEnv} portal={portalContext.portal}
        updatePortal={portalContext.updatePortal}/>
    </div>
  </div>
}
