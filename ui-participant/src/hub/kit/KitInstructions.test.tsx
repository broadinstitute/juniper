import React from 'react'
import { act, render, screen } from '@testing-library/react'
import KitInstructions from './KitInstructions'
import { asMockedFn, setupRouterTest } from '@juniper/ui-core'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { mockUseActiveUser } from 'test-utils/user-mocking-utils'
import { mockEnrollee } from 'test-utils/test-participant-factory'
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
      <KitInstructions/>)
    await act(async () => {
      render(RoutedComponent)
    })

    expect(screen.getByText('Sample Kit Instructions')).toBeInTheDocument()
    expect(screen.queryByText('Consent Required')).not.toBeInTheDocument()
    expect(screen.getByText('Your kit information')).toBeInTheDocument()
    expect(screen.getByLabelText('assign-qr')).toBeInTheDocument()
    expect(screen.queryByLabelText('collect-qr')).not.toBeInTheDocument()
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
      <KitInstructions/>)
    render(RoutedComponent)

    expect(screen.getByText('Sample Kit Instructions')).toBeInTheDocument()
    expect(screen.getByText('Consent Required')).toBeInTheDocument()
    expect(screen.queryByLabelText('assign-qr')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('collect-qr')).not.toBeInTheDocument()
  })
})
