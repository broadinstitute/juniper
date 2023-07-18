import React from 'react'

import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import ExportDataControl from './ExportDataControl'

test('renders the file types', async () => {
  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <ExportDataControl studyEnvContext={mockStudyEnvContext()} show={true} setShow={() => {}}/>)
  render(RoutedComponent)
  expect(screen.getByText('Tab-delimted (.tsv)')).toBeInTheDocument()
  expect(screen.getByText('Excel (.xlsx)')).toBeInTheDocument()
})
