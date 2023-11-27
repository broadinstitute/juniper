import React, { useState } from 'react'
import { saveBlobAsDownload } from 'util/downloadUtils'
import Api from 'api/api'
import { currentIsoDate } from 'util/timeUtils'
import { PortalShortcodeControl } from './PopulateControls'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'

/** control for downloading portal configs as a zip file */
export default function ExtractPortal({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [portalShortcode, setPortalShortcode] = useState(initialPortalShortcode)
  const [isLoading, setIsLoading] = useState(false)

  const doExport = () => {
    doApiLoad(async () => {
      const response = await Api.extractPortal(portalShortcode)
      const blob = await response.blob()
      const fileName = `${currentIsoDate()}-${portalShortcode}-config.zip`
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading })
  }

  return <form onSubmit={e => {
    e.preventDefault()
    if (!isLoading) { doExport() }
  }}>
    <h3>Extract portal</h3>
    <PortalShortcodeControl portalShortcode={portalShortcode} setPortalShortcode={setPortalShortcode}/>
    <br/>
    <Button variant="primary" type="button" onClick={doExport} disabled={isLoading}>
      {isLoading ? <LoadingSpinner/> : 'Download configs'}
    </Button>
  </form>
}
