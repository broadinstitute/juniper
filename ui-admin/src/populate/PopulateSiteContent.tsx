import React, { useState } from 'react'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import {
  PopulateButton,
  OverwriteControl,
  PortalShortcodeControl, FileNameControl
} from './PopulateControls'

/** control for invoking the populate siteContent API */
export default function PopulateSiteContentControl({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [isLoading, setIsLoading] = useState(false)
  const [isOverwrite, setIsOverwrite] = useState(false)
  const [fileName, setFileName] = useState('')
  const [portalShortcode, setPortalShortcode] = useState(initialPortalShortcode)

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

  const updatePortalShortcode = (value: string) => {
    setFileName(`portals/${value}/siteContent/siteContent.json`)
    setPortalShortcode(value)
  }

  return <form className="row">
    <h3 className="h5">Site Content</h3>
    <p>
            Uploads the SiteContent file to the database.  Does NOT attach it to a study.
    </p>
    <div className="d-flex flex-column row-gap-2">
      <PortalShortcodeControl portalShortcode={portalShortcode} setPortalShortcode={updatePortalShortcode}/>
      <FileNameControl fileName={fileName} setFileName={setFileName}/>
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
