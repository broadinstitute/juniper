import { findDefaultEnrollmentStudy, RedirectFromOAuth } from './RedirectFromOAuth'
import { mockStudy, mockStudyEnv } from '../test-utils/test-portal-factory'
import { PortalStudy } from '@juniper/ui-core'
import * as ReactOidc from 'react-oidc-context'
import React from 'react'
import { renderWithRouter } from 'test-utils/router-testing-utils'
import Api from '../api/api'
import { WebStorageStateStore } from 'oidc-client-ts'

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

jest.mock('react-oidc-context', () => {
  return {
    ...jest.requireActual('react-oidc-context'),
    useAuth: jest.fn().mockReturnValue({ })
  }
})

describe('handles logins', () => {
  it('registers new users', () => {
    //const authSpy = jest.spyOn(ReactOidc, 'useAuth').mockReturnValue(mockAuthProps)


    const registerSpy = jest.spyOn(Api, 'register')
    renderWithRouter(<RedirectFromOAuth/>)
  })
})

const mockAuthProps: ReactOidc.AuthContextProps =  {
  querySessionStatus: jest.fn(),
  revokeTokens: jest.fn(),
  startSilentRenew: jest.fn(),
  stopSilentRenew: jest.fn(),
  isLoading: true,
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
  'events': jest.fn()(),
  clearStaleState: jest.fn(),
  signinPopup: jest.fn(),
  signinSilent: jest.fn(),
  signinRedirect: jest.fn(),
  removeUser: jest.fn(),
  signoutPopup: jest.fn(),
  signoutRedirect: jest.fn()
}

