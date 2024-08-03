import React, {
  useMemo,
  useState
} from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { ExportData } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  CellContext,
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
import { Button } from 'components/forms/Button'
import {
  renderPageHeader,
  renderTruncatedText
} from 'util/pageUtils'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { buildFilter } from 'util/exportUtils'

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
      accessorFn: (d: string) => valueMap[d],
      enableSorting: false,
      cell: (info: CellContext<string, string>) => renderTruncatedText(info.getValue(), 100)
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
      studyEnvContext.currentEnv.environmentName, {
        fileFormat: 'JSON', limit: 10, filter: buildFilter()
      })
    const result = await response.json()
    if (!response.ok) {
      Store.addNotification(failureNotification('Failed to load export data', result.message))
    } else {
      setData(result)
    }
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Data Export') }
    <div className="align-items-center justify-content-between">
      <div>
        <span className="text-muted fst-italic px-2">
          (Transposed for readability, the actual export has participants as rows)
        </span>
      </div>
      <div >
        <Button onClick={() => setShowExportModal(!showExportModal)}
          variant="light" className="border m-1"
          aria-label="show or hide export modal">
          <FontAwesomeIcon icon={faDownload} className="fa-lg"/> Download
        </Button>
      </div>
    </div>
    <ExportDataControl studyEnvContext={studyEnvContext} show={showExportModal} setShow={setShowExportModal}/>
    <LoadingSpinner isLoading={isLoading}/>
    {!isLoading && basicTableLayout(table)}
  </div>
}

export default ExportDataBrowser
