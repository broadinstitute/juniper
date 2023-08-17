import React, { useState } from 'react'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { PopulateButton, useFileNameControl, OverwriteControl, usePortalShortcodeControl } from './PopulateControls'

/** control for invoking the populate siteContent API */
export default function PopulateSiteContentControl({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [isLoading, setIsLoading] = useState(false)
  const [isOverwrite, setIsOverwrite] = useState(false)
  const { fileName, fileNameControl } = useFileNameControl()
  const { portalShortcode, shortcodeControl } = usePortalShortcodeControl(initialPortalShortcode)

  /** execute the command */
  const populate = async () => {
    setIsLoading(true)
    try {
      await Api.populateSiteContent(fileName, isOverwrite, portalShortcode)
      Store.addNotification(successNotification('Populate succeeded'))
    } catch {
      Store.addNotification(failureNotification('Populate failed'))
    }
    setIsLoading(false)
  }
  return <form className="row">
    <h3 className="h5">Site Content</h3>
    <p>
            Uploads the SiteContent file to the database.  Does NOT attach it to a study.
    </p>
    <div className="d-flex flex-column row-gap-2">
      {shortcodeControl}
      {fileNameControl}
      <OverwriteControl isOverwrite={isOverwrite} setIsOverwrite={setIsOverwrite}
        text={<span>
                    If no, a new version will
                    be created for its stableId..<br/>
                    If yes, the existing stableId/version will be replaced.`
        </span>}/>
      <PopulateButton onClick={populate} isLoading={isLoading}/>
    </div>
  </form>
}
