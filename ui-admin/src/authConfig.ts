import _ from 'lodash'
import { WebStorageStateStore } from "oidc-client-ts";

// adapted from https://learn.microsoft.com/en-us/azure/active-directory-b2c/configure-authentication-sample-spa-app
/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */
const aadB2cName = process.env.REACT_APP_B2C_TENANT_NAME ? process.env.REACT_APP_B2C_TENANT_NAME : 'NAME_NEEDED'
const aadb2cClientId = process.env.REACT_APP_B2C_CLIENT_ID  ? process.env.REACT_APP_B2C_CLIENT_ID : 'ID_NEEDED'

export const getLocalStorage = _.once(() => {
  return window.localStorage
})

// TODO: This is a modified copy of code from Terra UI. It could use some clean-up.
export const getOidcConfig = () => {
  const metadata = {
    authorization_endpoint:
      `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev/oauth2/v2.0/authorize`,
    token_endpoint:
      `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev/oauth2/v2.0/token`
  }
  return {
    authority: `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev`,
    client_id: aadb2cClientId,
    popup_redirect_uri: `${window.origin}/redirect-from-oauth`,
    silent_redirect_uri: `${window.origin}/redirect-from-oauth-silent`,
    metadata,
    prompt: 'consent login',
    scope: 'openid email profile',
    loadUserInfo: false,
    stateStore: new WebStorageStateStore({ store: getLocalStorage() }),
    userStore: new WebStorageStateStore({ store: getLocalStorage() }),
    automaticSilentRenew: true,
    // Leo's setCookie interval is currently 5 min, set refresh auth then 5 min 30 seconds to gurantee that setCookie's
    // token won't expire between 2 setCookie api calls
    accessTokenExpiringNotificationTimeInSeconds: 330,
    includeIdTokenInSilentRenew: true,
    extraQueryParams: { access_type: 'offline' },
    redirect_uri: ''
  }
}

export const b2cPolicies = {
  names: {
    signUpSignIn: "b2c_1_susi",
    forgotPassword: "b2c_1_reset",
    editProfile: "b2c_1_edit_profile"
  },
  authorities: {
    signUpSignIn: {
      authority: `https://${aadB2cName}.b2clogin.com/${aadB2cName}.onmicrosoft.com/B2C_1A_signup_signin_dev`
    }
  },
  authorityDomain: `https://${aadB2cName}.b2clogin.com`
}
