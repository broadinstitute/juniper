import { getOidcConfig } from './authConfig'
import { UserManager } from 'oidc-client-ts'
import { Config, EnvSpec } from './api/api'
import { AuthContextProps } from 'react-oidc-context'

/**
 *
 */
export function changePasswordRedirect(config: Config, envSpec: EnvSpec, selectedLanguage: string) {
  const oidcConfig = getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cChangePasswordPolicyName)
  const userManager = new UserManager(oidcConfig)
  userManager.signinRedirect({
    redirectMethod: 'replace',
    extraQueryParams: {
      portalShortcode: envSpec.shortcode as string,
      // eslint-disable-next-line camelcase
      ui_locales: selectedLanguage
    }
  })
}

/**
 *
 */
export function signInRedirect(auth: AuthContextProps, extraQueryParams: { [key: string]: string }) {
  auth.signinRedirect({
    redirectMethod: 'replace',
    extraQueryParams
  })
}
