import React, { useState } from 'react'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import {
  FileNameControl,
  OverwriteControl,
  PopulateButton, PortalShortcodeControl
} from './PopulateControls'

/** control for invoking the populate survey API */
export default function PopulateSurveyControl({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [isLoading, setIsLoading] = useState(false)
  const [isOverwrite, setIsOverwrite] = useState(false)
  const [fileName, setFileName] = useState('')
  const [portalShortcode, setPortalShortcode] = useState(initialPortalShortcode)
  /** execute the command */
  const populate = async () => {
    setIsLoading(true)
    try {
      await Api.populateSurvey(fileName, isOverwrite, portalShortcode)
      Store.addNotification(successNotification('Populate succeeded'))
    } catch {
      Store.addNotification(failureNotification('Populate failed'))
    }
    setIsLoading(false)
  }
  return <form className="row">
    <h3 className="h5">Survey</h3>
    <p>
            Uploads the given survey file to the database.  Does NOT attach it to a study.
    </p>
    <div className="d-flex flex-column row-gap-2">
      <PortalShortcodeControl portalShortcode={portalShortcode} setPortalShortcode={setPortalShortcode}/>
      <FileNameControl fileName={fileName} setFileName={setFileName}/>
      <OverwriteControl isOverwrite={isOverwrite} setIsOverwrite={setIsOverwrite}
        text={<span>
                If no, a new version will
                  be created for its stableId..<br/>
                  If yes, the existing stableId/version will be replaced.
                  Overwrite should almost never be used in production.
        </span>}/>
      <PopulateButton onClick={populate} isLoading={isLoading}/>
    </div>
  </form>
}
