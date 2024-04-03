import { findDefaultEnrollmentStudy, RedirectFromOAuth } from './RedirectFromOAuth'
import { mockStudy, mockStudyEnv } from '../test-utils/test-portal-factory'
import { PortalStudy } from '@juniper/ui-core'
import { AuthContextProps } from 'react-oidc-context'
import React from 'react'
import { Logger, UserManagerEvents, UserManagerSettingsStore, WebStorageStateStore } from 'oidc-client-ts'
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


class MockLogger extends Logger {}

const mockAuthProps: AuthContextProps =  {
  'isLoading': true,
  'isAuthenticated': false,
  'settings': {
    'authority': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/fake',
    'metadataUrl': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/fake',
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
    'stateStore': new WebStorageStateStore(),
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
    'userStore': new WebStorageStateStore()
  },
  'events': new UserManagerEvents(new UserManagerSettingsStore({
    'authority': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/fake',
    'metadataUrl': 'https://juniperdemodev.b2clogin.com/juniperdemodev.onmicrosoft.com/fake',
    'client_id': '37d95cc4-7c71-465e-9fc2-66be9a54c202',
    'redirect_uri': 'https://sandbox.demo.localhost:3001/redirect-from-oauth'
  }))
}


const MockAuthContext = React.createContext<AuthContextProps>(mockAuthProps)


const MockAuthContextProvider = ({ authState, children }: {authState: AuthContextProps, children: React.ReactNode}) => {
  return <MockAuthContext.Provider value={authState}>
    { children }
  </MockAuthContext.Provider>
}
