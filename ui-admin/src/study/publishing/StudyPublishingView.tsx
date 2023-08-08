import React from 'react'
import { Portal, PortalEnvironment } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCogs } from '@fortawesome/free-solid-svg-icons/faCogs'
import { faClipboardCheck } from '@fortawesome/free-solid-svg-icons/faClipboardCheck'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import { isSuperuser } from 'user/UserProvider'
import PortalEnvPublishControl from 'portal/publish/PortalEnvPublishControl'
import { Link } from 'react-router-dom'
import { siteContentPath } from 'portal/PortalRouter'

const ENV_SORT_ORDER = ['sandbox', 'irb', 'live']
/** Page an admin user sees immediately after logging in */
export default function StudyPublishingView({ portal, studyShortcode }: {portal: Portal, studyShortcode: string}) {
  const sortedEnvs = portal.portalEnvironments.sort((pa, pb) =>
    ENV_SORT_ORDER.indexOf(pa.environmentName) - ENV_SORT_ORDER.indexOf(pb.environmentName))
  return <div className="p-4 container">
    <div className="row">
      <ul className="list-unstyled">
        { sortedEnvs.map(portalEnv => <li key={portalEnv.environmentName}>
          <StudyEnvPublishView portalEnv={portalEnv} portal={portal} studyShortcode={studyShortcode}/>
        </li>)}
      </ul>
    </div>
  </div>
}

const ENVIRONMENT_ICON_MAP: Record<string, React.ReactNode> = {
  sandbox: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faCogs}/>,
  irb: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faClipboardCheck}/>,
  live: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faUsers}/>
}

/** shows publishing related info and controls for a given environment */
function StudyEnvPublishView({ portal, portalEnv, studyShortcode }:
                                          {portal: Portal, portalEnv: PortalEnvironment, studyShortcode: string}) {
  const envIcon = ENVIRONMENT_ICON_MAP[portalEnv.environmentName]
  const isInitialized = portalEnv.portalEnvironmentConfig.initialized
  return <div className="bg-white p-3 mb-2">
    <h3 className="h5 text-capitalize me-4">{envIcon} {portalEnv.environmentName}</h3>
    { isSuperuser() && <PortalEnvPublishControl portal={portal} studyShortcode={studyShortcode}
      destEnvName={portalEnv.environmentName}/> }
    <div className="ms-4 mt-3">
      { !isInitialized && <div className="fst-italic text-muted">Not initialized</div> }
      { isInitialized && <div>
                Website
        {portalEnv.siteContent && <Link to={siteContentPath(portal.shortcode, portalEnv.environmentName)}
          className="ms-2 fw-normal">
          {portalEnv.siteContent.stableId} v{portalEnv.siteContent.version}
        </Link> }
      </div>}
    </div>
  </div>
}

