import { findDefaultEnrollmentStudy, RedirectFromOAuth } from './RedirectFromOAuth'
import { mockPortal, mockStudy, mockStudyEnv } from 'test-utils/test-portal-factory'
import { PortalStudy } from '@juniper/ui-core'
import React from 'react'
import { renderWithRouter } from 'test-utils/router-testing-utils'
import Api from 'api/api'
import PortalProvider from 'providers/PortalProvider'
import { act } from '@testing-library/react'
import { User } from 'oidc-client-ts'


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

const defaultAuthUser: User = {
  // eslint-disable-next-line camelcase
  session_state: 'nothing',
  // eslint-disable-next-line camelcase
  access_token: 'bleh',
  // eslint-disable-next-line camelcase
  token_type: 'fake',
  // eslint-disable-next-line camelcase
  expires_at: 3000,
  state: '',
  expired: false,
  // eslint-disable-next-line camelcase
  expires_in: 4000,
  scopes: [],
  toStorageString: jest.fn(),
  profile: {
    email: 'jsalk@test.com',
    sub: '',
    sid: '',
    iss: '',
    aud: '',
    exp: 400,
    iat: 400
  }
}
const newAuthUser: User = {
  ...defaultAuthUser,
  profile: {
    ...defaultAuthUser.profile,
    newUser: true
  }
} as unknown as User // needed becausetypescript doesn't recognize some of the destructured props from defaultAuthUser?

const mockAuthUserContainer = { user: defaultAuthUser }
const getMockUser = () => mockAuthUserContainer.user
const setMockUser = (user: User) => { mockAuthUserContainer.user = user }

jest.mock('react-oidc-context', () => {
  return {
    ...jest.requireActual('react-oidc-context'),
    useAuth: () => ({
      querySessionStatus: jest.fn(),
      revokeTokens: jest.fn(),
      startSilentRenew: jest.fn(),
      stopSilentRenew: jest.fn(),
      isLoading: true,
      user: getMockUser(),
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
        'stateStore': jest.fn()(),
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
        'userStore': jest.fn()()
      },
      'events': jest.fn()(),
      clearStaleState: jest.fn(),
      signinPopup: jest.fn(),
      signinSilent: jest.fn(),
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
      signoutPopup: jest.fn(),
      signoutRedirect: jest.fn()
    })
  }
})

describe('handles logins', () => {
  it('registers new users', async () => {
    jest.spyOn(Api, 'getPortal').mockResolvedValue(mockPortal())
    const tokenSpy = jest.spyOn(Api, 'tokenLogin')
    const registerSpy = jest.spyOn(Api, 'register')
    setMockUser(newAuthUser)
    await act(async () => {
      renderWithRouter(<PortalProvider>
        <RedirectFromOAuth/>
      </PortalProvider>)
    })
    expect(tokenSpy).toHaveBeenCalledTimes(0)
    expect(registerSpy).toHaveBeenCalledTimes(1)
  })

  it('logs in existing users', async () => {
    jest.spyOn(Api, 'getPortal').mockResolvedValue(mockPortal())
    const tokenSpy = jest.spyOn(Api, 'tokenLogin')
    const registerSpy = jest.spyOn(Api, 'register')
    setMockUser(defaultAuthUser)
    await act(async () => {
      renderWithRouter(<PortalProvider>
        <RedirectFromOAuth/>
      </PortalProvider>)
    })
    expect(tokenSpy).toHaveBeenCalledTimes(1)
    expect(registerSpy).toHaveBeenCalledTimes(0)
  })
})

