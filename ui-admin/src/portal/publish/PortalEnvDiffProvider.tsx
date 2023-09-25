import { useParams } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import Api, { PortalEnvironmentChange } from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import _cloneDeep from 'lodash/cloneDeep'
import LoadingSpinner from 'util/LoadingSpinner'
import PortalEnvDiffView from './PortalEnvDiffView'
import { Portal } from '@juniper/ui-core/build/types/portal'
import { studyPublishingPath } from '../../study/StudyRouter'

type PortalEnvDiffProviderProps = {
  portal: Portal,
  studyShortcode: string,
  updatePortal: (portal: Portal) => void
}
/**
 * loads a diff between two environments, based on the passed-in environment and an environment name in a url param
 * also contains logic for updating an environment with a changeset
 */
const PortalEnvDiffProvider = ({ portal, studyShortcode, updatePortal }: PortalEnvDiffProviderProps) => {
  const params = useParams()
  const sourceEnvName = params.sourceEnvName
  const destEnvName = params.destEnvName
  const [isLoading, setIsLoading] = useState(true)
  const [diffResult, setDiffResult] = useState<PortalEnvironmentChange>()

  const applyChanges = (changeSet: PortalEnvironmentChange) => {
    Api.applyEnvChanges(portal.shortcode, destEnvName!, changeSet).then(result => {
      Store.addNotification(successNotification(`${destEnvName} environment updated`))
      const updatedPortal = _cloneDeep(portal)
      const envIndex = updatedPortal.portalEnvironments.findIndex(env => env.environmentName === result.environmentName)
      updatedPortal.portalEnvironments[envIndex] = result
      updatePortal(updatedPortal)
      // for now, do a hard reload to make sure all changes are propagated
      window.location.pathname = studyPublishingPath(portal.shortcode, studyShortcode)
    }).catch(e => {
      Store.addNotification(failureNotification(`Update failed: ${  e.message}`))
    })
  }

  useEffect(() => {
    if (!sourceEnvName || !destEnvName) {
      return
    }
    Api.fetchEnvDiff(portal.shortcode, sourceEnvName, destEnvName).then(result => {
      setDiffResult(result)
      setIsLoading(false)
    }).catch(e => {
      alert(e)
      setIsLoading(false)
    })
  }, [portal.shortcode, studyShortcode, sourceEnvName, destEnvName])

  if (!sourceEnvName || !destEnvName) {
    return <div>Source and dest environment must be specified</div>
  }

  return <>
    {isLoading && <LoadingSpinner/> }
    {(!isLoading && diffResult) && <PortalEnvDiffView sourceEnvName={sourceEnvName}
      portal={portal} destEnvName={destEnvName}
      applyChanges={applyChanges} changeSet={diffResult}/>}
  </>
}

export default PortalEnvDiffProvider
