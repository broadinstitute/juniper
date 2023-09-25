import { useNavigate, useParams } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import Api, { PortalEnvironmentChange } from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'
import PortalEnvDiffView from './PortalEnvDiffView'
import { Portal } from '@juniper/ui-core/build/types/portal'
import { studyPublishingPath } from '../../study/StudyRouter'
import { doApiLoad } from '../../api/api-utils'

type PortalEnvDiffProviderProps = {
  portal: Portal,
  studyShortcode: string,
  reloadPortal: () => void
}
/**
 * loads a diff between two environments, based on the passed-in environment and an environment name in a url param
 * also contains logic for updating an environment with a changeset
 */
const PortalEnvDiffProvider = ({ portal, studyShortcode, reloadPortal }: PortalEnvDiffProviderProps) => {
  const params = useParams()
  const sourceEnvName = params.sourceEnvName
  const destEnvName = params.destEnvName
  const [isLoading, setIsLoading] = useState(true)
  const [diffResult, setDiffResult] = useState<PortalEnvironmentChange>()
  const navigate = useNavigate()

  const applyChanges = async (changeSet: PortalEnvironmentChange) => {
    doApiLoad(async () => {
      await Api.applyEnvChanges(portal.shortcode, destEnvName!, changeSet)
      Store.addNotification(successNotification(`${destEnvName} environment updated`))
      await reloadPortal()
      navigate(studyPublishingPath(portal.shortcode, studyShortcode))
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
