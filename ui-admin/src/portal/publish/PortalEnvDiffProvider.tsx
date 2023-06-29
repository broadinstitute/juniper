import { useNavigate, useParams } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import Api, { PortalEnvironmentChange } from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import _cloneDeep from 'lodash/cloneDeep'
import LoadingSpinner from 'util/LoadingSpinner'
import PortalEnvDiffView from './PortalEnvDiffView'
import { Portal, PortalEnvironment } from '@juniper/ui-core/build/types/portal'

type PortalEnvDiffProviderProps = {
  portal: Portal,
  portalEnv: PortalEnvironment,
  updatePortal: (portal: Portal) => void
}
const PortalEnvDiffProvider = ({ portal, portalEnv, updatePortal }: PortalEnvDiffProviderProps) => {
  const params = useParams()
  const sourceEnvName: string | undefined = params.sourceEnvName
  const [isLoading, setIsLoading] = useState(true)
  const [diffResult, setDiffResult] = useState<PortalEnvironmentChange | null>(null)
  const navigate = useNavigate()

  const applyChanges = (changeSet: PortalEnvironmentChange) => {
    Api.applyEnvChanges(portal.shortcode, portalEnv.environmentName, changeSet).then(result => {
      Store.addNotification(successNotification(`${portalEnv.environmentName} environment updated`))
      const updatedPortal = _cloneDeep(portal)
      const envIndex = updatedPortal.portalEnvironments.findIndex(env => env.environmentName === result.environmentName)
      updatedPortal.portalEnvironments[envIndex] = result
      updatePortal(updatedPortal)
      navigate(`/${portal.shortcode}`)
    }).catch(e => {
      Store.addNotification(failureNotification(`Update failed: ${  e.message}`))
    })
  }

  useEffect(() => {
    if (!sourceEnvName) {
      alert('no source environment specified')
      return
    }
    Api.fetchEnvDiff(portal.shortcode, sourceEnvName, portalEnv.environmentName).then(result => {
      setDiffResult(result)
      setIsLoading(false)
    }).catch(e => {
      alert(e)
      setIsLoading(false)
    })
  }, [])
  return <>
    {isLoading && <LoadingSpinner/> }
    {(!isLoading && diffResult) && <PortalEnvDiffView sourceName={sourceEnvName ?? 'unknown'}
      portal={portal} portalEnv={portalEnv}
      applyChanges={applyChanges} changeSet={diffResult}/>}
  </>
}

export default PortalEnvDiffProvider
