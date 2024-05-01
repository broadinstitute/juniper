import React, { useState } from 'react'
import Api, { DataImportItem } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import {
  basicTableLayout,
  DownloadControl,
  IndeterminateCheckbox,
  renderEmptyMessage,
  RowVisibilityCount
} from 'util/tableUtils'
import { currentIsoDate, instantToDefaultString } from '@juniper/ui-core'
import { useLoadingEffect } from '../api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import { StudyEnvContextT, useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'
import { useParams } from 'react-router-dom'


/** show the dataImportItem list in table */
export default function DataImportView({ studyEnvContext }:
                                           { studyEnvContext: StudyEnvContextT }) {
  const [dataImportItems, setDataImportItems] = useState<DataImportItem[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const columns: ColumnDef<DataImportItem>[] = [{
    id: 'select',
    header: ({ table }) => <IndeterminateCheckbox
      checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
      onChange={table.getToggleAllRowsSelectedHandler()}/>,
    cell: ({ row }) => (
      <div className="px-1">
        <IndeterminateCheckbox
          checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
          onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
      </div>
    )
  }, {
    header: 'ImportItemId',
    accessorKey: 'id'
  },
  {
    header: 'ImportId',
    accessorKey: 'importId'
  },
  {
    header: 'Operator',
    accessorKey: 'createdParticipantUserId'
  },
  {
    header: 'EnrolleeId',
    accessorKey: 'createdEnrolleeId'
  },
  {
    header: 'Status',
    accessorKey: 'status'
  },
  {
    header: 'Message',
    accessorKey: 'message'
  },
  {
    header: 'Imported At',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }]

  const table = useReactTable({
    data: dataImportItems,
    columns,
    state: {
      sorting,
      rowSelection
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const { dataImportId } = useParams()
  if (!dataImportId) {
    return <></>
  }
  const numSelected = Object.keys(rowSelection).length
  const studyEnvParams = useStudyEnvParamsFromPath()
  const studyShortCode = studyEnvParams.studyShortcode
  if (!studyShortCode) {
    return <></>
  }

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchDataImport(studyEnvContext.portal.shortcode, studyShortCode,
      studyEnvContext.currentEnv.environmentName, dataImportId)
    // @ts-ignore
    setDataImportItems(result.importItems)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName])

  return <div className="container-fluid px-4 py-2">
    {renderPageHeader('Data Imports')}
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <DownloadControl
            table={table}
            fileName={`${studyEnvContext.portal.shortcode}-DataImport-${currentIsoDate()}.tsv`}
          />
        </div>
      </div>

      {basicTableLayout(table)}
      {renderEmptyMessage(dataImportItems, 'No data imports')}
    </LoadingSpinner>
  </div>
}
