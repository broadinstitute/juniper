import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import HubPage from './HubPage'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { MockI18nProvider } from '@juniper/ui-core'
import ProvideFullTestUserContext from 'test-utils/ProvideFullTestUserContext'
import {
  mockEnrolleesWithProxies,
  mockPortal,
  mockPpUsersWithProxies,
  mockRelations,
  mockUser
} from '../test-utils/test-proxy-environment'


jest.mock('../api/api', () => {
  return {
    getPortalEnvDashboardAlerts: () => Promise.resolve([]),
    getPortal: () => Promise.resolve(mockPortal),
    listOutreachActivities: () => Promise.resolve([])
  }
})


describe('HubPage with proxies', () => {
  it('is rendered with participant selector and study name', async () => {
    const { RoutedComponent } = setupRouterTest(
      <ProvideFullTestUserContext
        enrollees={mockEnrolleesWithProxies}
        ppUsers={mockPpUsersWithProxies}
        relations={mockRelations}
        user={mockUser}
        portal={mockPortal}
      >
        <MockI18nProvider>
          <HubPage/>
        </MockI18nProvider>
      </ProvideFullTestUserContext>
    )
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('Test Study')).toBeInTheDocument())
    await waitFor(
      async () => expect(
        await screen.findByLabelText('Select participant')
      ).toHaveTextContent('Jonas Salk {youInParens}'))
  })
  it('switches between proxies', async () => {
    const { RoutedComponent } = setupRouterTest(
      <ProvideFullTestUserContext
        enrollees={mockEnrolleesWithProxies}
        ppUsers={mockPpUsersWithProxies}
        relations={mockRelations}
        user={mockUser}
        portal={mockPortal}
        activePpUserId={'test-psalk-pp-user'}
      >
        <MockI18nProvider>
          <HubPage/>
        </MockI18nProvider>
      </ProvideFullTestUserContext>
    )
    render(RoutedComponent)

    await waitFor(() => expect(screen.getByText('Test Study')).toBeInTheDocument())
    expect(await screen.findByLabelText('Select participant')).toHaveTextContent('Jonas Salk {youInParens}')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeNull()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeNull()
    selectParticipant('Peter Salk')
    expect(await screen.findByLabelText('Select participant')).toHaveTextContent('Peter Salk')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeInTheDocument()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeNull()
    selectParticipant('Jonathan Salk')
    expect(await screen.findByLabelText('Select participant')).toHaveTextContent('Jonathan Salk')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeNull()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeInTheDocument()
  })
})

const selectParticipant = (name: string) => {
  const select = screen.getByLabelText('Select participant')
  fireEvent.click(select)

  const option = screen.getByText(name)
  fireEvent.click(option)
}
