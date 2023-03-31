import { Portal, PortalEnvironment, Study, StudyEnvironment } from '../api/api'
import Modal from 'react-bootstrap/Modal'
import React, { useState } from 'react'
import Select from 'react-select'
import { portalEnvDiffPath } from './PortalRouter'
import { Link } from 'react-router-dom'

type SelectOptionType = { label: string, value: string }

const ALLOWED_COPY_FLOWS: Record<string, string[]> = {
  irb: ['sandbox', 'live'],
  sandbox: ['irb', 'live'],
  live: ['irb']
}

/** modal allowing a user to copy one environment's configs over to another */
function PortalEnvPublishControl({ destEnv, portal, publishFunc }: {destEnv: PortalEnvironment,
  publishFunc: (source: string, dest: string) => void, portal: Portal}) {
  const [showModal, setShowModal] = useState(false)
  const destEnvName = destEnv.environmentName

  const initializedEnvironmentNames = getInitializedEnvironmentNames(portal)
  const allowedSourceNames = ALLOWED_COPY_FLOWS[destEnvName].filter((envName: string) => {
    return initializedEnvironmentNames.includes(envName)
  })
  const [sourceEnvName, setSourceEnvName] = useState<string>(allowedSourceNames[0])

  const opts = allowedSourceNames.map((name: string) => ({
    label: name, value: name
  }))
  const currentVal = { label: sourceEnvName, value: sourceEnvName }
  let envSelector = <></>
  if (allowedSourceNames.length == 1) {
    envSelector = <Link to={portalEnvDiffPath(portal.shortcode, destEnvName, sourceEnvName)}>
      Copy from {sourceEnvName}
    </Link>
  }

  if (allowedSourceNames.length > 1) {
    envSelector = <> Copy from&nbsp;
      <Select options={opts} value={currentVal}
        onChange={(opt: SelectOptionType | null) =>
          setSourceEnvName(opt?.value ? opt?.value : allowedSourceNames[0])} />
      <Link to={portalEnvDiffPath(portal.shortcode, destEnvName, sourceEnvName)}>
        Copy
      </Link>
    </>
  }

  return <div className="d-flex align-items-baseline">{envSelector}</div>
}

/** gets the environments that have been initialized based on their config */
function getInitializedEnvironmentNames(portal: Portal): string[] {
  return portal.portalEnvironments.filter(env => env.portalEnvironmentConfig.initialized)
    .map(env => env.environmentName)
}

export default PortalEnvPublishControl
