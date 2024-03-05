import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import UserList from './UserList'
import AdminUserDetail from './AdminUserDetail'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { studyUsersPath } from 'study/StudyRouter'
import { Portal, Study } from '@juniper/ui-core'

/**
 * Handles user management paths across all users
 */
export default function AdminUserRouter() {
  return <>
    <NavBreadcrumb value="users">
      <Link to="users">Users</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":adminUserId" element={<AdminUserDetail/>}/>
      <Route index element={<UserList/>}/>
      <Route path="*" element={<div>Unknown admin user page</div>}/>
    </Routes>
  </>
}

/**
 * handles user management paths for the given portal & study
 */
export function PortalAdminUserRouter({ portal, study }: {portal: Portal, study: Study}) {
  return <>
    <NavBreadcrumb value="users">
      <Link to={studyUsersPath(portal.shortcode, study.shortcode)}>Users</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":adminUserId" element={<AdminUserDetail portalShortcode={portal.shortcode}/>}/>
      <Route index element={<UserList portal={portal}/>}/>
      <Route path="*" element={<div>Unknown portal admin user page</div>}/>
    </Routes>
  </>
}
