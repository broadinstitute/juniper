import React, { useEffect, useState } from 'react'
import { StudyEnvContextT, studyEnvDatasetListViewPath } from '../../StudyEnvironmentRouter'
import Api, { DatasetDetails, DatasetJobHistory } from '../../../api/api'
import LoadingSpinner from '../../../util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../../util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowLeft, faExternalLink, faTrash } from '@fortawesome/free-solid-svg-icons'
import { instantToDefaultString } from '@juniper/ui-core'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../../util/tableUtils'
import { Link, useParams } from 'react-router-dom'
import { useUser } from '../../../user/UserProvider'
import DeleteDatasetModal from './DeleteDatasetModal'

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
  const [showDeleteDatasetModal, setShowDeleteDatasetModal] = useState(false)
  const [datasetDetails, setDatasetDetails] = useState<DatasetDetails | undefined>(undefined)
  const [datasetJobHistory, setDatasetJobHistory] = useState<DatasetJobHistory[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const datasetName = useParams().datasetName as string
  const { user } = useUser()

  const table = useReactTable({
    data: datasetJobHistory,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const contentHeaderStyle = {
    padding: '1em 0 0 1em',
    borderBottom: '1px solid #f6f6f6'
  }

  const loadData = async () => {
    //Fetch dataset details
    await Api.listDatasetsForStudyEnvironment(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setDatasetDetails(result.find((dataset: { datasetName: string }) => dataset.datasetName === datasetName))
    }).catch(e =>
      Store.addNotification(failureNotification(`Error loading dataset: ${e.message}`))
    )

    //Fetch dataset job history
    await Api.getJobHistoryForDataset(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      datasetName).then(result => {
      setDatasetJobHistory(result)
    }).catch(e =>
      Store.addNotification(failureNotification(`Error loading dataset job history: ${e.message}`))
    )

    setIsLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])
  return <div className="container-fluid py-3">
    <h1 className="h3">Terra Data Repo</h1>
    <Link to={studyEnvDatasetListViewPath(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName)} className="mx-2">
      <FontAwesomeIcon icon={faArrowLeft}/> Back to dataset list
    </Link>
    {user?.superuser && datasetDetails?.status == 'CREATED' &&
        <button className="btn btn-secondary" onClick={() => setShowDeleteDatasetModal(!showDeleteDatasetModal)}
          aria-label="show or hide export modal">
          <FontAwesomeIcon icon={faTrash}/> Delete dataset
        </button>
    }
    <DeleteDatasetModal studyEnvContext={studyEnvContext}
      show={showDeleteDatasetModal}
      setShow={setShowDeleteDatasetModal}
      datasetName={datasetName}
      loadDatasets={loadData}/>
    <LoadingSpinner isLoading={isLoading}>
      <div className="col-12 p-3">
        <ul className="list-unstyled">
          <li className="bg-white my-3">
            <div style={contentHeaderStyle}>
              <h6>Dataset Details</h6>
            </div>
            <div className="flex-grow-1 p-3">
              <div className="form-group">
                <dl>
                  <dt>Dataset Name</dt><dd>{ datasetDetails?.datasetName }</dd>
                  <dt>Description</dt><dd>{ datasetDetails?.description ?
                    datasetDetails?.description : <span className="fst-italic">N/A</span> }</dd>
                  <dt>Created By</dt><dd>{ datasetDetails?.createdBy }</dd>
                  <dt>Date Created</dt><dd>{ instantToDefaultString(datasetDetails?.createdAt) }</dd>
                </dl>
                { datasetDetails?.status == 'CREATED' &&
                  <a href={`https://jade.datarepo-dev.broadinstitute.org/datasets/${datasetDetails?.tdrDatasetId}`}
                    target="_blank">View dataset in Terra Data Repo <FontAwesomeIcon icon={faExternalLink}/></a>
                }
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
