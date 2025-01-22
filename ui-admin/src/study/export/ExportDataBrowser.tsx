import React, {
  useEffect,
  useMemo,
  useState
} from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Api, { ExportData, ExportOptions } from 'api/api'
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
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import { faDownload, faInfoCircle } from '@fortawesome/free-solid-svg-icons'
import { doApiLoad } from 'api/api-utils'
import { Button } from 'components/forms/Button'
import {
  renderPageHeader,
  renderTruncatedText
} from 'util/pageUtils'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { basicTableLayout } from '../../util/table/tableUtils'
import { currentIsoDate, saveBlobAsDownload } from '@juniper/ui-core'
import ExportOptionsForm, { FILE_FORMATS } from './ExportOptionsForm'
import { useSearchParams } from 'react-router-dom'

export const DEFAULT_EXPORT_OPTS: ExportOptions = {
  splitOptionsIntoColumns: false,
  stableIdsForOptions: false,
  fileFormat: 'TSV',
  includeSubHeaders: true,
  onlyIncludeMostRecent: true,
  filterString: '{enrollee.subject} = true and {enrollee.consented} = true',
  excludeModules: [],
  includeFields: []
}

const ExportDataBrowser = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const [data, setData] = useState<ExportData | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({})
  const [searchParams] = useSearchParams()
  const [isDownloading, setIsDownloading] = useState(false)
  const [exportOptions, setExportOptions] = useState<ExportOptions>(DEFAULT_EXPORT_OPTS)

  // auto-load preview if specified
  useEffect(() => {
    if (searchParams.get('showPreview')) {
      loadPreview()
    }
  }, [])

  const doExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportEnrollees(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, exportOptions)
      const fileSuffix = FILE_FORMATS.find(format =>
        exportOptions.fileFormat === format.value)?.fileSuffix
      const fileName = `${currentIsoDate()}-enrollees.${fileSuffix}`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading: setIsDownloading })
  }

  const doDictionaryExport = () => {
    doApiLoad(async () => {
      const response = await Api.exportDictionary(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, exportOptions)
      const fileName = `${currentIsoDate()}-DataDictionary.xlsx`
      const blob = await response.blob()
      saveBlobAsDownload(blob, fileName)
    }, { setIsLoading: setIsDownloading })
  }


  const loadPreview = async ()  => {
    doApiLoad(async () => {
      const response = await Api.exportEnrollees(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, {
          ...exportOptions,
          fileFormat: 'JSON', rowLimit: 10
        })
      const result = await response.json()
      if (!response.ok) {
        Store.addNotification(failureNotification('Failed to load export data', result.message))
      } else {
        setData(result)
      }
    }, { setIsLoading })
  }

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

  return <div className="container-fluid px-4 py-2 align-items-start">
    { renderPageHeader('Data Export') }
    <ExportOptionsForm setExportOptions={setExportOptions} exportOptions={exportOptions}/>
    <div className="align-items-center my-4">
      <Button variant="primary" onClick={doExport} disabled={isDownloading || isLoading}>
        Download <FontAwesomeIcon icon={faDownload}/> <LoadingSpinner isLoading={isDownloading}/>
      </Button>
      <Button variant="secondary" onClick={doDictionaryExport} disabled={isDownloading || isLoading}>
        Download dictionary (.xlsx) <LoadingSpinner isLoading={isDownloading}/>
      </Button>
      <Button
        disabled={isDownloading || isLoading}
        variant="secondary"
        className="border ms-4 "
        onClick={loadPreview}
      >
        Show preview <LoadingSpinner isLoading={isLoading}/>
      </Button>
    </div>
    <LoadingSpinner isLoading={isDownloading}/>
    {!data && <div className={'d-flex justify-content-center'}>

    </div> }
    {!isDownloading && data &&
      <>
        <hr/>
        <div className="my-2">
          <span className="text-muted fst-italic m-1">
            <FontAwesomeIcon className={'me-2'} icon={faInfoCircle}/>
             Preview shows up to 10 participants, transposed for readability --
            the downloaded export will have participants as rows.
          </span>
        </div>
        {basicTableLayout(table)}
      </>
    }
  </div>
}

export default ExportDataBrowser
