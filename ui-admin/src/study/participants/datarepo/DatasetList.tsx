import React, { useEffect, useState } from 'react'
import { getDatasetDashboardPath, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DatasetDetails } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from '../../../util/timeUtils'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from '../../../util/tableUtils'
import { Link } from 'react-router-dom'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { useUser } from '../../../user/UserProvider'
import CreateDatasetModal from './CreateDatasetModal'

const datasetColumns = (currentEnvPath: string): ColumnDef<DatasetDetails>[] => [{
  id: 'datasetName',
  header: 'Dataset Name',
  accessorKey: 'datasetName',
  cell: info => <Link to={getDatasetDashboardPath(info.getValue() as unknown as string, currentEnvPath)}
    className="mx-2">{info.getValue() as unknown as string}</Link>
}, {
  id: 'description',
  header: 'Description',
  accessorKey: 'description',
  cell: info => info.getValue() ? info.getValue() : <span className="fst-italic">N/A</span>
}, {
  id: 'created',
  header: 'Date Created',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.getValue() as unknown as number)
}, {
  id: 'status',
  header: 'Status',
  accessorKey: 'status'
}]

const DatasetList = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const { currentEnvPath } = studyEnvContext
  const [showCreateDatasetModal, setShowCreateDatasetModal] = useState(false)
  const [datasets, setDatasets] = useState<DatasetDetails[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [datasetsSorting, setDatasetsSorting] = React.useState<SortingState>([])
  const { user } = useUser()

  const datasetTable = useReactTable({
    data: datasets,
    columns: datasetColumns(currentEnvPath),
    state: {
      sorting: datasetsSorting
    },
    onSortingChange: setDatasetsSorting,
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
      const datasets = await Api.listDatasetsForStudyEnvironment(
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
    { user.superuser &&
        <button className="btn btn-secondary" onClick={() => setShowCreateDatasetModal(!showCreateDatasetModal)}
          aria-label="show or hide export modal">
          <FontAwesomeIcon icon={faPlus}/> Create new dataset
        </button>
    }
    <CreateDatasetModal studyEnvContext={studyEnvContext}
      show={showCreateDatasetModal}
      setShow={setShowCreateDatasetModal}
      loadDatasets={loadData}/>
    <LoadingSpinner isLoading={isLoading}>
      <div className="col-12 p-3">
        <ul className="list-unstyled">
          <li className="bg-white my-3">
            <div style={contentHeaderStyle}>
              <h6>Datasets</h6>
            </div>
            {basicTableLayout(datasetTable)}
          </li>
        </ul>
      </div>
    </LoadingSpinner>
  </div>
}

export default DatasetList
