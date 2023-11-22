import React from 'react'
import { NavLink, Outlet, Route, Routes } from 'react-router-dom'
import PopulatePortalControl from './PopulatePortalControl'
import PopulateSurveyControl from './PopulateSurveyControl'
import PopulateSiteContentControl from './PopulateSiteContent'
import PopulateAdminConfig from './PopulateAdminConfig'
import ExportPortal from "./ExportPortal";

/** shows links to the populate control panels, and handles the routing for them */
export default function PopulateRouteSelect({ portalShortcode }: {portalShortcode?: string}) {
  /** styles links as bold if they are the current path */
  const navStyleFunc = ({ isActive }: {isActive: boolean}) => {
    return isActive ? { fontWeight: 'bold' } : {}
  }

  return <div className="container-fluid">
    <div className="bg-white p-3 ps-5 row">
      <div className="col-md-2 px-0 py-3 mh-100 bg-white border-end">
        <h2 className="h4 px-3">Populate</h2>
        <ul className="">
          <li className="py-1"><NavLink to="portal" style={navStyleFunc}>Portal</NavLink></li>
          <li className="py-1"><NavLink to="survey" style={navStyleFunc}>Survey</NavLink></li>
          <li className="py-1">
            <NavLink to="siteContent" style={navStyleFunc}>Site Content</NavLink>
          </li>
          <li className="py-1"><NavLink to="adminConfig" style={navStyleFunc}>Admin config</NavLink></li>
          <li className="py-1"><NavLink to="exportPortal" style={navStyleFunc}>Export portal</NavLink></li>
        </ul>
      </div>
      <div className="col-md-6 py-3">
        <Routes>
          <Route path="portal" element={<PopulatePortalControl/>}/>
          <Route path="survey"
            element={<PopulateSurveyControl initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="siteContent"
            element={<PopulateSiteContentControl initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="adminConfig"
            element={<PopulateAdminConfig/>}/>
          <Route path="exportPortal"
                 element={<ExportPortal initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="*" element={<div>Choose a populate option</div>}/>
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
