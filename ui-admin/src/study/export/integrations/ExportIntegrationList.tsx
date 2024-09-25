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
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'


/** show the mailing list in table */
export default function ExportIntegrationList({ studyEnvContext }:
  {studyEnvContext: StudyEnvContextT }) {
  const [integrations, setIntegrations] = useState<ExportIntegration[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const columns: ColumnDef<ExportIntegration>[] = [{
    header: 'Destination',
    accessorKey: 'destinationType'
  }, {
    header: 'Name',
    accessorKey: 'name'
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

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchExportIntegrations(paramsFromContext(studyEnvContext))
    setIntegrations(result)
  }, [studyEnvContext.currentEnvPath])

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Export Integrations') }
    <LoadingSpinner isLoading={isLoading}>
      { basicTableLayout(table) }
      { renderEmptyMessage(integrations, 'No intgrations') }
    </LoadingSpinner>
  </div>
}
