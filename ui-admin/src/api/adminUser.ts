
export type AdminUser = {
  id: string,
  createdAt: number,
  username: string,
  token: string,
  superuser: boolean,
  lastLogin: number,
  portalPermissions: Record<string, string[]>,
  portalAdminUsers?: PortalAdminUser[]
};

export type AdminUserParams = {
  username: string,
  superuser: boolean,
  portalShortcode: string | null,
  roleNames: string[]
}

export type PortalAdminUser = {
  portalId: string
  roles: Role[]
}

export type Role = {
  name: string
  displayName: string
  description: string
  permissions: Permission[]
}

export type Permission = {
  displayName: string
  description: string
  name: string
}
