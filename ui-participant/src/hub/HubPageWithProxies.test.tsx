import {
  fireEvent,
  render,
  screen,
  waitFor
} from '@testing-library/react'
import React from 'react'
import HubPage from './HubPage'
import {
  MockI18nProvider,
  setupRouterTest
} from '@juniper/ui-core'
import ProvideFullTestUserContext from 'test-utils/ProvideFullTestUserContext'
import {
  mockEnrolleesWithProxies,
  mockPortal,
  mockPpUsersWithProxies,
  mockRelations,
  mockUser
} from 'test-utils/test-proxy-environment'


jest.mock('../api/api', () => {
  return {
    getPortalEnvDashboardAlerts: () => Promise.resolve([]),
    getPortal: () => Promise.resolve(mockPortal),
    listOutreachActivities: () => Promise.resolve([])
  }
})

jest.mock('mixpanel-browser')

describe('HubPage with proxies', () => {
  it('is rendered with participant selector and study name', async () => {
    const { RoutedComponent } = setupRouterTest(
      <ProvideFullTestUserContext
        enrollees={mockEnrolleesWithProxies}
        ppUsers={mockPpUsersWithProxies}
        proxyRelations={mockRelations}
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
        await screen.findByLabelText('{selectParticipant}')
      ).toHaveTextContent('Peter Salk'))
  })
  it('switches between proxies', async () => {
    const { RoutedComponent } = setupRouterTest(
      <ProvideFullTestUserContext
        enrollees={mockEnrolleesWithProxies}
        ppUsers={mockPpUsersWithProxies}
        proxyRelations={mockRelations}
        user={mockUser}
        portal={mockPortal}
      >
        <MockI18nProvider>
          <HubPage/>
        </MockI18nProvider>
      </ProvideFullTestUserContext>
    )
    render(RoutedComponent)

    expect(await screen.findByLabelText('{selectParticipant}')).toHaveTextContent('Peter Salk')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeInTheDocument()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeNull()
    selectParticipant('Jonas Salk {youInParens}')
    await waitFor(() => expect(screen.getByText('Test Study')).toBeInTheDocument())
    expect(await screen.findByLabelText('{selectParticipant}')).toHaveTextContent('Jonas Salk {youInParens}')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeNull()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeNull()
    expect(screen.queryByText('{joinStudy}')).toBeInTheDocument() // should be a join study link
    selectParticipant('Jonathan Salk')
    expect(await screen.findByLabelText('{selectParticipant}')).toHaveTextContent('Jonathan Salk')
    expect(screen.queryByText('{test-demographics-survey:0}')).toBeNull()
    expect(screen.queryByText('{test-consent-survey:0}')).toBeInTheDocument()
  })
})

const selectParticipant = (name: string) => {
  const select = screen.getByLabelText('{selectParticipant}')
  fireEvent.click(select)

  const option = screen.getByText(name)
  fireEvent.click(option)
}
