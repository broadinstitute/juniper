import React from 'react'
import { Portal, PortalEnvironment, Study } from '../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Link } from 'react-router-dom'
import { mailingListPath } from './PortalRouter'
import PortalEnvPublishControl from './publish/PortalEnvPublishControl'
import { faCogs } from '@fortawesome/free-solid-svg-icons/faCogs'
import { faClipboardCheck } from '@fortawesome/free-solid-svg-icons/faClipboardCheck'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'

const ENVIRONMENT_ICON_MAP: Record<string, React.ReactNode> = {
  sandbox: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faCogs}/>,
  irb: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faClipboardCheck}/>,
  live: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faUsers}/>
}

/** shows information about the portal config, does not allow editing yet */
export default function PortalEnvConfigView({ portal, portalEnv }:
  {portal: Portal, portalEnv: PortalEnvironment}) {
  const envIcon = ENVIRONMENT_ICON_MAP[portalEnv.environmentName]
  const isInitialized = portalEnv.portalEnvironmentConfig.initialized
  return <div className="bg-white p-3 mb-2">
    <div className="d-flex align-items-baseline">
      <h3 className="h5 text-capitalize me-4">{envIcon} {portalEnv.environmentName}</h3>
      <PortalEnvPublishControl portal={portal} destEnv={portalEnv}/>
    </div>

    <div className="ms-4 mt-3">
      { !isInitialized && <div className="fst-italic text-muted">Not initialized</div> }
      { isInitialized && <div>
        <div><h4 className="h6">Website</h4></div>
        <div className="mt-4">
          <h4 className="h6">
            <Link to={mailingListPath(portal.shortcode, portalEnv.environmentName)}>Mailing List</Link>
          </h4>
        </div>
        <div className="mt-4 mb-2">
          <h4 className="h6">Studies</h4>
          <ul>
            { portal.portalStudies.map(portalStudy => {
              const study = portalStudy.study
              return <li key={study.shortcode}>
                <StudyConfigView study={study} envName={portalEnv.environmentName}/>
              </li>
            }
            )}
          </ul>
        </div>
      </div>}
    </div>
  </div>
}


/** basic info about configuration for a given study */
function StudyConfigView({ study, envName }: {study: Study, envName: string}) {
  return <div>
    <span>{study.name}</span>
    <Link to={`studies/${study.shortcode}/env/${envName}`} className="ms-3">Content</Link>
    <Link to={`studies/${study.shortcode}/env/${envName}/participants`} className="ms-3">Participants</Link>
  </div>
}


