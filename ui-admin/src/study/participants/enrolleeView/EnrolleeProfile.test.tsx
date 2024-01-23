import React from 'react'

import EnrolleeProfile from './EnrolleeProfile'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { dateToDefaultString } from '../../../util/timeUtils'
import userEvent from '@testing-library/user-event'
import Api from '../../../api/api'


test('renders enrollee profile', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockImplementation(() => Promise.resolve([]))
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
    }}/>)
  render(RoutedComponent)

  const profile = enrollee.profile
  const mailingAddress = profile.mailingAddress

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
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockImplementation(() => Promise.resolve([]))
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
    }}/>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.clear(screen.getByPlaceholderText('Given Name'))
  await userEvent.type(screen.getByPlaceholderText('Given Name'), 'James')
  await userEvent.clear(screen.getByPlaceholderText('Family Name'))
  await userEvent.type(screen.getByPlaceholderText('Family Name'), 'Bond')
  await userEvent.clear(screen.getByPlaceholderText('City'))
  await userEvent.type(screen.getByPlaceholderText('City'), 'London')

  await userEvent.click(screen.getByText('Next: Add Justification'))

  // they are broken up by a fontawesome arrow, but if you just say exact: false,
  // screen figures it out
  expect(screen.getByText('Jonas James', { exact: false })).toBeInTheDocument()
  expect(screen.getByText('Salk Bond', { exact: false })).toBeInTheDocument()
  expect(screen.getByText('Cambridge London', { exact: false })).toBeInTheDocument()
})

test('profile update is sent appropriately with justification', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  jest.spyOn(Api, 'fetchEnrolleeAdminTasks').mockImplementation(() => Promise.resolve([]))
  jest.spyOn(Api, 'updateProfileForEnrollee')
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {
    }}/>)
  render(RoutedComponent)

  await userEvent.click(screen.getByText('Edit', { exact: false }))
  await waitFor(() => {
    expect(screen.getByPlaceholderText('Given Name')).toBeInTheDocument()
  })

  await userEvent.clear(screen.getByPlaceholderText('Given Name'))
  await userEvent.type(screen.getByPlaceholderText('Given Name'), 'James')
  await userEvent.clear(screen.getByPlaceholderText('Family Name'))
  await userEvent.type(screen.getByPlaceholderText('Family Name'), 'Bond')
  await userEvent.clear(screen.getByPlaceholderText('City'))
  await userEvent.type(screen.getByPlaceholderText('City'), 'London')

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
