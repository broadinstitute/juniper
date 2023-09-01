import { PortalEnvContext } from '../PortalRouter'
import React, { useEffect, useState } from 'react'
import { ApiContextT, SiteContent } from '@juniper/ui-core'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'
import SiteContentEditor from './SiteContentEditor'


/** logic for loading, changing, and saving SiteContent objects */
const SiteContentLoader = ({ portalEnvContext }: {portalEnvContext: PortalEnvContext}) => {
  const { portal, portalEnv } = portalEnvContext
  const portalShortcode = portal.shortcode
  const [isLoading, setIsLoading] = useState(true)
  const [siteContent, setSiteContent] = useState(portalEnv.siteContent)
  if (!siteContent) {
    return <div>no site content configured</div>
  }

  /** uses the admin image retrieval endpoint */
  const getImageUrl = (cleanFileName: string, version: number) =>
      `/api/public/portals/v1/${portalShortcode}/env/${portalEnv.environmentName}` +
      `/siteImages/${version}/${cleanFileName}`
  const previewApi: ApiContextT = {
    getImageUrl,
    submitMailingListContact: () => Promise.resolve({})
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
    try {
      newVersion = await Api.createNewSiteContentVersion(portalShortcode, workingContent.stableId, workingContent)
      Store.addNotification(successNotification(`Version ${newVersion.version} created`))
    } catch {
      Store.addNotification(failureNotification('save failed'))
      return
    }
    try {
      let updatedEnv = {
        ...portalEnv,
        siteContentId: newVersion.id
      }
      updatedEnv = await Api.updatePortalEnv(portalShortcode, portalEnv.environmentName, updatedEnv)
      portalEnvContext.updatePortalEnv({
        ...portalEnv,
        ...updatedEnv,
        siteContent: newVersion
      })
      setSiteContent(newVersion)
      Store.addNotification(successNotification(`Environment updated to use version ${newVersion.version}`))
    } catch {
      Store.addNotification(failureNotification(
        'could not update environment--it will still point to prior version'
      ))
    }
  }
  const readOnly = portalEnv.environmentName !== 'sandbox'

  useEffect(() => {
    if (!portalEnv.siteContent) {
      return
    }
    loadSiteContent(portalEnv.siteContent.stableId, portalEnv.siteContent.version)
  }, [portalEnv.environmentName, portalShortcode])

  return <>
    { !isLoading && <SiteContentEditor siteContent={siteContent}
      createNewVersion={createNewVersion}
      loadSiteContent={loadSiteContent}
      previewApi={previewApi}
      portalShortcode={portalShortcode}
      readOnly={readOnly}
    /> }
    { isLoading && <LoadingSpinner/> }
  </>
}

export default SiteContentLoader
