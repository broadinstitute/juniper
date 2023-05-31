import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import DatasetDashboard from './DatasetDashboard'
import { DatasetDetails, DatasetJobHistory } from 'api/api'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockDatasetDetails, mockStudyEnvContext } from 'test-utils/mocking-utils'
import Router from 'react-router-dom'

jest.mock('api/api', () => ({
  listDatasetsForStudyEnvironment: () => {
    const successfulDatasetDetails = mockDatasetDetails('successful_dataset', 'CREATED')
    const failedDatasetDetails = mockDatasetDetails('failed_dataset', 'FAILED')

    const datasetList: DatasetDetails[] = [successfulDatasetDetails, failedDatasetDetails]

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
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  jest.spyOn(Router, 'useParams').mockReturnValue({ datasetName: 'successful_dataset' })
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
  jest.spyOn(Router, 'useParams').mockReturnValue({ datasetName: 'failed_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('failed_dataset', { exact: false })).toBeInTheDocument()
  })

  expect(screen.queryByText('View dataset in Terra Data Repo')).not.toBeInTheDocument()
})

test('renders the dataset details', async () => {
  const studyEnvContext = mockStudyEnvContext()

  const { RoutedComponent } = setupRouterTest(<DatasetDashboard studyEnvContext={studyEnvContext}/>)
  jest.spyOn(Router, 'useParams').mockReturnValue({ datasetName: 'successful_dataset' })
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('successful_dataset', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('a successfully created dataset', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('study.admin@test.com', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('5/31/2023', { exact: false })).toBeInTheDocument()
  })
})
