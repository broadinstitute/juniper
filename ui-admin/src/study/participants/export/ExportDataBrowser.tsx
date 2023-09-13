import React, { useMemo, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { ExportData } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import ExportDataControl from './ExportDataControl'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import { useLoadingEffect } from 'api/api-utils'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const ExportDataBrowser = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [data, setData] = useState<ExportData | null>(null)
  const [showExportModal, setShowExportModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})

  const columns = useMemo<ColumnDef<string, string>[]>(() => {
    if (!data) {
      return []
    }
    const enrolleeCols = data.valueMaps.map(valueMap => ({
      id: valueMap['enrollee.shortcode'],
      header: valueMap['enrollee.shortcode'],
      accessorFn: (d: string) => valueMap[d]
    }))
    return [{
      header: 'Key',
      id: 'keyCol',
      width: 100,
      accessorFn: (d:string, row) => data.headerRowValues[row]
    }, {
      header: 'Label',
      id: 'labelCol',
      width: 200,
      accessorFn: (d:string, row) => data.subHeaderRowValues[row]
    }, ...enrolleeCols]
  }, [data])

  const table = useReactTable({
    data: data?.columnKeys ?? [],
    columns,
    state: {
      sorting,
      rowSelection,
      columnVisibility
    },
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.exportEnrollees(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, { fileFormat: 'JSON', limit: 10 })
    const result = await response.json()
    setData(result)
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  return <div className="container-fluid py-3">
    <h1 className="h3">Data export preview</h1>
    <span className="text-muted fst-italic">
      (Transposed for readability, the actual export has participants as rows)
    </span>
    <button className="btn btn-secondary" onClick={() => setShowExportModal(!showExportModal)}
      aria-label="show or hide export modal">
      Download <FontAwesomeIcon icon={faDownload}/>
    </button>
    <ExportDataControl studyEnvContext={studyEnvContext} show={showExportModal} setShow={setShowExportModal}/>
    <LoadingSpinner isLoading={isLoading}/>
    {!isLoading && basicTableLayout(table)}
  </div>
}

export default ExportDataBrowser
