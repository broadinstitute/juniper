import React, { useEffect, useState } from 'react'
import { datasetDashboardPath, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DatasetDetails } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from '@juniper/ui-core'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout, renderEmptyMessage } from 'util/tableUtils'
import { Link } from 'react-router-dom'
import { useUser } from 'user/UserProvider'
import CreateDatasetModal from './CreateDatasetModal'
import { Button } from 'components/forms/Button'
import { faSquarePlus } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'

const datasetColumns = (currentEnvPath: string): ColumnDef<DatasetDetails>[] => [{
  id: 'datasetName',
  header: 'Dataset Name',
  accessorKey: 'datasetName',
  cell: info => {
    return info.row.original.status !== 'DELETING' ?
      <Link to={datasetDashboardPath(info.row.original.datasetName, currentEnvPath)} className="mx-2">
        {info.getValue() as unknown as string}
      </Link> : <span className="mx-2">{info.row.original.datasetName}</span>
  }
}, {
  id: 'description',
  header: 'Description',
  accessorKey: 'description',
  cell: info => info.getValue() ? info.getValue() : <span className="fst-italic">N/A</span>
}, {
  id: 'created',
  header: 'Date Created',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.row.original.createdAt)
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

  const loadData = async () => {
    //Fetch datasets
    await Api.listDatasetsForStudyEnvironment(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setDatasets(result)
    }).catch(e =>
      Store.addNotification(failureNotification(`Error loading datasets: ${e.message}`))
    )
    setIsLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Terra Data Repo') }
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <h4>Datasets</h4>
        {user?.superuser &&
            <Button onClick={() => setShowCreateDatasetModal(!showCreateDatasetModal)}
              variant="light" className="border m-1"
              aria-label="show or export to tdr modal">
              <FontAwesomeIcon icon={faSquarePlus} className="fa-lg"/> Create dataset
            </Button>
        }
      </div>
      { basicTableLayout(datasetTable) }
      { renderEmptyMessage(datasets, 'No datasets') }
    </LoadingSpinner>
    <CreateDatasetModal studyEnvContext={studyEnvContext}
      show={showCreateDatasetModal}
      setShow={setShowCreateDatasetModal}
      loadDatasets={loadData}/>
  </div>
}

export default DatasetList
