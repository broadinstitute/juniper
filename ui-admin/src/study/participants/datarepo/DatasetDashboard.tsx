import React, { useEffect, useState } from 'react'
import { getDatasetListViewPath, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DatasetDetails, DatasetJobHistory } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faExternalLink, faArrowLeft } from '@fortawesome/free-solid-svg-icons'
import { instantToDefaultString } from '../../../util/timeUtils'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from '../../../util/tableUtils'
import { Link, useParams } from 'react-router-dom'

const columns: ColumnDef<DatasetJobHistory>[] = [{
  id: 'jobType',
  header: 'Action',
  accessorKey: 'jobType'
}, {
  id: 'status',
  header: 'Status',
  accessorKey: 'status'
}, {
  id: 'created',
  header: 'Created',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.getValue() as unknown as number)
}, {
  id: 'lastUpdated',
  header: 'Last Updated',
  accessorKey: 'lastUpdatedAt',
  cell: info => instantToDefaultString(info.getValue() as unknown as number)
}, {
  id: 'jobDetails',
  header: 'Job Details',
  accessorKey: 'tdrJobId',
  cell: info => <a href={
    `https://jade.datarepo-dev.broadinstitute.org/activity?expandedJob=${info.getValue()}`} target="_blank"
  >View job in Terra Data Repo <FontAwesomeIcon icon={faExternalLink}/></a>
}]

const DatasetDashboard = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const { currentEnvPath } = studyEnvContext
  const [datasetDetails, setDatasetDetails] = useState<DatasetDetails | null>(null)
  const [datasetJobHistory, setDatasetJobHistory] = useState<DatasetJobHistory[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const datasetName = useParams().datasetName as string

  const table = useReactTable({
    data: datasetJobHistory,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    debugTable: true
  })

  const contentHeaderStyle = {
    padding: '1em 0 0 1em',
    borderBottom: '1px solid #f6f6f6'
  }

  const loadData = async () => {
    try {
      //Fetch dataset details
      const datasetDetails = await Api.listDatasetsForStudyEnvironment(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)
      const datasetDetailsResponse = await datasetDetails.json()
      setDatasetDetails(datasetDetailsResponse.find((dataset: { datasetName: string }) =>
        dataset.datasetName === datasetName))

      //Fetch dataset job history
      const datasetJobHistory = await Api.getJobHistoryForDataset(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        datasetName)
      const datasetJobHistoryResponse = await datasetJobHistory.json()
      setDatasetJobHistory(datasetJobHistoryResponse)

      setIsLoading(false)
    } catch (e) {
      Store.addNotification(failureNotification(`Error loading dataset information`))
    }
  }

  useEffect(() => {
    loadData()
  }, [])
  return <div className="container-fluid py-3">
    <h1 className="h3">Terra Data Repo</h1>
    <Link to={getDatasetListViewPath(currentEnvPath)} className="mx-2">
      <FontAwesomeIcon icon={faArrowLeft}/> Back to dataset list
    </Link>
    <LoadingSpinner isLoading={isLoading}>
      <div className="col-12 p-3">
        <ul className="list-unstyled">
          <li className="bg-white my-3">
            <div style={contentHeaderStyle}>
              <h6>Dataset Details</h6>
            </div>
            <div className="flex-grow-1 p-3">
              <div className="form-group">
                <div className="form-group-item">
                  <label>Dataset Name:</label> { datasetDetails?.datasetName }
                  <br/>
                  <label>Dataset ID:</label> { datasetDetails?.datasetId }
                  <br/>
                  <label>Date Created:</label> { instantToDefaultString(datasetDetails?.createdAt) }
                  <br/>
                  <label>Description:</label> { datasetDetails?.description ?
                    datasetDetails?.description : <em>N/A</em> }
                </div>
                <br/>
                <a href={`https://jade.datarepo-dev.broadinstitute.org/datasets/${datasetDetails?.datasetId}`}
                  target="_blank">View dataset in Terra Data Repo <FontAwesomeIcon icon={faExternalLink}/></a>
              </div>
            </div>
          </li>
          <li className="bg-white my-3">
            <div style={contentHeaderStyle}>
              <h6>Export History</h6>
            </div>
            {basicTableLayout(table)}
          </li>
        </ul>
      </div>
    </LoadingSpinner>
  </div>
}

export default DatasetDashboard
