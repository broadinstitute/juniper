import React from 'react'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'

import ParticipantList from './ParticipantList'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import {
  mockEnrolleeSearchExpressionResult,
  mockFamily,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import {
  Family, renderWithRouter,
  setupRouterTest
} from '@juniper/ui-core'

const mockSearchApi = (numSearchResults: number) => {
  return jest.spyOn(Api, 'executeSearchExpression')
    .mockImplementation(() => {
      const enrolleeSearchResults: EnrolleeSearchExpressionResult[] =
        new Array(numSearchResults)
          .fill(mockEnrolleeSearchExpressionResult())
      return Promise.resolve(enrolleeSearchResults)
    })
}

const mockSearchApiWithFamilies = (numSearchResults: number, numFamilies: number) => {
  const enrolleeSearchResults: EnrolleeSearchExpressionResult[] =
    new Array(numSearchResults)
      .fill(mockEnrolleeSearchExpressionResult())
  const families: Family[] = new Array(numFamilies)
    .fill(mockFamily())

  const searchSpy = jest.spyOn(Api, 'executeSearchExpression')
    .mockImplementation(() => Promise.resolve(enrolleeSearchResults))
  const familySpy = jest.spyOn(Api, 'getAllFamilies')
    .mockImplementation(() => Promise.resolve(families))

  return { searchSpy, familySpy }
}


test('renders a participant with link', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const participantLink = screen.getByText('JOSALK')
  expect(participantLink).toHaveAttribute('href', `/${studyEnvContext.currentEnvPath}/participants/JOSALK`)
  expect(screen.getByText('Created')).toBeInTheDocument()
  expect(screen.getByText('Last login')).toBeInTheDocument()
})

test('renders filters for participant columns', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //There are 3 default columns shown, 1 of which allow text search
  const searchInputs = await screen.findAllByPlaceholderText('Filter...')
  expect(searchInputs).toHaveLength(1)
})

test('filters participants based on shortcode', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Assert that JOSALK is visible in the table
  await screen.findByText('JOSALK')

  //Search for some unknown shortcode
  await userEvent.type(screen.getAllByPlaceholderText('Filter...')[0], 'UNKNOWN SHORTCODE')

  //Assert that JOSALK is no longer visible in the table
  await waitFor(() => {
    expect(screen.queryByText('JOSALK')).not.toBeInTheDocument()
  })
})

test('send email is toggled depending on participants selected', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  renderWithRouter(<ParticipantList studyEnvContext={studyEnvContext}/>)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.click(screen.getByLabelText('Actions'))
  const sendEmailButton = screen.getByText('Send email')
  expect(sendEmailButton).toHaveAttribute('aria-disabled', 'true')
})

test('add synthetic participant not shown for live environment', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  studyEnvContext.currentEnv.environmentName = 'live'
  renderWithRouter(<ParticipantList studyEnvContext={studyEnvContext}/>)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.click(screen.getByLabelText('Actions'))
  expect(screen.queryByText('Add synthetic participant')).not.toBeInTheDocument()
})

test('add synthetic participant shown for sandbox environment', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  renderWithRouter(<ParticipantList studyEnvContext={studyEnvContext}/>)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.click(screen.getByLabelText('Actions'))
  expect(screen.getByText('Add synthetic participant')).toBeInTheDocument()
})

test('keyword search sends search api request', async () => {
  jest.clearAllMocks()
  const searchSpy = mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.type(screen.getByPlaceholderText('Search by name, email, or shortcode'), 'foo')
  await userEvent.click(screen.getByTitle('submit search'))
  await waitFor(() => {
    expect(searchSpy).toHaveBeenCalledTimes(2)
  })
  expect(searchSpy).toHaveBeenNthCalledWith(2,
    'portalCode',
    'fakeStudy',
    'sandbox',
    '({profile.name} contains \'foo\' ' +
    'or {profile.contactEmail} contains \'foo\' ' +
    'or {enrollee.shortcode} contains \'foo\' ' +
    'or {family.shortcode} contains \'foo\') ' +
    'and {enrollee.subject} = true ' +
    'and include({user.lastLogin})')
})

test('allows the user to cycle pages', async () => {
  mockSearchApi(100)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Wait for results to be rendered
  await screen.findAllByText('JOSALK')

  expect(screen.getByText('Showing 10 of 100 rows')).toBeInTheDocument()
  expect(screen.getByText('Page 1 of 10')).toBeInTheDocument()

  await userEvent.click(screen.getByLabelText('Next page'))
  expect(screen.getByText('Page 2 of 10')).toBeInTheDocument()

  await userEvent.click(screen.getByLabelText('Previous page'))
  expect(screen.getByText('Page 1 of 10')).toBeInTheDocument()
})

test('allows the user to change the page size', async () => {
  mockSearchApi(100)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Wait for results to be rendered
  await screen.findAllByText('JOSALK')

  expect(screen.getByText('Showing 10 of 100 rows')).toBeInTheDocument()
  expect(screen.getByText('Page 1 of 10')).toBeInTheDocument()

  jest.spyOn(Storage.prototype, 'setItem')
  await userEvent.selectOptions(screen.getByLabelText('Number of rows per page'), '25')
  expect(screen.getByText('Showing 25 of 100 rows')).toBeInTheDocument()
  expect(screen.getByText('Page 1 of 4')).toBeInTheDocument()

  //Also assert that the preferred number of rows is saved to local storage
  expect(localStorage.setItem).toHaveBeenCalledWith('participantList.preferredNumRows', '25')
})

test('allows the user to group by family', async () => {
  mockSearchApiWithFamilies(100, 10)

  const studyEnvContext = mockStudyEnvContext()

  studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage = true
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Wait for results to be rendered
  await screen.findAllByText('JOSALK')

  await userEvent.click(screen.getByLabelText('Family view'))

  //Wait for results to be rendered
  expect(screen.getAllByText('F_MOCK')[0]).toBeInTheDocument()
})

test('ensure cannot group by family if family linkage not enabled', async () => {
  mockSearchApiWithFamilies(100, 10)

  const studyEnvContext = mockStudyEnvContext()

  studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage = false
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Wait for results to be rendered
  await screen.findAllByText('JOSALK')

  expect(screen.queryByLabelText('Family view')).not.toBeInTheDocument()
})
