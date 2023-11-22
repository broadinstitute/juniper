import React from 'react'
import { NavLink, Outlet, Route, Routes } from 'react-router-dom'
import PopulatePortalControl from './PopulatePortalControl'
import PopulateSurveyControl from './PopulateSurveyControl'
import PopulateSiteContentControl from './PopulateSiteContent'
import PopulateAdminConfig from './PopulateAdminConfig'
import { navDivStyle, navListItemStyle } from 'util/subNavStyles'

/** shows links to the populate control panels, and handles the routing for them */
export default function PopulateRouteSelect({ portalShortcode }: {portalShortcode?: string}) {
  /** styles links as bold if they are the current path */
  function getLinkCssClasses({ isActive }: { isActive: boolean }) {
    return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
  }

  return <div className="container-fluid">
    <div className="bg-white p-3 ps-5 row">
      <div className="col-md-2 px-0 py-3 mh-100 bg-white">
        <div className="d-flex">
          <div style={navDivStyle}>
            <ul className="list-unstyled">
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="." className={getLinkCssClasses}>
                  Populate
                </NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="portal" className={getLinkCssClasses}>Portal</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="survey" className={getLinkCssClasses}>Survey</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="siteContent" className={getLinkCssClasses}>Site Content</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="adminConfig" className={getLinkCssClasses}>Admin config</NavLink>
              </li>
            </ul>
          </div>
        </div>
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
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
