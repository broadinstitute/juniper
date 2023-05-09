import React, { useEffect, useMemo, useState } from 'react'
import { getDatasetDashboardPath, getDatasetListViewPath, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
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
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { useUser } from '../../../user/UserProvider'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import CreateDatasetModal from './CreateDatasetModal'

const DatasetList = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const { currentEnvPath } = studyEnvContext
  const [showCreateDatasetModal, setShowCreateDatasetModal] = useState(false)
  const [datasets, setDatasets] = useState<DatasetDetails[]>([])
  const [jobs, setJobs] = useState<DatasetJobHistory[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [datasetsSorting, setDatasetsSorting] = React.useState<SortingState>([])
  const [jobsSorting, setJobsSorting] = React.useState<SortingState>([])
  const { user } = useUser()

  const datasetColumns = useMemo<ColumnDef<DatasetDetails, string>[]>(() => {
    return [{
      id: 'select'
    }, {
      id: 'datasetName',
      header: 'Dataset Name',
      accessorKey: 'datasetName',
      cell: info => <Link to={getDatasetDashboardPath(info.getValue() as unknown as string, currentEnvPath)}
        className="mx-2">{info.getValue() as unknown as string}</Link>
    }, {
      id: 'datasetUuid',
      header: 'Dataset ID',
      accessorKey: 'datasetId'
    }, {
      id: 'created',
      header: 'Created Date',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as unknown as number)
    }, {
      id: 'lastUpdated',
      header: 'Last Export',
      accessorKey: 'lastExported',
      cell: info => instantToDefaultString(info.getValue() as unknown as number)
    }, {
      header: 'Terra Data Repo',
      accessorKey: 'datasetId',
      cell: info => <a href={
        `https://jade.datarepo-dev.broadinstitute.org/datasets/${info.getValue()}`} target="_blank"
      >View in Terra Data Repo <FontAwesomeIcon icon={faExternalLink}/></a>
    }]
  }, [datasets?.length])

  const datasetTable = useReactTable({
    data: datasets,
    columns: datasetColumns,
    state: {
      sorting: datasetsSorting
    },
    onSortingChange: setDatasetsSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const jobColumns = useMemo<ColumnDef<DatasetJobHistory, string>[]>(() => [{
    id: 'selectjob'
  }, {
    id: 'datasetName',
    header: 'Dataset Name',
    accessorKey: 'datasetName'
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
    header: 'Data Repo Job ID',
    accessorKey: 'tdrJobId',
    cell: info => <a href={
      `https://jade.datarepo-dev.broadinstitute.org/activity?expandedJob=${info.getValue()}`} target="_blank"
    >{info.getValue()} <FontAwesomeIcon icon={faExternalLink}/></a>
  }], [jobs?.length])

  const jobTable = useReactTable({
    data: jobs.filter(x => x.jobType === 'CREATE_DATASET'),
    columns: jobColumns,
    state: {
      sorting: jobsSorting
    },
    onSortingChange: setJobsSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const contentHeaderStyle = {
    padding: '1em 0 0 1em',
    borderBottom: '1px solid #f6f6f6'
  }

  const loadData = async () => {
    try {
      //Fetch datasets
      const datasets = await Api.getDatasetsForStudyEnvironment(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)
      const datasetsResponse = await datasets.json()
      setDatasets(datasetsResponse)

      //Fetch jobs
      const jobHistory = await Api.getJobHistoryForStudyEnvironment(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)
      const jobHistoryResponse = await jobHistory.json()
      setJobs(jobHistoryResponse)

      setIsLoading(false)
    } catch (e) {
      Store.addNotification(failureNotification(`Error loading datasets`))
    }
  }

  useEffect(() => {
    loadData()
  }, [])
  return <div className="container-fluid py-3">
    <h1 className="h3">Study Environment Datasets</h1>
    { user.superuser &&
        <button className="btn btn-secondary" onClick={() => setShowCreateDatasetModal(!showCreateDatasetModal)}
          aria-label="show or hide export modal">
          <FontAwesomeIcon icon={faPlus}/> Create new dataset
        </button>
    }
    <CreateDatasetModal studyEnvContext={studyEnvContext}
      show={showCreateDatasetModal}
      setShow={setShowCreateDatasetModal}/>
    <LoadingSpinner isLoading={isLoading}>
      <div className="col-12 p-3">
        <ul className="list-unstyled">
          <li className="bg-white my-3">
            <div style={contentHeaderStyle}>
              <h6>Datasets</h6>
            </div>
            <table className="table table-striped">
              <thead>
                <tr>
                  {datasetTable.getFlatHeaders().map(header => sortableTableHeader(header))}
                </tr>
              </thead>
              <tbody>
                {datasetTable.getRowModel().rows.map(row => {
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
          {/*<li className="bg-white my-3">*/}
          {/*  <div style={contentHeaderStyle}>*/}
          {/*    <h6>Dataset Creation History</h6>*/}
          {/*  </div>*/}
          {/*  <table className="table table-striped">*/}
          {/*    <thead>*/}
          {/*      <tr>*/}
          {/*        {jobTable.getFlatHeaders().map(header => sortableTableHeader(header))}*/}
          {/*      </tr>*/}
          {/*    </thead>*/}
          {/*    <tbody>*/}
          {/*      {jobTable.getRowModel().rows.map(row => {*/}
          {/*        return (*/}
          {/*          <tr key={row.id}>*/}
          {/*            {row.getVisibleCells().map(cell => {*/}
          {/*              return (*/}
          {/*                <td key={cell.id}>*/}
          {/*                  {flexRender(cell.column.columnDef.cell, cell.getContext())}*/}
          {/*                </td>*/}
          {/*              )*/}
          {/*            })}*/}
          {/*          </tr>*/}
          {/*        )*/}
          {/*      })}*/}
          {/*    </tbody>*/}
          {/*  </table>*/}
          {/*</li>*/}
        </ul>
      </div>
    </LoadingSpinner>
  </div>
}

export default DatasetList
