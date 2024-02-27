import React from 'react'
import { usePortalEnv } from 'providers/PortalProvider'
import { render, screen, waitFor } from '@testing-library/react'
import LandingPage from './LandingPage'
import { expectNever, setupRouterTest } from 'test-utils/router-testing-utils'
import {
  mockLocalSiteContent,
  mockPortal,
  mockPortalEnvironment
} from 'test-utils/test-portal-factory'
import { MockI18nProvider } from '../test-utils/i18n-testing-utils'

jest.mock('providers/PortalProvider', () => {
  return {
    ...jest.requireActual('providers/PortalProvider'),
    usePortalEnv: jest.fn()
  }
})

describe('LandingPage', () => {
  beforeEach(() => {
    // @ts-expect-error "TS doesn't know about mocks"
    usePortalEnv.mockReturnValue({
      portal: mockPortal(),
      portalEnv: mockPortalEnvironment(),
      localContent: mockLocalSiteContent()
    })
  })

  it('handles trivial landing page', () => {
    const { RoutedComponent } =
            setupRouterTest(
              <MockI18nProvider mockTexts={{}}>
                <LandingPage localContent={mockLocalSiteContent()}/>
              </MockI18nProvider>
            )
    render(RoutedComponent)
    // mailing list modal is hidden by default
    expectNever(() =>
      expect(screen.getByLabelText('Join mailing list')).toHaveAttribute('aria-hidden', 'false')
    )
  })

  it('shows mailing list modal if url param is present', () => {
    const { RoutedComponent } =
            setupRouterTest(
              <MockI18nProvider mockTexts={{}}>
                <LandingPage localContent={mockLocalSiteContent()}/>
              </MockI18nProvider>, ['?showJoinMailingList=true'])
    render(RoutedComponent)
    // mailing list modal is hidden by default
    waitFor(() => expect(screen.getByLabelText('Join mailing list'))
      .toHaveAttribute('aria-hidden', 'false'))
  })
})
