import {Configuration, LogLevel} from "@azure/msal-browser";

// adapted from https://learn.microsoft.com/en-us/azure/active-directory-b2c/configure-authentication-sample-spa-app
/**
 * To learn more about user flows, visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/user-flow-overview
 * To learn more about custom policies,
 *    visit: https://docs.microsoft.com/en-us/azure/active-directory-b2c/custom-policy-overview
 */
const aadB2cName = process.env.REACT_APP_B2C_TENANT_NAME ? process.env.REACT_APP_B2C_TENANT_NAME : 'NAME_NEEDED'
const aadb2cClientId = process.env.REACT_APP_B2C_CLIENT_ID  ? process.env.REACT_APP_B2C_CLIENT_ID : 'ID_NEEDED'

console.log(aadB2cName, aadb2cClientId)

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

/**
 * https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-browser/docs/configuration.md
 */
export const msalConfig: Configuration = {
  auth: {
    clientId: aadb2cClientId,
    authority: b2cPolicies.authorities.signUpSignIn.authority,
    knownAuthorities: [b2cPolicies.authorityDomain],
    redirectUri: "http://localhost:3000/redirect-from-oauth",
    // redirectUri: "https://jwt.ms/",
    postLogoutRedirectUri: "/",
    navigateToLoginRequestUrl: false
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false
  },
  system: {
    loggerOptions: {
      loggerCallback: (level: number, message: string, containsPii: boolean) => {
        // all participant logins should be assumed to contain PII, so don't log
        return;
      }
    }
  }
}

/**
 * https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-permissions-and-consent#openid-connect-scopes
 */
export const loginRequest = {
  scopes: []
}

// const b2cPolicies = {
//   names: {
//     signUpSignIn: "B2C_1_susi_reset_v2",
//     editProfile: "B2C_1_edit_profile_v2"
//   },
//   authorities: {
//     signUpSignIn: {
//       authority: "https://fabrikamb2c.b2clogin.com/fabrikamb2c.onmicrosoft.com/B2C_1_susi_reset_v2",
//     },
//     editProfile: {
//       authority: "https://fabrikamb2c.b2clogin.com/fabrikamb2c.onmicrosoft.com/B2C_1_edit_profile_v2"
//     }
//   },
//   authorityDomain: "fabrikamb2c.b2clogin.com"
// }

// export const msalConfig = {
//   auth: {
//     clientId: "bbd07d43-01cb-4b69-8fd0-5746d9a5c9fe",
//     // authority: b2cPolicies.authorities.signUpSignIn.authority,
//     // knownAuthorities: [b2cPolicies.authorityDomain],
//     redirectUri: "http://localhost:3000"
//     // You must register this URI on Azure Portal/App Registration. Defaults to "window.location.href".
//   },
//   cache: {
//     cacheLocation: "sessionStorage",
//     // Configures cache location. "sessionStorage" is more secure, but "localStorage" gives you SSO between tabs.
//     storeAuthStateInCookie: false
//     // If you wish to store cache items in cookies as well as browser cache, set this to "true".
//   },
//   system: {
//     loggerOptions: {
//       loggerCallback: (level: any, message: any, containsPii: any) => {
//         if (containsPii) {
//           return;
//         }
//         // switch (level) {
//         //   case msal.LogLevel.Error:
//         //     console.error(message);
//         //     return;
//         //   case msal.LogLevel.Info:
//         //     console.info(message);
//         //     return;
//         //   case msal.LogLevel.Verbose:
//         //     console.debug(message);
//         //     return;
//         //   case msal.LogLevel.Warning:
//         //     console.warn(message);
//         //     return;
//         // }
//       }
//     }
//   }
// }
