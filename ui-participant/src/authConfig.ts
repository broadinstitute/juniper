import { WebStorageStateStore } from 'oidc-client-ts'

/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */
const aadB2cName = process.env.REACT_APP_B2C_TENANT_NAME ? process.env.REACT_APP_B2C_TENANT_NAME : 'NAME_NEEDED'
const aadb2cClientId = process.env.REACT_APP_B2C_CLIENT_ID ? process.env.REACT_APP_B2C_CLIENT_ID : 'ID_NEEDED'

// TODO: This is a modified copy of code from Terra UI. It could use some clean-up.
/* eslint-disable camelcase, max-len */
export const getOidcConfig = (b2cTenantName: string = aadB2cName, b2cClientId: string = aadb2cClientId) => {
  return {
    /*
     * oidc-client-ts uses `authority` to fetch `metadata`. For some reason providing `metadata` manually results in not
     * getting an `access_token` back from B2C... even with a straight copy of what `authority` returns. So, while this
     * does mean an extra network call when loading the UI, for now, this is what works.
     */
    // oidc-client-ts appends /v2.0/.well-known/openid-configuration, so can't use ?p={policy} here
    authority:
      `https://${b2cTenantName}.b2clogin.com/${b2cTenantName}.onmicrosoft.com/B2C_1A_ddp_participant_signup_signin_dev`,
    client_id: b2cClientId,
    redirect_uri: `${window.origin}/redirect-from-oauth`,
    popup_redirect_uri: `${window.origin}/redirect-from-oauth`,
    silent_redirect_uri: `${window.origin}/redirect-from-oauth-silent`,
    prompt: 'consent login',
    scope: 'openid',
    loadUserInfo: false,
    stateStore: new WebStorageStateStore({ store: window.localStorage }),
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    automaticSilentRenew: true,
    // Leo's setCookie interval is currently 5 min, set refresh auth then 5 min 30 seconds to gurantee that setCookie's
    // token won't expire between 2 setCookie api calls
    accessTokenExpiringNotificationTimeInSeconds: 330,
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
