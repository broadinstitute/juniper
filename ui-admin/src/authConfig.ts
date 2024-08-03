import { WebStorageStateStore } from 'oidc-client-ts'

/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */
const aadB2cName = import.meta.env.VITE_B2C_TENANT_NAME ? import.meta.env.VITE_B2C_TENANT_NAME : 'NAME_NEEDED'
const aadb2cClientId = import.meta.env.VITE_B2C_CLIENT_ID  ? import.meta.env.VITE_B2C_CLIENT_ID : 'ID_NEEDED'
const aadb2cPolicyName = import.meta.env.VITE_B2C_POLICY_NAME ? import.meta.env.VITE_B2C_POLICY_NAME : 'POLICY_NEEDED'

// TODO: This is a modified copy of code from Terra UI. It could use some clean-up.
/* eslint-disable camelcase, max-len */
export const getOidcConfig = (
  b2cTenantName: string = aadB2cName,
  b2cClientId: string = aadb2cClientId,
  b2cPolicyName: string = aadb2cPolicyName) => {
  const metadata = {
    authorization_endpoint: `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/${b2cPolicyName}/oauth2/v2.0/authorize`,
    token_endpoint: `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/${b2cPolicyName}/oauth2/v2.0/token`,
    end_session_endpoint: `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/${b2cPolicyName}/oauth2/v2.0/logout`
  }

  return {
    authority: `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/${b2cPolicyName}`,
    client_id: b2cClientId,
    popup_redirect_uri: `${window.origin}/redirect-from-oauth`,
    silent_redirect_uri: `${window.origin}/redirect-from-oauth-silent`,
    post_logout_redirect_uri: window.origin,
    metadata,
    prompt: 'login',
    scope: `openid email ${b2cClientId}`,
    loadUserInfo: false,
    stateStore: new WebStorageStateStore({ store: window.localStorage }),
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    automaticSilentRenew: true,
    accessTokenExpiringNotificationTimeInSeconds: 300,
    includeIdTokenInSilentRenew: true,
    extraQueryParams: { access_type: 'offline' },
    redirect_uri: '',
    onSigninCallback: () => {
      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      )
    }
  }
}
``/* eslint-enable camelcase, max-len */
