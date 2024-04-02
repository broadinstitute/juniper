import { findDefaultEnrollmentStudy, RedirectFromOAuth } from './RedirectFromOAuth'
import { mockStudy, mockStudyEnv } from '../test-utils/test-portal-factory'
import { ApiContextT, emptyApi, PortalStudy } from '@juniper/ui-core'
import { AuthContextProps } from 'react-oidc-context'
import React from 'react'
import { WebStorageStateStore } from 'oidc-client-ts'
import { render } from '@testing-library/react'
import Api from '../api/api'

describe('determines default study', () => {
  it('find the default study if just one study', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(study)
  })

  it('does not find a default study if two studies', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study }, { study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(null)
  })

  it('finds a default study if only one is accepting enrollment', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const inactiveStudy = {
      ...mockStudy(),
      studyEnvironments: [
        {
          ...mockStudyEnv(),
          studyEnvironmentConfig: {
            ...mockStudyEnv().studyEnvironmentConfig,
            acceptingEnrollment: false
          }
        }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study: inactiveStudy }, { study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(study)
  })

  it('finds a study by shortcode', () => {
    const study = {
      ...mockStudy(),
      shortcode: 'foo',
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const study2 = {
      ...mockStudy(),
      shortcode: 'bar',
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study: study2 }, { study }]
    expect(findDefaultEnrollmentStudy('foo', portalStudies)).toBe(study)
  })

})

describe('handles logins', () => {
  it('registers new users', () => {
    const registerSpy = jest.spyOn(Api, 'register')
    render(<MockAuthContextProvider authState={mockAuthProps}>
      <RedirectFromOAuth/>
    </MockAuthContextProvider>)


  })
})

const mockAuthProps: AuthContextProps =  {
    'isLoading': true,
    'isAuthenticated': false,
    'settings': {
      'authority': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/B2C_1A_ddp_participant_signup_signin_demo-dev/v2.0',
      'metadataUrl': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/B2C_1A_ddp_participant_signup_signin_demo-dev/v2.0/.well-known/openid-configuration',
      'client_id': '37d95cc4-7c71-465e-9fc2-66be9a54c202',
      'response_type': 'code',
      'scope': 'openid email 37d95cc4-7c71-465e-9fc2-66be9a54c202',
      'redirect_uri': 'https://sandbox.demo.localhost:3001/redirect-from-oauth',
      'client_authentication': 'client_secret_post',
      'prompt': 'login',
      'response_mode': 'query',
      'filterProtocolClaims': true,
      'loadUserInfo': false,
      'staleStateAgeInSeconds': 900,
      'mergeClaims': false,
      'fetchRequestCredentials': 'same-origin',
      'stateStore': {
        set: jest.fn(),
        get: jest.fn(),
        getAllKeys: jest.fn(),
        remove: jest.fn()
      },
      'extraQueryParams': {
        'access_type': 'offline'
      },
      'extraTokenParams': {},
      'popup_redirect_uri': 'https://sandbox.demo.localhost:3001/redirect-from-oauth',
      'popupWindowFeatures': {
        'location': false,
        'toolbar': false,
        'height': 640
      },
      'popupWindowTarget': '_blank',
      'redirectMethod': 'assign',
      'redirectTarget': 'self',
      'silent_redirect_uri': 'https://sandbox.demo.localhost:3001/redirect-from-oauth-silent',
      'silentRequestTimeoutInSeconds': 10,
      'automaticSilentRenew': true,
      'validateSubOnSilentRenew': true,
      'includeIdTokenInSilentRenew': true,
      'monitorSession': false,
      'monitorAnonymousSession': false,
      'checkSessionIntervalInSeconds': 2,
      'stopCheckSessionOnError': true,
      'query_status_response_type': 'code',
      'revokeTokenTypes': [
        'access_token',
        'refresh_token'
      ],
      'revokeTokensOnSignout': false,
      'includeIdTokenInSilentSignout': false,
      'accessTokenExpiringNotificationTimeInSeconds': 300,
      'userStore': {
        '_store': {
          'cookiesAcknowledged': 'true',
          'oidc.6b680be404db4b8fbeb3376427832a33': '{\'id\':\'6b680be404db4b8fbeb3376427832a33\',\'created\':1711655193,\'request_type\':\'si:r\',\'code_verifier\':\'d4ec4be887f648868520ad13101704b5037bc15302d24b43a1e590e3366d1ac8dd30607684df43c590fb4f0eb155a9b2\',\'authority\':\'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/B2C_1A_ddp_participant_signup_signin_demo-dev/v2.0\',\'client_id\':\'37d95cc4-7c71-465e-9fc2-66be9a54c202\',\'redirect_uri\':\'https://sandbox.demo.localhost:3001/redirect-from-oauth\',\'scope\':\'openid email 37d95cc4-7c71-465e-9fc2-66be9a54c202\',\'extraTokenParams\':{},\'response_mode\':\'query\'}',
          'selectedLanguage': 'en'
        },
        set: jest.fn(),
        get: jest.fn(),
        getAllKeys: jest.fn(),
        remove: jest.fn(),
        '_prefix': 'oidc.'
      }
    },
    'events': {
      '_logger': {
        '_name': 'UserManagerEvents'
      },
      '_expiringTimer': {
        '_name': 'Access token expiring',
        '_logger': {
          '_name': 'Timer('Access token expiring')'
        },
        '_callbacks': [
          null
        ],
        '_timerHandle': null,
        '_expiration': 0
      },
      '_expiredTimer': {
        '_name': 'Access token expired',
        '_logger': {
          '_name': 'Timer('Access token expired')'
        },
        '_callbacks': [],
        '_timerHandle': null,
        '_expiration': 0
      },
      '_expiringNotificationTimeInSeconds': 300,
      '_userLoaded': {
        '_name': 'User loaded',
        '_logger': {
          '_name': 'Event('User loaded')'
        },
        '_callbacks': [
          null,
          null
        ]
      },
      '_userUnloaded': {
        '_name': 'User unloaded',
        '_logger': {
          '_name': 'Event('User unloaded')'
        },
        '_callbacks': [
          null
        ]
      },
      '_silentRenewError': {
        '_name': 'Silent renew error',
        '_logger': {
          '_name': 'Event('Silent renew error')'
        },
        '_callbacks': [
          null
        ]
      },
      '_userSignedIn': {
        '_name': 'User signed in',
        '_logger': {
          '_name': 'Event('User signed in')'
        },
        '_callbacks': []
      },
      '_userSignedOut': {
        '_name': 'User signed out',
        '_logger': {
          '_name': 'Event('User signed out')'
        },
        '_callbacks': []
      },
      '_userSessionChanged': {
        '_name': 'User session changed',
        '_logger': {
          '_name': 'Event('User session changed')'
        },
        '_callbacks': []
      }
    }
  }


const MockAuthContext = React.createContext<AuthContextProps>(mockAuthProps)

const MockAuthContextProvider = ({authState, children}: {authState: AuthContextProps, children: React.ReactNode}) => {
  return <MockAuthContext.Provider value={authState}>
    { children }
  </MockAuthContext.Provider>
}
