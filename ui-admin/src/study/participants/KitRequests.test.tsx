import React from 'react'

import KitRequests from './KitRequests'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'

test('renders kit requsets', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(
    <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('Kit requests')).toBeInTheDocument()
  })
  expect(screen.getByText('Test kit')).toBeInTheDocument()
  expect(screen.getByText('Kit Without Label')).toBeInTheDocument()
  expect(screen.getByText('1234 Fake Street')).toBeInTheDocument()
})
