import React, { useEffect, useState } from 'react'
import Api, { Portal, PortalEnvironment } from '../api/api'
import { useParams } from 'react-router-dom'
import LoadingSpinner from '../util/LoadingSpinner'

type EnvironmentDiffProps = {
  portal: Portal,
  portalEnv: PortalEnvironment,
}


export default function EnvironmentDiff({ portal, portalEnv }: EnvironmentDiffProps) {
  const params = useParams()
  const sourceEnvName: string | undefined = params.sourceEnvName
  const [isLoading, setIsLoading] = useState(true)
  const [diffResult, setDiffResult] = useState(null)

  useEffect(() => {
    if (!sourceEnvName) {
      alert('no source environment specified')
      return
    }
    Api.fetchEnvDiff(portal.shortcode, sourceEnvName, portalEnv.environmentName).then(result => {
      setDiffResult(result)
    }).catch(e => {
      alert(e)
    })
  }, [])

  return <LoadingSpinner isLoading={isLoading}>

  </LoadingSpinner>
}
