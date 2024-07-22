import React from 'react'

import EnrolleeProfile from './EnrolleeProfile'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { dateToDefaultString, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'
import { Store } from 'react-notifications-component'

jest.mock('user/UserProvider', () => {
  return {
    __esModule: true,
    useUser: () => {
      return {
        user: {
          superuser: true
        }
      }
    }
  }
})

jest.mock('api/api', () => ({
  ...jest.requireActual('api/api'),
  fetchEnrolleeAdminTasks: jest.fn().mockResolvedValue([]),
  updateProfileForEnrollee: jest.fn().mockResolvedValue({}),
  validateAddress: jest.fn().mockResolvedValue({})
}))

test('renders enrollee profile', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)

  const profile = enrollee.profile
  const mailingAddress = profile.mailingAddress
  await waitFor(() => expect(screen.getByText('Notes')).toBeInTheDocument())

  expect(screen.getByText(`${enrollee.profile.givenName} ${enrollee.profile.familyName}`)).toBeInTheDocument()
  expect(screen.getByText(dateToDefaultString(enrollee.profile.birthDate))).toBeInTheDocument()
  expect(screen.getByText(enrollee.profile.mailingAddress.street1)).toBeInTheDocument()
  // e.g., Boston, MA 02120
  expect(screen.getByText(
    `${mailingAddress.city}, ${mailingAddress.state} ${mailingAddress.postalCode}`
  )).toBeInTheDocument()
})

test('displays updates before submitting', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
        // nothing
      }}/>
    </MockI18nProvider>
  )
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.clear(screen.getByPlaceholderText('Given Name'))
  await userEvent.type(screen.getByPlaceholderText('Given Name'), 'James')
  await userEvent.clear(screen.getByPlaceholderText('Family Name'))
  await userEvent.type(screen.getByPlaceholderText('Family Name'), 'Bond')
  await userEvent.clear(screen.getByPlaceholderText('{city}'))
  await userEvent.type(screen.getByPlaceholderText('{city}'), 'London')

  await userEvent.click(screen.getByText('Next: Add Justification'))

  // they are broken up by a fontawesome arrow, but if you just say exact: false,
  // screen figures it out
  expect(screen.getByText('Jonas James', { exact: false })).toBeInTheDocument()
  expect(screen.getByText('Salk Bond', { exact: false })).toBeInTheDocument()
  expect(screen.getByText('Cambridge London', { exact: false })).toBeInTheDocument()
})

test('profile update is sent appropriately with justification', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  jest.spyOn(Api, 'updateProfileForEnrollee')
  jest.spyOn(Store, 'addNotification').mockReturnValue('')
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
        // nothing
      }}/>
    </MockI18nProvider>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.clear(screen.getByPlaceholderText('Given Name'))
  await userEvent.type(screen.getByPlaceholderText('Given Name'), 'James')
  await userEvent.clear(screen.getByPlaceholderText('Family Name'))
  await userEvent.type(screen.getByPlaceholderText('Family Name'), 'Bond')
  await userEvent.clear(screen.getByPlaceholderText('{city}'))
  await userEvent.type(screen.getByPlaceholderText('{city}'), 'London')

  await userEvent.click(screen.getByText('Next: Add Justification'))

  await userEvent.type(screen.getByPlaceholderText('Why are you making this change?'), 'A really great reason')

  await userEvent.click(screen.getByText('Save & Complete Change'))

  expect(Api.updateProfileForEnrollee)
    .toHaveBeenCalledWith(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode,
      {
        justification: 'A really great reason',
        profile: {
          ...enrollee.profile,
          givenName: 'James',
          familyName: 'Bond',
          mailingAddress: {
            ...enrollee.profile.mailingAddress,
            city: 'London'
          }
        }
      }
    )
})


test('shows error message on address validation', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  jest.spyOn(Api, 'validateAddress').mockResolvedValue({
    valid: false,
    invalidComponents: ['STREET_NAME']
  })

  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
        // nothing
      }}/>
    </MockI18nProvider>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.click(screen.getByText('Validate'))

  // starting part of the error
  expect(screen.getByText('{addressInvalidStreetName}')).toBeInTheDocument()

  // makes the field red
  const streetClasses = screen.getByPlaceholderText('{street1}').className
  expect(streetClasses).toContain('is-invalid')

  const countryClasses = screen.getByPlaceholderText('{city}').className
  expect(countryClasses.includes('is-invalid')).toBeFalsy()
  expect(countryClasses.includes('is-valid')).toBeFalsy()
})

test('shows modal on improvable address validation', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  jest.spyOn(Api, 'validateAddress').mockResolvedValue({
    valid: true,
    suggestedAddress: {
      street1: '415 Main St',
      city: 'Cambridge',
      country: 'USA',
      postalCode: '02142',
      street2: '',
      state: 'MA'
    }
  })

  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
        // nothing
      }}/>
    </MockI18nProvider>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.click(screen.getByText('Validate'))

  expect(screen.getByText('{suggestBetterAddressBody}')).toBeInTheDocument()
})

test('makes all fields green upon positive validation', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockResolvedValue([])
  jest.spyOn(Api, 'validateAddress').mockResolvedValue({
    valid: true
  })

  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
        // nothing
      }}/>
    </MockI18nProvider>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.click(screen.getByText('Validate'))

  const classes = screen.getByPlaceholderText('{street1}').className
  expect(classes).toContain('is-valid')
})
