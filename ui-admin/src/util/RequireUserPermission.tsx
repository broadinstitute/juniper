import React from 'react'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import { Portal } from '@juniper/ui-core'

/**
 Example use:
 <RequireUserPermission superuser> things </RequireSuperuserPermission>

 <RequireUserPermission portal={portal} perms={["participant_data_view"]}>stuff</RequireUserPermission>
 */


export type RequireUserPermissionProps = RequireSuperuser | RequirePermissions

type BaseRequireUserPermissionProps = {
  noPermissionMessage?: React.ReactNode,
  children: React.ReactNode,
}

type RequireSuperuser = BaseRequireUserPermissionProps & {
  superuser: true
}

const isRequireSuperuser = (props: RequireUserPermissionProps): props is RequireSuperuser => {
  return (props as RequireSuperuser).superuser
}

type RequirePermissions = BaseRequireUserPermissionProps &  {
  portal: Portal,
  perms: string[],
}

const isRequirePermissions = (props: RequireUserPermissionProps): props is RequirePermissions => {
  return (props as RequirePermissions).perms !== undefined
}

export const RequireUserPermission = (props: RequireUserPermissionProps) => {
  const { user } = useUser()

  const hasPermission = () => {
    if (isRequireSuperuser(props)) {
      return user?.superuser
    }
    if (isRequirePermissions(props)) {
      return props.perms.every(perm => userHasPermission(user, props.portal.id, perm))
    }
    return false
  }

  if (hasPermission()) {
    return <>{props.children}</>
  }

  return <>{props.noPermissionMessage}</>
}
