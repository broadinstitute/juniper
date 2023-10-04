import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import ParticipantList from './ParticipantList'
import Api, { EnrolleeSearchResult } from 'api/api'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import { act } from 'react-dom/test-utils'
import { KEYWORD_FACET } from 'api/enrolleeSearch'

const mockSearchApi = () => {
  return jest.spyOn(Api, 'searchEnrollees')
    .mockImplementation(() => {
      const fakeEnrollee = mockEnrollee()
      const enrolleeSearchResults: EnrolleeSearchResult[] = [{
        enrollee: fakeEnrollee,
        profile: fakeEnrollee.profile,
        mostRecentKitStatus: null,
        participantUser: {
          lastLogin: 50405345,
          username: 'testUser1@test.com'
        }
      }]
      return Promise.resolve(enrolleeSearchResults)
    })
}

test('renders a participant with link', async () => {
  mockSearchApi()
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
  mockSearchApi()
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //There are 3 default columns shown, 2 of which allow text search
  const searchInputs = await screen.findAllByPlaceholderText('Filter...')
  expect(searchInputs).toHaveLength(2)
})

test('filters participants based on shortcode', async () => {
  mockSearchApi()
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
  mockSearchApi()
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const participantLink = screen.getByText('Send email')
  expect(participantLink).toHaveAttribute('aria-disabled', 'true')
})


test('keyword search sends search api request', async () => {
  const searchSpy = mockSearchApi()
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
