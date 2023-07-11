import React, { useContext, useEffect, useState } from 'react'
import Api, { AdminUser } from 'api/api'

export type AdminUserContextT = {
  users: AdminUser[],
  isLoading: boolean
}

const AdminUserContext = React.createContext<AdminUserContextT>({
  users: [],
  isLoading: true
})

/** helper function for accessing context */
export const useAdminUserContext = () => {
  return useContext(AdminUserContext)
}

/**
 * provides a list of the users for the given portal.  This is a non-blocking provider--the children will
 * render while the list is loading -- it's up to children to determine how to render during that time.
 *
 * This provider also makes no guarantees of user freshness--it's assumed users will be added infrequently
 * enough that, e.g., polling for updates isn't worth the trouble.
 */
export default function AdminUserProvider({ portalShortcode, children }:
{portalShortcode: string, children: React.ReactNode}) {
  const [userState, setUserState] = useState<AdminUserContextT>({ users: [], isLoading: true })
  useEffect(() => {
    Api.fetchAdminUsersByPortal(portalShortcode).then(result => {
      setUserState({
        users: result,
        isLoading: true
      })
    })
  }, [portalShortcode])
  return <AdminUserContext.Provider value={userState}>
    {children}
  </AdminUserContext.Provider>
}
