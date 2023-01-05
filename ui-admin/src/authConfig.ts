import _ from 'lodash'
import { WebStorageStateStore } from 'oidc-client-ts'

/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */
const aadB2cName = process.env.REACT_APP_B2C_TENANT_NAME ? process.env.REACT_APP_B2C_TENANT_NAME : 'NAME_NEEDED'
const aadb2cClientId = process.env.REACT_APP_B2C_CLIENT_ID  ? process.env.REACT_APP_B2C_CLIENT_ID : 'ID_NEEDED'

// TODO: This is a modified copy of code from Terra UI. It could use some clean-up.
export const getOidcConfig = () => {
  const metadata = {
    // eslint-disable-next-line camelcase
    authorization_endpoint:
      `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev/oauth2/v2.0/authorize`,
    // eslint-disable-next-line camelcase
    token_endpoint:
      `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev/oauth2/v2.0/token`
  }
  return {
    authority: `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev`,
    // eslint-disable-next-line camelcase
    client_id: aadb2cClientId,
    // eslint-disable-next-line camelcase
    popup_redirect_uri: `${window.origin}/redirect-from-oauth`,
    // eslint-disable-next-line camelcase
    silent_redirect_uri: `${window.origin}/redirect-from-oauth-silent`,
    metadata,
    prompt: 'consent login',
    scope: 'openid email profile',
    loadUserInfo: false,
    stateStore: new WebStorageStateStore({ store: window.localStorage }),
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    automaticSilentRenew: true,
    // Leo's setCookie interval is currently 5 min, set refresh auth then 5 min 30 seconds to gurantee that setCookie's
    // token won't expire between 2 setCookie api calls
    accessTokenExpiringNotificationTimeInSeconds: 330,
    includeIdTokenInSilentRenew: true,
    // eslint-disable-next-line camelcase
    extraQueryParams: { access_type: 'offline' },
    // eslint-disable-next-line camelcase
    redirect_uri: ''
  }
}

export const b2cPolicies = {
  names: {
    signUpSignIn: 'b2c_1_susi',
    forgotPassword: 'b2c_1_reset',
    editProfile: 'b2c_1_edit_profile'
  },
  authorities: {
    signUpSignIn: {
      authority: `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev`
    }
  },
  authorityDomain: `https://${aadB2cName}.b2clogin.com`
}
