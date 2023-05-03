import React, { useEffect, useMemo, useState } from 'react'
import {getDatasetDashboardPath, getDatasetListViewPath, StudyEnvContextT} from 'study/StudyEnvironmentRouter'
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
import DatasetDashboard from "./DatasetDashboard";

const DatasetList = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const { currentEnvPath } = studyEnvContext
  const [datasets, setDatasets] = useState<DatasetDetails[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])

  const columns = useMemo<ColumnDef<DatasetDetails, string>[]>(() => {
    return [{
      id: 'select'
    }, {
      id: 'jobType',
      header: 'Dataset ID',
      accessorKey: 'datasetId',
      cell: info => <Link to={getDatasetDashboardPath(info.getValue() as unknown as string, currentEnvPath)}
                          className="mx-2">{info.getValue() as unknown as string}</Link>
    }, {
      id: 'created',
      header: 'Created',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as unknown as number)
    }, {
      id: 'lastUpdated',
      header: 'Last Updated',
      accessorKey: 'lastExported',
      cell: info => instantToDefaultString(info.getValue() as unknown as number)
    }, {
      header: 'Terra Data Repo',
      accessorKey: 'datasetId',
      cell: info => <a href={
        `https://jade.datarepo-dev.broadinstitute.org/datasets/${info.getValue()}`} target="_blank"
      >View in Terra Data Repo <FontAwesomeIcon icon={faExternalLink}/></a>
    }];
  }, [datasets?.length])

  const table = useReactTable({
    data: datasets,
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
      //Fetch datasets
      const datasets = await Api.getDatasetsForStudyEnvironment(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)
      const datasetsResponse = await datasets.json()
      setDatasets(datasetsResponse)

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

export default DatasetList
