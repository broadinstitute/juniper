import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import DatasetDashboard from './DatasetDashboard'
import { DatasetDetails, DatasetJobHistory } from '../../../api/api'
import { mockDatasetDetails, mockStudyEnvContext } from '../../../test-utils/mocking-utils'
import { useParams } from 'react-router-dom'
import { setupRouterTest } from '@juniper/ui-core'

jest.mock('api/api', () => ({
  listDatasetsForStudyEnvironment: () => {
    const successfulDatasetDetails = mockDatasetDetails('successful_dataset', 'CREATED')
    const failedDatasetDetails = mockDatasetDetails('failed_dataset', 'FAILED')
    const deletingDatasetDetails = mockDatasetDetails('deleting_dataset', 'DELETING')

    const datasetList: DatasetDetails[] = [successfulDatasetDetails, failedDatasetDetails, deletingDatasetDetails]

    return Promise.resolve(datasetList)
  },

  getJobHistoryForDataset: () => {
    const jobHistory: DatasetJobHistory[] = []
    return Promise.resolve(jobHistory)
  }
}))

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn()
}))

test('renders a link to TDR if the dataset successful created', async () => {
  // avoid cluttering the console with the info messages from the table creation
  jest.spyOn(console, 'info').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  ;(useParams as jest.Mock).mockReturnValue({ datasetName: 'successful_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('successful_dataset', { exact: false })).toBeInTheDocument()
  })

  const participantLink = screen.getByText('View dataset in Terra Data Repo')
  expect(participantLink).toHaveAttribute(
    'href', `https://jade.datarepo-dev.broadinstitute.org/datasets/a-fake-tdr-dataset-id`
  )
})

test('does not render a link to TDR if the dataset failed to create', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  ;(useParams as jest.Mock).mockReturnValue({ datasetName: 'failed_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('failed_dataset', { exact: false })).toBeInTheDocument()
  })

  expect(screen.queryByText('View dataset in Terra Data Repo')).not.toBeInTheDocument()
})

test('does not render a Delete Dataset button for datasets in DELETING state', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  ;(useParams as jest.Mock).mockReturnValue({ datasetName: 'deleting_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('deleting_dataset', { exact: false })).toBeInTheDocument()
  })

  expect(screen.queryByText('Delete dataset')).not.toBeInTheDocument()
})

test('renders the dataset details', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  ;(useParams as jest.Mock).mockReturnValue({ datasetName: 'successful_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('successful_dataset', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('a successfully created dataset', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('0b9ade05-f7e3-483e-b85a-43deac7505c0', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('5/31/2023', { exact: false })).toBeInTheDocument()
  })
})
