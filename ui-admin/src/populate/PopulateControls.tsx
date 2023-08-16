import React, {useState} from 'react'
import {Link, NavLink, Outlet, Route, Routes} from "react-router-dom";
import PopulatePortalControl from "./PopulatePortalControl";
import Select from "react-select";
import PopulateSurveyControl from "./PopulateSurveyControl";
import PopulateSiteContentControl from "./PopulateSiteContent";

export default function PopulateControls({portalShortcode}: {portalShortcode?: string}) {
    const navStyleFunc = ({ isActive }: {isActive: boolean}) => {
        return isActive ? { fontWeight: 'bold' } : {}
    }

    return <div className="container row">
        <div className="col-md-3">
            <h2 className="h4">Populate</h2>
            <ul className="list-unstyled list-group">
                <li className="list-group-item"><NavLink to="portal" style={navStyleFunc}>Portal</NavLink></li>
                <li className="list-group-item"><NavLink to="survey" style={navStyleFunc}>Survey</NavLink></li>
                <li className="list-group-item">
                    <NavLink to="siteContent" style={navStyleFunc}>Site Content</NavLink>
                </li>
            </ul>
        </div>
        <div className="col-md-9">
            <Routes>
                <Route path="portal" element={<PopulatePortalControl/>}/>
                <Route path="survey" element={<PopulateSurveyControl initialPortalShortcode={portalShortcode || ''}/>}/>
                <Route path="siteContent"
                       element={<PopulateSiteContentControl initialPortalShortcode={portalShortcode || ''}/>}/>
            </Routes>
            <Outlet/>
        </div>
    </div>
}

export const useOverwriteControl = () => {
    const [isOverwrite, setIsOverwrite] = useState(false)
    const overwriteOpts = [{label: 'Yes', value: true}, {label: 'No', value: false}]
    const currentValue = overwriteOpts.find(opt => opt.value === isOverwrite)
    return {
        isOverwrite,
        overwriteControl: <label className="form-label">
            Override
            <Select options={overwriteOpts} value={currentValue}
                    onChange={opt => setIsOverwrite(!!opt?.value)}/>
        </label>
    }
}

export const useFileNameControl = () => {
    const [fileName, setFileName] = useState('')
    return {
        fileName,
        fileNameControl: <label className="form-label">
            File name
            <input type="text" value={fileName} className="form-control"
                   onChange={e => setFileName(e.target.value)}/>
        </label>
    }
}

export const usePortalShortcodeControl = (initialShortcode: string) => {
    const [portalShortcode, setPortalShortcode] = useState(initialShortcode)
    return {
        portalShortcode,
        shortcodeControl: <label className="form-label">
            Portal shortcode
            <input type="text" value={portalShortcode} className="form-control"
                   onChange={e => setPortalShortcode(e.target.value)}/>
        </label>
    }
}
