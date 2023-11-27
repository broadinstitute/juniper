import React, { useState } from 'react'
import Api from 'api/api'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import { FileNameControl, OverwriteControl, PopulateButton } from './PopulateControls'
import {doApiLoad} from "../api/api-utils";
import {useFileUploadButton} from "../util/uploadUtils";

/** control for invoking the populate portal API */
export default function PopulatePortalControl() {
  const [isLoading, setIsLoading] = useState(false)
  const [isOverwrite, setIsOverwrite] = useState(false)
  const [fileName, setFileName] = useState('')
    const { file, FileChooser } = useFileUploadButton(() => 1)
  /** execute the command */
  const populate = async () => {
      doApiLoad(async () => {
          if (fileName.length) {
              await Api.populatePortal(fileName, isOverwrite)
          } else if (file) {
              await Api.uploadPortal(file, isOverwrite)
          } else {
              Store.addNotification(failureNotification('No file or name provided'))
              return
          }
          Store.addNotification(successNotification('Populate succeeded'))
      }, {setIsLoading})
  }

  return <form className="row" onSubmit={e => {
      e.preventDefault()
      if (!isLoading) { populate() }
  }}>
    <h3 className="h5">Portal</h3>
    <p>Repopulates the entire portal, including all studies contained in the portal. </p>
    <div className="d-flex flex-column row-gap-2">
        <div>
            { FileChooser }
            <span className="text-muted fst-italic ms-2">{file?.name}</span>
        </div>
        - or -
      <FileNameControl fileName={fileName} setFileName={setFileName}/>


      <OverwriteControl isOverwrite={isOverwrite} setIsOverwrite={setIsOverwrite}
        text={<span>
                If no, no existing data or forms are touched,
                except for synthetic participants which are refreshed.<br/>
                If yes, existing participants, surveys, and site content will be destroyed, and everything reset to
                from the files.  This method will fail if there are participants in the live environment who
                have not been withdrawn.
        </span>}/>
      <PopulateButton onClick={populate} isLoading={isLoading}/>
    </div>
  </form>
}
