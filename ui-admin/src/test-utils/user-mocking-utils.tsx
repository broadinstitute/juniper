import React from 'react'
import { AdminUser } from '../api/api'
import { UserContext, UserContextT } from '../user/UserProvider'

/** returns simple admin user for testing */
export const mockAdminUser = (superuser: boolean): AdminUser => {
  return {
    id: 'adminUser1',
    username: 'blah',
    superuser,
    token: 'fakeToken',
    portalAdminUsers: [],
    portalPermissions: {},
    isAnonymous: false
  }
}

/** component for wrapping test components that require a superuser from context */
export const MockSuperuserProvider = ({ children }: { children: React.ReactNode }) => {
  return <MockUserProvider user={mockAdminUser(true)}>{children}</MockUserProvider>
}

/** component for wrapping test components that require a non-superuser from context */
export const MockRegularUserProvider = ({ children }: { children: React.ReactNode }) => {
  return <MockUserProvider user={mockAdminUser(false)}>{children}</MockUserProvider>
}

/** component for wrapping test components that require a user from context */
export const MockUserProvider = ({ children, user }: { children: React.ReactNode, user: AdminUser }) => {
  const fakeUserContext: UserContextT = {
    user,
    loginUser: () => null,
    loginUserUnauthed: () => null,
    logoutUser: () => null
  }
  return <UserContext.Provider value={fakeUserContext}>
    {children}
  </UserContext.Provider>
}

