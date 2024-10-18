import React from 'react'
import { setupRouterTest } from '@juniper/ui-core'
import {
  mockPortalContext,
  mockPortalEnvironmentConfig,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import {
  act,
  render,
  screen
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'
import { ReactNotifications } from 'react-notifications-component'
import {
  mockAdminUser,
  MockUserProvider
} from 'test-utils/user-mocking-utils'
import LoadedSettingsView from './SettingsView'

jest.mock('api/api', () => ({
  ...jest.requireActual('api/api'),
  updatePortalEnvConfig: jest.fn(() => Promise.resolve(mockPortalEnvironmentConfig())),
  updateStudyEnvironmentConfig: jest.fn(() => Promise.resolve({})),
  fetchAllowedKitTypes: jest.fn(() => Promise.resolve([])),
  fetchKitTypes: jest.fn(() => Promise.resolve([]))
}))


describe('Portal Settings', () => {
  test('renders portal settings', async () => {
    const { RoutedComponent } = setupRouterTest(<LoadedSettingsView
      studyEnvContext={mockStudyEnvContext()}
      portalContext={mockPortalContext()}
    />)

    render(RoutedComponent)

    // renders general settings

    expect(screen.getByText('General Portal Settings')).toBeInTheDocument()
    expect(screen.getByLabelText('Email source address')).toBeInTheDocument()


    // switches to website settings

    await act(async () => {
      await userEvent.click(screen.getByText('Website'))
    })

    expect(screen.queryByText('General Portal Settings')).not.toBeInTheDocument()
    expect(screen.getByText('Website Settings')).toBeInTheDocument()

    expect(screen.getByLabelText('password protected')).toBeInTheDocument()

    // switches to language settings
    await act(async () => {
      await userEvent.click(screen.getByText('Languages'))
    })

    expect(screen.queryByText('Website Settings')).not.toBeInTheDocument()

    expect(screen.getByText('Language Settings')).toBeInTheDocument()
    expect(screen.getByLabelText('Default portal language', { exact: false })).toBeInTheDocument()
  })
  test('updates portal settings', async () => {
    const mock = jest.spyOn(Api, 'updatePortalEnvConfig')

    const studyEnvContext = mockStudyEnvContext()
    const portalContext = mockPortalContext()
    const { RoutedComponent } = setupRouterTest(<>
      <ReactNotifications/>
      <LoadedSettingsView
        studyEnvContext={studyEnvContext}
        portalContext={portalContext}
      />
    </>)

    render(RoutedComponent)

    // updates general settings

    await act(async () => {
      await userEvent.clear(screen.getByLabelText('Email source address'))
      await userEvent.type(screen.getByLabelText('Email source address'), 'testing@test.com')
    })

    await act(async () => {
      await userEvent.click(screen.getByText('Save portal settings'))
    })

    expect(mock).toHaveBeenCalledWith(
      portalContext.portal.shortcode,
      studyEnvContext.currentEnv.environmentName,
      {
        'acceptingRegistration': true,
        'defaultLanguage': 'en',
        'emailSourceAddress': 'testing@test.com',
        'initialized': true,
        'password': 'broad_institute',
        'passwordProtected': false
      })
  })
})

describe('Study Settings', () => {
  test('renders study settings', async () => {
    const { RoutedComponent } = setupRouterTest(<MockUserProvider user={mockAdminUser(true)}>
      <LoadedSettingsView
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
      />
    </MockUserProvider>, ['/enrollment'])

    render(RoutedComponent)

    // renders general settings

    expect(screen.getByText('Study Enrollment Settings')).toBeInTheDocument()
    expect(screen.getByLabelText('password protected')).toBeInTheDocument()


    // switches to website settings

    await act(async () => {
      await userEvent.click(screen.getByText('Kits'))
    })

    expect(screen.queryByText('Enrollment Settings')).not.toBeInTheDocument()
    expect(screen.getByText('Kit Settings')).toBeInTheDocument()


    expect(screen.getByText('Kit types')).toBeInTheDocument()
  })
  test('updates study settings', async () => {
    const mock = jest.spyOn(Api, 'updateStudyEnvironmentConfig')

    const portalContext = mockPortalContext()
    const studyEnvContext = mockStudyEnvContext()

    const { RoutedComponent } = setupRouterTest(<MockUserProvider user={mockAdminUser(true)}>
      <LoadedSettingsView
        studyEnvContext={studyEnvContext}
        portalContext={portalContext}
      />
    </MockUserProvider>, ['/enrollment'])

    render(RoutedComponent)

    // renders general settings

    expect(screen.getByText('Study Enrollment Settings')).toBeInTheDocument()

    await act(async () => {
      await userEvent.clear(screen.getByLabelText('password'))
      await userEvent.type(screen.getByLabelText('password'), 'super_secret')
    })

    await act(async () => {
      await userEvent.click(screen.getByText('Save study settings'))
    })


    expect(mock).toHaveBeenCalledWith(
      portalContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      {
        'acceptingEnrollment': true,
        'acceptingProxyEnrollment': false,
        'enableFamilyLinkage': false,
        'initialized': true,
        'password': 'super_secret',
        'passwordProtected': false,
        'useDevDsmRealm': false,
        'useStubDsm': false,
        'enableInPersonKits': false
      }
    )
  })
})
