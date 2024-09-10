import { PortalEnvContext } from '../PortalRouter'
import React, {
  useEffect,
  useState
} from 'react'
import { SiteContent } from '@juniper/ui-core'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'
import SiteContentEditor from './SiteContentEditor'
import { previewApi } from 'util/apiContextUtils'

/** logic for loading, changing, and saving SiteContent objects */
const SiteContentLoader = ({ portalEnvContext }: {portalEnvContext: PortalEnvContext}) => {
  const { portal, portalEnv } = portalEnvContext
  const portalShortcode = portal.shortcode
  const [isLoading, setIsLoading] = useState(true)
  const [siteContent, setSiteContent] = useState(portalEnv.siteContent)

  if (!siteContent) {
    return <div>no site content configured</div>
  }

  const loadCurrentSiteContent = async () => {
    setIsLoading(true)
    Api.getCurrentSiteContent(portalShortcode, portalEnv.environmentName).then(response => {
      setSiteContent(response)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Could not load site content'))
    })
  }

  const loadSiteContent = async (stableId: string, version: number) => {
    setIsLoading(true)
    Api.getSiteContent(portalShortcode, stableId, version).then(response => {
      setSiteContent(response)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Could not load site content'))
    })
  }

  /** saves the current content as a new version, and updates the portal environment to use it */
  const createNewVersion = async (workingContent: SiteContent) => {
    let newVersion: SiteContent
    setIsLoading(true)
    try {
      newVersion = await Api.createNewSiteContentVersion(portalShortcode, workingContent.stableId, workingContent)
      Store.addNotification(successNotification(`Version ${newVersion.version} created`))
    } catch {
      Store.addNotification(failureNotification('save failed'))
      return
    }
    await switchToVersion(newVersion.id, newVersion.stableId, newVersion.version, newVersion)
    setIsLoading(false)
  }

  /** switch the environment to the given version specified by id/stableId/version.
   * if the version has already been retrieved from the server,
   * it can be passed in as an optional argument to save a duplicate fetch.
   */
  const switchToVersion = async (id: string, stableId: string, version: number, loadedContent?: SiteContent) => {
    try {
      const updatedEnv = {
        ...portalEnv,
        siteContentId: id
      }
      await Api.updatePortalEnv(portalShortcode, portalEnv.environmentName, updatedEnv)
      if (!loadedContent) {
        loadedContent = await Api.getSiteContent(portalShortcode, stableId, version)
      }
      setSiteContent(loadedContent)
      Store.addNotification(successNotification(`Environment updated to use version ${version}`))
    } catch {
      Store.addNotification(failureNotification(
        'could not update environment--it will still point to prior version'
      ))
    }
  }

  const readOnly = portalEnv.environmentName !== 'sandbox'

  useEffect(() => {
    loadCurrentSiteContent()
  }, [portalEnv.environmentName, portalShortcode])

  return <>
    { !isLoading &&
        <SiteContentEditor siteContent={siteContent}
          key={`${siteContent.stableId}-${siteContent.version}`}
          createNewVersion={createNewVersion}
          loadSiteContent={loadSiteContent}
          switchToVersion={switchToVersion}
          previewApi={previewApi(portalShortcode, portalEnv.environmentName)}
          portalEnvContext={portalEnvContext}
          readOnly={readOnly}
        />
    }
    { isLoading && <LoadingSpinner/> }
  </>
}

export default SiteContentLoader
