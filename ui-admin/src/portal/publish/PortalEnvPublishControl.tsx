import { Portal } from 'api/api'
import React, { useState } from 'react'
import Select from 'react-select'
import { Link } from 'react-router-dom'
import { studyDiffPath } from 'study/StudyRouter'

type SelectOptionType = { label: string, value: string }

const ALLOWED_COPY_FLOWS: Record<string, string[]> = {
  irb: ['sandbox', 'live'],
  sandbox: ['irb', 'live'],
  live: ['irb']
}

/** modal allowing a user to copy one environment's configs over to another */
const PortalEnvPublishControl = ({ destEnvName, portal, studyShortcode }:
                                     {destEnvName: string, portal: Portal, studyShortcode: string}) => {
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
    envSelector = <Link to={studyDiffPath(portal.shortcode, studyShortcode, sourceEnvName, destEnvName)}
      className="btn btn-outline-primary">
      Compare to {sourceEnvName}
    </Link>
  }

  if (allowedSourceNames.length > 1) {
    envSelector = <> Compare to&nbsp;
      <Select options={opts} value={currentVal}
        onChange={(opt: SelectOptionType | null) =>
          setSourceEnvName(opt?.value ? opt?.value : allowedSourceNames[0])} />
      <Link to={studyDiffPath(portal.shortcode, studyShortcode, sourceEnvName, destEnvName)}
        className="btn btn-outline-primary ms-2">
        Compare
      </Link>
    </>
  }

  return <div className="d-flex align-items-baseline">{envSelector}</div>
}

/** gets the environments that have been initialized based on their config */
const getInitializedEnvironmentNames = (portal: Portal): string[] => {
  return portal.portalEnvironments.filter(env => env.portalEnvironmentConfig.initialized)
    .map(env => env.environmentName)
}

export default PortalEnvPublishControl
