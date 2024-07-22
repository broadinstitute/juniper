import { NavLink, Outlet, Route, Routes } from 'react-router-dom'
import React from 'react'
import KitIntegrationDashboard from './KitIntegrationDashboard'
import AddressValidationIntegrationDashboard from './AddressValidationIntegrationDashboard'
import { navDivStyle, navLinkStyleFunc, navListItemStyle } from 'util/subNavStyles'
import { renderPageHeader } from 'util/pageUtils'

/** shows links to the populate control panels, and handles the routing for them */
export default function IntegrationDashboard() {
  return <div className="container-fluid px-4 py-2">
    {renderPageHeader('Integrations')}
    <div className="d-flex">
      <div style={navDivStyle}>
        <ul className="list-unstyled">
          <li style={navListItemStyle}><NavLink to="kits" style={navLinkStyleFunc}>Kits</NavLink></li>
          <li style={navListItemStyle}><NavLink to="addressValidation" style={navLinkStyleFunc}>Address
            Validation</NavLink></li>
        </ul>
      </div>
      <div className="px-3">
        <Routes>
          <Route path="kits" element={<KitIntegrationDashboard/>}/>
          <Route path="addressValidation" element={<AddressValidationIntegrationDashboard/>}/>
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
