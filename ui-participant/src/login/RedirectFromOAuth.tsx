import { UserManager } from 'oidc-client-ts'
import React, { useEffect } from 'react'
import { getOidcConfig } from '../authConfig'
import { useConfig } from '../providers/ConfigProvider'

export const RedirectFromOAuth = () => {
  const config = useConfig()
  const userManager = new UserManager(getOidcConfig(config.b2cTenantName, config.b2cClientId))

  const url = window.location.href
  const isSilent = window.location.pathname.startsWith('/redirect-from-oauth-silent')
  useEffect(() => {
    if (isSilent) {
      userManager.signinSilentCallback(url)
    } else {
      userManager.signinPopupCallback(url)
    }
  }, [])

  return <div>Loading...</div>
}
