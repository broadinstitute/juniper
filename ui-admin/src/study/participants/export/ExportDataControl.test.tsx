import React from 'react'

import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import ExportDataControl from './ExportDataControl'
import userEvent from '@testing-library/user-event'

test('renders the file types', async () => {
  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <ExportDataControl studyEnvContext={mockStudyEnvContext()} show={true} setShow={() => {}}/>)
  render(RoutedComponent)
  expect(screen.getByText('Tab-delimted (.tsv)')).toBeInTheDocument()
  expect(screen.getByText('Excel (.xlsx)')).toBeInTheDocument()
})

test('help page loads', async () => {
  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <ExportDataControl studyEnvContext={mockStudyEnvContext()} show={true} setShow={() => {}}/>)
  render(RoutedComponent)
  userEvent.click(screen.getByText('help page'))
  waitFor(() => expect(screen.getByText('Participant List Export Info')).toBeInTheDocument())
})
