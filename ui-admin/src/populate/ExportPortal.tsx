import React, {useState} from 'react'
import {Store} from "react-notifications-component";
import {failureNotification} from "../util/notifications";
import {saveBlobAsDownload} from "../util/downloadUtils";
import Api from "../api/api";
import {currentIsoDate} from "../util/timeUtils";
import {PopulateButton, PortalShortcodeControl} from "./PopulateControls";
import LoadingSpinner from "../util/LoadingSpinner";
import {Button} from "../components/forms/Button";
import {doApiLoad} from "../api/api-utils";

export default function ExportPortal({ initialPortalShortcode }: {initialPortalShortcode: string}) {
    const [portalShortcode, setPortalShortcode] = useState(initialPortalShortcode)
    const [isLoading, setIsLoading] = useState(false)

    const doExport = () => {
        doApiLoad(async () => {
            const response = await Api.exportPortal(portalShortcode)
            const blob = await response.blob()
            const fileName = `${currentIsoDate()}-${portalShortcode}-config.zip`
            saveBlobAsDownload(blob, fileName)
        }, {setIsLoading})
    }

    return <form>
      <h3>Export portal</h3>
    <PortalShortcodeControl portalShortcode={portalShortcode} setPortalShortcode={setPortalShortcode}/>
    <Button variant="primary" type="button" onClick={doExport} disabled={isLoading}>
        {isLoading ? <LoadingSpinner/> : 'Populate'}
    </Button>
  </form>
}