import React, { useState } from 'react'
import Api from '../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import {
  OverwriteControl,
  PopulateButton,
  useFileNameControl,
  usePortalShortcodeControl
} from './PopulateControls'

/** control for invoking the populate survey API */
export default function PopulateSurveyControl({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [isLoading, setIsLoading] = useState(false)
  const [isOverwrite, setIsOverwrite] = useState(false)
  const { fileName, fileNameControl } = useFileNameControl()
  const { portalShortcode, shortcodeControl } = usePortalShortcodeControl(initialPortalShortcode)
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
      {shortcodeControl}
      {fileNameControl}
      <OverwriteControl isOverwrite={isOverwrite} setIsOverwrite={setIsOverwrite}
        text={<span>
                If no, the survey will
                    be populated to the next available version number for its stable id.<br/>
                    If yes, the survey content will be updated in-place while
                    preserving the version number.  This should almost never be used in production.
        </span>}/>
      <PopulateButton onClick={populate} isLoading={isLoading}/>
      <PopulateButton onClick={populate} isLoading={isLoading}/>
    </div>
  </form>
}
