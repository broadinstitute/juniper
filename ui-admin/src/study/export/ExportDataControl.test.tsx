import React from 'react'

import { mockStudyEnvContext } from '../../test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import ExportDataModal from './ExportDataModal'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

test('renders the file types', async () => {
  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <ExportDataModal studyEnvContext={mockStudyEnvContext()} show={true} setShow={() => {}}/>)
  render(RoutedComponent)
  expect(screen.getByText('Tab-delimited (.tsv)')).toBeInTheDocument()
  expect(screen.getByText('Excel (.xlsx)')).toBeInTheDocument()
})

test('help page loads', async () => {
  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <ExportDataModal studyEnvContext={mockStudyEnvContext()} show={true} setShow={() => {}}/>)
  render(RoutedComponent)
  userEvent.click(screen.getByText('help page'))
  waitFor(() => expect(screen.getByText('Participant List Export Info')).toBeInTheDocument())
})
