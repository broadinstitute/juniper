import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import { DatasetDetails } from '../../../api/api'
import { mockDatasetDetails, mockStudyEnvContext } from '../../../test-utils/mocking-utils'
import DatasetList from './DatasetList'
import { setupRouterTest } from '@juniper/ui-core'

jest.mock('api/api', () => ({
  listDatasetsForStudyEnvironment: () => {
    const successfulDatasetDetails = mockDatasetDetails('successful_dataset', 'CREATED')
    const failedDatasetDetails = mockDatasetDetails('failed_dataset', 'FAILED')
    const deletingDatasetDetails = mockDatasetDetails('deleting_dataset', 'DELETING')

    const datasetList: DatasetDetails[] = [successfulDatasetDetails, failedDatasetDetails, deletingDatasetDetails]

    return Promise.resolve(datasetList)
  }
}))

test('list all datasets', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('successful_dataset')).toBeInTheDocument()
    expect(screen.getByText('deleting_dataset')).toBeInTheDocument()
    expect(screen.getByText('failed_dataset')).toBeInTheDocument()
  })
})

test('should not render links for DELETING datasets', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('deleting_dataset')).toBeInTheDocument()
  })

  const deletingLink = screen.getByText('deleting_dataset')
  expect(deletingLink).not.toHaveAttribute('href')
})

test('should render links for datasets with terminal statuses', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetList studyEnvContext={studyEnvContext}/>)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('successful_dataset')).toBeInTheDocument()
  })

  const successfulLink = screen.getByText('successful_dataset')
  expect(successfulLink).toHaveAttribute('href')
})
