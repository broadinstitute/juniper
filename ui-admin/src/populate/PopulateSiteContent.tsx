import React, {useState} from "react";
import Api from "../api/api";
import {Store} from "react-notifications-component";
import {failureNotification, successNotification} from "util/notifications";
import {useFileNameControl, useOverwriteControl, usePortalShortcodeControl} from "./PopulateControls";

export default function PopulateSiteContentControl({initialPortalShortcode}: {initialPortalShortcode: string}) {
    const {isOverwrite, overwriteControl} = useOverwriteControl()
    const {fileName, fileNameControl} = useFileNameControl()
    const {portalShortcode, shortcodeControl} = usePortalShortcodeControl(initialPortalShortcode)
    const populate = async () => {
        try {
            await Api.populateSiteContent(fileName, isOverwrite, portalShortcode)
            Store.addNotification(successNotification('Populate succeeded'))
        } catch {
            Store.addNotification(failureNotification('Populate failed'))
        }
    }
    return <form className="row">
        <h3 className="h5">Site Content</h3>
        <div className="d-flex flex-column row-gap-2">
            {shortcodeControl}
            {fileNameControl}
            {overwriteControl}
            <button className="btn btn-primary" type="button" onClick={populate}>Populate</button>
        </div>
    </form>
}