import React, { useState } from 'react'
import Api, { DataImportItem } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout, DownloadControl, renderEmptyMessage, RowVisibilityCount } from 'util/tableUtils'
import { currentIsoDate, instantToDefaultString } from '@juniper/ui-core'
import { useLoadingEffect } from '../api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import { StudyEnvContextT, useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'
import { Link, useParams } from 'react-router-dom'


/** show the dataImportItem list in table */
export default function DataImportView({ studyEnvContext }:
                                         { studyEnvContext: StudyEnvContextT }) {
  const [dataImportItems, setDataImportItems] = useState<DataImportItem[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const importedDate = instantToDefaultString(dataImportItems[0]?.createdAt)
  const columns: ColumnDef<DataImportItem>[] = [
    {
      header: 'EnrolleeId',
      accessorKey: 'createdEnrolleeId',
      cell: ({ row }) => {
        const enrolleIdLast8 = row.original?.createdEnrolleeId?.slice(-8)
        if (row.original.status == 'DELETED' || row.original.status == 'FAILED') {
          return <p>detail-{enrolleIdLast8}</p>
        } else {
          return <Link to={`${studyEnvContext.currentEnvPath}/participants/${row.original.createdEnrolleeId}`}
            className="me-1">view detail-{enrolleIdLast8}</Link>
        }
      }
    },
    {
      header: 'Status',
      accessorKey: 'status'
    },
    {
      header: 'Message',
      accessorKey: 'message'
    }
  ]

  const table = useReactTable({
    data: dataImportItems,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { dataImportId } = useParams()
  if (!dataImportId) {
    return <></>
  }
  const studyEnvParams = useStudyEnvParamsFromPath()
  const studyShortCode = studyEnvParams.studyShortcode
  if (!studyShortCode) {
    return <></>
  }

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchDataImport(studyEnvContext.portal.shortcode, studyShortCode,
      studyEnvContext.currentEnv.environmentName, dataImportId)
    // @ts-ignore
    setDataImportItems(result.importItems)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName])

  return <div className="container-fluid px-4 py-2">
    {renderPageHeader('Data Import Items')}
    {`Imported Date:${importedDate}`}
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <DownloadControl
            table={table}
            fileName={`${studyEnvContext.portal.shortcode}-DataImportItem-${currentIsoDate()}`}
          />
        </div>
      </div>

      {basicTableLayout(table)}
      {renderEmptyMessage(dataImportItems, 'No data import items')}
    </LoadingSpinner>
  </div>
}
