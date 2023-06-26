import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import ParticipantList from './ParticipantList'
import { EnrolleeSearchResult } from 'api/api'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import { act } from 'react-dom/test-utils'

jest.mock('api/api', () => ({
  searchEnrollees: () => {
    const fakeEnrollee = mockEnrollee()
    const enrolleeSearchResults: EnrolleeSearchResult[] = [{
      enrollee: fakeEnrollee,
      profile: fakeEnrollee.profile,
      mostRecentKitStatus: null
    }]
    return Promise.resolve(enrolleeSearchResults)
  }
}))

test('renders a participant with link', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const participantLink = screen.getByText('JOSALK')
  expect(participantLink).toHaveAttribute('href', `/${studyEnvContext.currentEnvPath}/participants/JOSALK`)
})

test('renders filters for participant columns', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Assert that all 3 default columns have filter inputs
  await waitFor(() => {
    expect(screen.getAllByPlaceholderText('Search...')).toHaveLength(3)
  })
})

test('filters participants based on shortcode', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)

  //Assert that JOSALK is visible in the table
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })

  //Search for some unknown shortcode
  await act(() =>
    userEvent.type(screen.getAllByPlaceholderText('Search...')[0], 'UNKNOWN SHORTCODE')
  )

  //Assert that JOSALK is no longer visible in the table
  await waitFor(() => {
    expect(screen.queryByText('JOSALK')).not.toBeInTheDocument()
  })
})

test('send email is toggled depending on participants selected', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(<ParticipantList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('JOSALK')).toBeInTheDocument()
  })
  const participantLink = screen.getByText('Send email')
  expect(participantLink).toHaveAttribute('aria-disabled', 'true')
})
