import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import ParticipantList from './ParticipantList'
import Api, { EnrolleeSearchExpressionResult, EnrolleeSearchFacet } from 'api/api'
import { mockEnrolleeSearchExpressionResult, mockStudyEnvContext, mockTaskSearchFacet } from 'test-utils/mocking-utils'
import userEvent from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

const mockSearchApi = (numSearchResults: number) => {
  return jest.spyOn(Api, 'executeSearchExpression')
    .mockImplementation(() => {
      const enrolleeSearchResults: EnrolleeSearchExpressionResult[] =
        new Array(numSearchResults)
          .fill(mockEnrolleeSearchExpressionResult())
      return Promise.resolve(enrolleeSearchResults)
    })
}

const mockGetFacetsApi = () => {
  return jest.spyOn(Api, 'getSearchFacets')
    .mockImplementation(() => {
      const searchFacets: EnrolleeSearchFacet[] = new Array(mockTaskSearchFacet())
      return Promise.resolve(searchFacets)
    })
}

test('renders a participant with link', async () => {
  mockGetFacetsApi()
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
  mockGetFacetsApi()
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //There are 3 default columns shown, 2 of which allow text search
  const searchInputs = await screen.findAllByPlaceholderText('Filter...')
  expect(searchInputs).toHaveLength(2)
})

test('filters participants based on shortcode', async () => {
  mockGetFacetsApi()
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
  mockGetFacetsApi()
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const sendEmailButton = screen.getByText('Send email')
  expect(sendEmailButton).toHaveAttribute('aria-disabled', 'true')
})

test('keyword search sends search api request', async () => {
  mockGetFacetsApi()
  const searchSpy = mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.type(screen.getByPlaceholderText('Search by name, email, or shortcode'), 'foo')
  await userEvent.click(screen.getByTitle('submit search'))
  expect(searchSpy).toHaveBeenCalledTimes(4)
  expect(searchSpy).toHaveBeenNthCalledWith(4,
    'portalCode',
    'fakeStudy',
    'sandbox',
    '({profile.name} contains \'foo\' '
    + 'or {profile.contactEmail} contains \'foo\''
    + ' or {enrollee.shortcode} contains \'foo\')'
  )
})

test('allows the user to cycle pages', async () => {
  mockGetFacetsApi()
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
  mockGetFacetsApi()
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
