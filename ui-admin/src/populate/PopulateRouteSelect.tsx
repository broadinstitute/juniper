import React from 'react'
import { NavLink, Outlet, Route, Routes } from 'react-router-dom'
import PopulatePortalControl from './PopulatePortalControl'
import PopulateSurveyControl from './PopulateSurveyControl'
import PopulateSiteContentControl from './PopulateSiteContent'
import PopulateAdminConfig from './PopulateAdminConfig'
import ExtractPortal from './ExtractPortal'
import { navDivStyle, navLinkStyleFunc, navListItemStyle } from 'util/subNavStyles'
import { renderPageHeader } from 'util/pageUtils'
import PopulateCommand from './PopulateCommand'
import PopulateHome from './PopulateHome'

/** shows links to the populate control panels, and handles the routing for them */
export default function PopulateRouteSelect({ portalShortcode }: {portalShortcode?: string}) {
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Populate') }
    <div className="d-flex">
      <div style={navDivStyle}>
        <ul className="list-unstyled">
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="" end style={navLinkStyleFunc}>Quick shortcuts</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="portal" style={navLinkStyleFunc}>Portal</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="survey" style={navLinkStyleFunc}>Survey</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="siteContent" style={navLinkStyleFunc}>Site Content</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="adminConfig" style={navLinkStyleFunc}>Admin config</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="extractPortal" style={navLinkStyleFunc}>Extract portal</NavLink>
          </li>
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="runCommand" style={navLinkStyleFunc}>Run command</NavLink>
          </li>
        </ul>
      </div>
      <div className="flex-grow-1 bg-white p-3">
        <Routes>
          <Route path="portal" element={<PopulatePortalControl/>}/>
          <Route path="survey"
            element={<PopulateSurveyControl initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="siteContent"
            element={<PopulateSiteContentControl initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="adminConfig"
            element={<PopulateAdminConfig/>}/>
          <Route path="extractPortal"
            element={<ExtractPortal initialPortalShortcode={portalShortcode || ''}/>}/>
          <Route path="runCommand"
            element={<PopulateCommand/>}/>
          <Route path="*" element={<PopulateHome/>}/>
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
