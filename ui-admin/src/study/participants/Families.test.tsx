import React from 'react'
import {
  mockEnrollee,
  mockFamily,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'
import { setupRouterTest } from '@juniper/ui-core'
import Families from 'study/participants/Families'

test('renders families', async () => {
  const enrollee = mockEnrollee()
  enrollee.families = [
    mockFamily()
  ]
  const studyEnvContext = mockStudyEnvContext()
  const { RoutedComponent } = setupRouterTest(
    <Families enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={jest.fn()}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('Families')).toBeInTheDocument()
  })
  expect(screen.getByText(enrollee.families[0].shortcode)).toBeInTheDocument()
})
