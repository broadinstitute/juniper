import React from 'react'
import { act, render, screen } from '@testing-library/react'
import KitInstructions from './KitInstructions'
import { asMockedFn, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { mockUseActiveUser } from 'test-utils/user-mocking-utils'
import { mockAssignedKitRequest, mockEnrollee } from 'test-utils/test-participant-factory'
import { usePortalEnv } from 'providers/PortalProvider'
import { mockUsePortalEnv } from 'test-utils/test-portal-factory'

jest.mock('providers/ActiveUserProvider')
jest.mock('providers/PortalProvider')

describe('KitInstructions', () => {
  it('renders kit instructions for a consented enrollee', async () => {
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      enrollees: [{
        ...mockEnrollee(),
        profileId: mockUseActiveUser().ppUser?.profileId || '',
        consented: true
      }]
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitInstructions/>
      </MockI18nProvider>)
    await act(async () => {
      render(RoutedComponent)
    })

    expect(screen.getByText('{kitsInPersonTitle}')).toBeInTheDocument()
    expect(screen.queryByText('{kitsInPersonConsentRequiredTitle}')).not.toBeInTheDocument()
    expect(screen.getByText('{kitsPageInPersonTitle}')).toBeInTheDocument()
    expect(screen.getByLabelText('shortcode-qr')).toBeInTheDocument()
  })

  it('renders in-person kit information', async () => {
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      enrollees: [{
        ...mockEnrollee(),
        profileId: mockUseActiveUser().ppUser?.profileId || '',
        consented: true,
        kitRequests: [mockAssignedKitRequest('CREATED', 'SALIVA')]
      }]
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitInstructions/>
      </MockI18nProvider>)
    await act(async () => {
      render(RoutedComponent)
    })

    expect(screen.getByText('{kitsInPersonTitle}')).toBeInTheDocument()
    expect(screen.queryByText('{kitsInPersonConsentRequiredTitle}')).not.toBeInTheDocument()
    expect(screen.getByText('{kitsInPersonYourKitTitle}')).toBeInTheDocument()
    expect(screen.getByDisplayValue('assigned-label')).toBeInTheDocument()
    expect(screen.getByLabelText('shortcode-qr')).toBeInTheDocument()
  })

  it('renders collected kit notice', async () => {
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      enrollees: [{
        ...mockEnrollee(),
        profileId: mockUseActiveUser().ppUser?.profileId || '',
        consented: true,
        kitRequests: [mockAssignedKitRequest('COLLECTED_BY_STAFF', 'SALIVA')]
      }]
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitInstructions/>
      </MockI18nProvider>)
    await act(async () => {
      render(RoutedComponent)
    })

    expect(screen.getByText('{kitsInPersonTitle}')).toBeInTheDocument()
    expect(screen.queryByText('{kitsInPersonConsentRequiredTitle}')).not.toBeInTheDocument()
    expect(screen.getByText('{kitsInPersonYourKitTitle}')).toBeInTheDocument()
    expect(screen.queryByDisplayValue('assigned-label')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('shortcode-qr')).not.toBeInTheDocument()
    expect(screen.getByText('{kitsInPersonCollectedDescription}')).toBeInTheDocument()
  })

  it('renders Consent Required message if enrollee has not consented', () => {
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      enrollees: [{
        ...mockUseActiveUser().enrollees[0],
        profileId: mockUseActiveUser().ppUser?.profileId || '',
        consented: false,
        kitRequests: []
      }]
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitInstructions/>
      </MockI18nProvider>)
    render(RoutedComponent)

    expect(screen.getByText('{kitsInPersonTitle}')).toBeInTheDocument()
    expect(screen.getByText('{kitsInPersonConsentRequiredTitle}')).toBeInTheDocument()
    expect(screen.queryByLabelText('shortcode-qr')).not.toBeInTheDocument()
  })
})
