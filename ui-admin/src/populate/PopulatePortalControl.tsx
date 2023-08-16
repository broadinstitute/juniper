import React, {useState} from 'react'
import Select from 'react-select'
import Api from "api/api";
import {failureNotification, successNotification} from "../util/notifications";
import {Store} from "react-notifications-component";
import {useFileNameControl, useOverwriteControl} from "./PopulateControls";
export default function PopulatePortalControl() {
    const {isOverwrite, overwriteControl} = useOverwriteControl()
    const {fileName, fileNameControl} = useFileNameControl()
    const populate = async () => {
        try {
            await Api.populatePortal(fileName, isOverwrite)
            Store.addNotification(successNotification('Populate succeeded'))
        } catch {
            Store.addNotification(failureNotification('Populate failed'))
        }
    }
    return <form className="row">
        <h3 className="h5">Portal</h3>
        <div className="d-flex flex-column row-gap-2">
            {fileNameControl}
            {overwriteControl}
            <button className="btn btn-primary" type="button" onClick={populate}>Populate</button>
        </div>
    </form>
}
