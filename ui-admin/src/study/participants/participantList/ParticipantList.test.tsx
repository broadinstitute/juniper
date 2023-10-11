import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import ParticipantList from './ParticipantList'
import Api, { EnrolleeSearchResult } from 'api/api'
import { mockEnrolleeSearchResult, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import { act } from 'react-dom/test-utils'
import { KEYWORD_FACET } from 'api/enrolleeSearch'

const mockSearchApi = (numSearchResults: number) => {
  return jest.spyOn(Api, 'searchEnrollees')
    .mockImplementation(() => {
      const enrolleeSearchResults: EnrolleeSearchResult[] = new Array(numSearchResults).fill(mockEnrolleeSearchResult())
      return Promise.resolve(enrolleeSearchResults)
    })
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

  //There are 3 default columns shown, 2 of which allow text search
  const searchInputs = await screen.findAllByPlaceholderText('Filter...')
  expect(searchInputs).toHaveLength(2)
})

test('filters participants based on shortcode', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Assert that JOSALK is visible in the table
  await screen.findByText('JOSALK')

  //Search for some unknown shortcode
  await act(() =>
    userEvent.type(screen.getAllByPlaceholderText('Filter...')[0], 'UNKNOWN SHORTCODE')
  )

  //Assert that JOSALK is no longer visible in the table
  await waitFor(() => {
    expect(screen.queryByText('JOSALK')).not.toBeInTheDocument()
  })
})

test('send email is toggled depending on participants selected', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const sendEmailButton = screen.getByText('Send email')
  expect(sendEmailButton).toBeDisabled()
})

test('download button is toggled depending on if there are participants or not', async () => {
  mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('Showing 0 of 0 rows')).toBeInTheDocument()
  })
  const downloadButton = screen.getByLabelText('Download table data')
  expect(downloadButton).toBeDisabled()
})

test('clicking the download button prompts the user', async () => {
  mockSearchApi(43)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const sendEmailButton = screen.getByText('Download')
  expect(sendEmailButton).toBeEnabled()
  await act(() => userEvent.click(sendEmailButton))
  expect(screen.getByText('This will download 43 rows')).toBeInTheDocument()
})


test('keyword search sends search api request', async () => {
  const searchSpy = mockSearchApi(1)
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  await userEvent.type(screen.getByTitle('search name, email and shortcode'), 'foo')
  await userEvent.click(screen.getByTitle('submit search'))
  expect(searchSpy).toHaveBeenCalledTimes(2)
  expect(searchSpy).toHaveBeenNthCalledWith(2, 'portalCode', 'fakeStudy', 'sandbox', [
    { facet: KEYWORD_FACET, values: ['foo'] }
  ])
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

  await act(() => userEvent.click(screen.getByLabelText('Next page')))
  expect(screen.getByText('Page 2 of 10')).toBeInTheDocument()

  await act(() => userEvent.click(screen.getByLabelText('Previous page')))
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
  await act(() => userEvent.selectOptions(screen.getByLabelText('Number of rows per page'), '25'))
  expect(screen.getByText('Showing 25 of 100 rows')).toBeInTheDocument()
  expect(screen.getByText('Page 1 of 4')).toBeInTheDocument()

  //Also assert that the preferred number of rows is saved to local storage
  expect(localStorage.setItem).toHaveBeenCalledWith('participantList.portalCode.fakeStudy.preferredNumRows', '25')
})
