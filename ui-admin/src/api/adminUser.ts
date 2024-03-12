
export type AdminUser = {
  id: string,
  createdAt: number,
  username: string,
  token: string,
  superuser: boolean,
  lastLogin: number,
  portalPermissions: Record<string, string[]>,
  isAnonymous: boolean,
  portalAdminUsers?: PortalAdminUser[]
};

export type NewAdminUser = {
  username: string,
  superuser: boolean,
  portalShortcode: string | null
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
