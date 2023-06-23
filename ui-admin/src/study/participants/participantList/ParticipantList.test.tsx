import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import ParticipantList from './ParticipantList'
import { EnrolleeSearchResult } from 'api/api'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'

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
