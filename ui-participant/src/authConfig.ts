import { WebStorageStateStore } from 'oidc-client-ts'
import { AuthProviderProps } from 'react-oidc-context'

/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */

// TODO: This is a modified copy of code from Terra UI. It could use some clean-up.
/* eslint-disable camelcase, max-len */
export const getOidcConfig = (b2cTenantName: string, b2cClientId: string, b2cPolicyName: string): AuthProviderProps => {
  return {
    /*
     * oidc-client-ts uses `authority` to fetch `metadata`. For some reason providing `metadata` manually results in not
     * getting an `access_token` back from B2C... even with a straight copy of what `authority` returns. So, while this
     * does mean an extra network call when loading the UI, for now, this is what works.
     */
    // oidc-client-ts appends /v2.0/.well-known/openid-configuration, so can't use ?p={policy} here
    authority:
      `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/${b2cPolicyName}/v2.0`,
    client_id: b2cClientId,
    redirect_uri: `${window.origin}/redirect-from-oauth`,
    popup_redirect_uri: `${window.origin}/redirect-from-oauth`,
    silent_redirect_uri: `${window.origin}/redirect-from-oauth-silent`,
    prompt: 'login',
    scope: `openid email ${b2cClientId}`,
    loadUserInfo: false,
    stateStore: new WebStorageStateStore({ store: window.localStorage }),
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    automaticSilentRenew: true,
    accessTokenExpiringNotificationTimeInSeconds: 300,
    includeIdTokenInSilentRenew: true,
    extraQueryParams: { access_type: 'offline' },
    // from https://github.com/authts/react-oidc-context/blob/f175dcba6ab09871b027d6a2f2224a17712b67c5/src/AuthProvider.tsx#L20-L30
    onSigninCallback: () => {
      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      )
    }
  }
}
/* eslint-enable camelcase, max-len */
