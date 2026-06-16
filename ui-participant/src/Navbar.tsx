import React, { useEffect } from 'react'

import Api, { getEnvSpec } from 'api/api'
import {
  getB2CLocale,
  ParticipantNavbar,
  useI18n
} from '@juniper/ui-core'
import { useUser } from 'providers/UserProvider'
import { useConfig } from 'providers/ConfigProvider'
import { UserManager } from 'oidc-client-ts'
import { getOidcConfig } from 'authConfig'
import mixpanel from 'mixpanel-browser'
import { usePortalEnv } from 'providers/PortalProvider'

type NavbarProps = JSX.IntrinsicElements['nav']

/** renders the navbar for participants */
export default function Navbar(props: NavbarProps) {
  const { user, logoutUser, proxyRelations, profile, ppUsers } = useUser()
  const { selectedLanguage, changeLanguage } = useI18n()
  const config = useConfig()
  const envSpec = getEnvSpec()

  const {
    portalEnv,
    portal,
    reloadPortal,
    localContent
  } = usePortalEnv()

  useEffect(() => {
    if (profile && profile.preferredLanguage && selectedLanguage !== profile.preferredLanguage) {
      changeLanguage(profile.preferredLanguage)
      reloadPortal()
    }
  }, [profile?.preferredLanguage, selectedLanguage, changeLanguage])


  async function updatePreferredLanguage(selectedLanguage: string) {
    //track the language change
    mixpanel.track('languageUpdated', { language: selectedLanguage, source: 'navbar' })
    const ppUser = ppUsers.find(ppUser => ppUser.profileId === profile?.id)
    if (profile && ppUser) {
      await Api.updateProfile({
        profile: { ...profile, preferredLanguage: selectedLanguage },
        ppUserId: ppUser.id
      })
    }
  }

  /** invoke B2C change password flow */
  function doChangePassword() {
    mixpanel.track('changePassword', { source: 'navbar' })
    const oidcConfig = getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cChangePasswordPolicyName)
    const userManager = new UserManager(oidcConfig)
    userManager.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        originUrl: window.location.origin,
        portalEnvironment: envSpec.envName,
        portalShortcode: envSpec.shortcode as string,
        // eslint-disable-next-line camelcase
        ui_locales: getB2CLocale(selectedLanguage)
      }
    })
  }

  /** send a logout to the api then logout */
  function doLogout() {
    mixpanel.track('userLogout', { source: 'navbar' })
    logoutUser()
  }

  return <ParticipantNavbar
    {...props}
    user={user || undefined}
    doChangePassword={doChangePassword}
    doLogout={doLogout}
    proxyRelations={proxyRelations}
    portal={portal}
    portalEnv={portalEnv}
    reloadPortal={reloadPortal}
    localContent={localContent}
    updatePreferredLanguage={updatePreferredLanguage}
  />
}

