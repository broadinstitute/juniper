import { NavLink, Outlet, Route, Routes } from 'react-router-dom'
import React from 'react'
import KitIntegrationDashboard from './KitIntegrationDashboard'

/** shows links to the populate control panels, and handles the routing for them */
export default function IntegrationDashboard() {
  /** styles links as bold if they are the current path */
  const navStyleFunc = ({ isActive }: {isActive: boolean}) => {
    return isActive ? { fontWeight: 'bold' } : {}
  }

  return <div className="container-fluid">
    <div className="bg-white p-3 ps-5 row">
      <div className="col-md-2 px-0 py-3 mh-100 bg-white border-end">
        <h2 className="h4 px-3">Integrations</h2>
        <ul className="">
          <li className="py-1"><NavLink to="kits" style={navStyleFunc}>Kits</NavLink></li>
        </ul>
      </div>
      <div className="col-md-6 py-3">
        <Routes>
          <Route path="kits" element={<KitIntegrationDashboard/>}/>
        </Routes>
        <Outlet/>
      </div>
    </div>
  </div>
}
