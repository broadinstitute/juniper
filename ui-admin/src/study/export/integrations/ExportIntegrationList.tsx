import React, { useState } from 'react'
import Api, { ExportIntegration } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/tableUtils'
import { instantToDefaultString } from '@juniper/ui-core'
import { useLoadingEffect } from 'api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { renderPageHeader } from 'util/pageUtils'
import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { faCheck, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import { Button } from '../../../components/forms/Button'
import Modal from 'react-bootstrap/Modal'
import { ExportIntegrationForm } from './ExportIntegrationView'
import { buildFilter } from '../../../util/exportUtils'

const DEFAULT_EXPORT_INTEGRATION: ExportIntegration = {
  name: 'new',
  destinationType: 'AIRTABLE',
  enabled: true,
  createdAt: new Date().getTime(),
  lastUpdatedAt: new Date().getTime(),
  id: '',
  destinationUrl: '',
  exportOptions: {
    splitOptionsIntoColumns: false,
    stableIdsForOptions: false,
    fileFormat: 'CSV',
    includeSubHeaders: false,
    onlyIncludeMostRecent: true,
    filterString: buildFilter({ includeProxiesAsRows: false, includeUnconsented: false }),
    excludeModules: ['surveys']
  }
}


/** show the mailing list in table */
export default function ExportIntegrationList({ studyEnvContext }:
  {studyEnvContext: StudyEnvContextT }) {
  const [integrations, setIntegrations] = useState<ExportIntegration[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newIntegration, setNewIntegration] = useState<ExportIntegration>(DEFAULT_EXPORT_INTEGRATION)
  const columns: ColumnDef<ExportIntegration>[] = [{
    header: 'Name',
    accessorKey: 'name'
  }, {
    header: 'Destination',
    accessorKey: 'destinationType'
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Enabled',
    accessorKey: 'enabled',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : '-'
  }, {
    header: '',
    enableSorting: false,
    id: 'actions',
    cell: info => <Link to={info.row.original.id}>View/Edit</Link>
  }]

  const table = useReactTable({
    data: integrations,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchExportIntegrations(paramsFromContext(studyEnvContext))
    setIntegrations(result)
  }, [studyEnvContext.currentEnvPath])

  const createNew = async () => {
    await Api.createExportIntegration(paramsFromContext(studyEnvContext), newIntegration)
    await reload()
    setShowCreateModal(false)
    setNewIntegration(DEFAULT_EXPORT_INTEGRATION)
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Export Integrations') }
    <LoadingSpinner isLoading={isLoading}>
      { studyEnvContext.currentEnv.environmentName === 'sandbox' && <Button
        variant="secondary" outline={true} onClick={() => setShowCreateModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Create Integration
      </Button> }
      { basicTableLayout(table) }
      { renderEmptyMessage(integrations, 'No intgrations') }
    </LoadingSpinner>
    { showCreateModal && <Modal show={true} size={'lg'} onHide={() => setShowCreateModal(false)} >
      <Modal.Header closeButton>
        <Modal.Title>Create new Export Integration</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <ExportIntegrationForm integration={newIntegration} setIntegration={setNewIntegration}/>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={createNew}>Create</Button>
        <Button variant="secondary" onClick={() => {
          setShowCreateModal(false)
          setNewIntegration(DEFAULT_EXPORT_INTEGRATION)
        }}>Cancel</Button>
      </Modal.Footer>
    </Modal>

    }
  </div>
}
