import React, { useContext, useEffect, useState } from 'react'
import Api from 'api/api'
import { AdminUser } from 'api/adminUser'
import { useUser } from 'user/UserProvider'

export type AdminUserContextT = {
  users: AdminUser[],
  isLoading: boolean
}

export const AdminUserContext = React.createContext<AdminUserContextT>({
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
  const { user } = useUser()
  const [userState, setUserState] = useState<AdminUserContextT>({ users: [], isLoading: true })
  useEffect(() => {
    if (user?.superuser) {
      Api.fetchAdminUsers().then(result => {
        setUserState({
          users: result,
          isLoading: true
        })
      })
    } else {
      Api.fetchAdminUsersByPortal(portalShortcode).then(result => {
        setUserState({
          users: result,
          isLoading: true
        })
      })
    }
  }, [portalShortcode])
  return <AdminUserContext.Provider value={userState}>
    {children}
  </AdminUserContext.Provider>
}
