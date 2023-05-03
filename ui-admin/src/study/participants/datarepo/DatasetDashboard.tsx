import React, { useEffect, useMemo, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DatasetDetails, DatasetJobHistory } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { instantToDefaultString } from '../../../util/timeUtils'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { sortableTableHeader } from '../../../util/tableUtils'
import { Link } from 'react-router-dom'

const DatasetDashboard = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [datasetDetails, setDatasetDetails] = useState<DatasetDetails | null>(null)
  const [datasetJobHistory, setDatasetJobHistory] = useState<DatasetJobHistory[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const datasetName = 'd2p_mbemis_1683138642532_ourheart_sandbox'

  const columns = useMemo<ColumnDef<DatasetJobHistory, string>[]>(() => [{
    id: 'select'
  }, {
    id: 'jobType',
    header: 'Action',
    accessorKey: 'jobType',
    cell: info => info.getValue() === 'CREATE_DATASET' ? 'Dataset Created' : 'Data Exported'
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
    header: 'Data Repo Job ID',
    accessorKey: 'tdrJobId',
    cell: info => <a href={
      `https://jade.datarepo-dev.broadinstitute.org/activity?expandedJob=${info.getValue()}`} target="_blank"
    >{info.getValue()} <FontAwesomeIcon icon={faExternalLink}/></a>
  }], [datasetJobHistory?.length])

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
      const datasetDetails = await Api.getDatasetsForStudyEnvironment(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)
      const datasetDetailsResponse = await datasetDetails.json()
      setDatasetDetails(datasetDetailsResponse)

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
      Store.addNotification(failureNotification(`Error loading data repo`))
    }
  }

  useEffect(() => {
    loadData()
  }, [])
  return <div className="container-fluid py-3">
    <h1 className="h3">Terra Data Repo</h1>
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
                  <label>Created Date:</label> { instantToDefaultString(datasetDetails?.createdAt) }
                  <br/>
                  <label>Last Successful Export:</label> { instantToDefaultString(datasetDetails?.lastExported) }
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
            <table className="table table-striped">
              <thead>
                <tr>
                  {table.getFlatHeaders().map(header => sortableTableHeader(header))}
                </tr>
              </thead>
              <tbody>
                {table.getRowModel().rows.map(row => {
                  return (
                    <tr key={row.id}>
                      {row.getVisibleCells().map(cell => {
                        return (
                          <td key={cell.id}>
                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                          </td>
                        )
                      })}
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </li>
        </ul>
      </div>
    </LoadingSpinner>
  </div>
}

export default DatasetDashboard
