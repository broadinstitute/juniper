import React from 'react'

import KitRequests from './KitRequests'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { setupRouterTest } from '@juniper/ui-core'

test('renders kit requests', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(
    <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('Kit Requests')).toBeInTheDocument()
  })
  expect(screen.getByText('Test kit')).toBeInTheDocument()
  expect(screen.getByText('Created')).toBeInTheDocument()
  expect(screen.getByText('1234 Fake Street')).toBeInTheDocument()
})
