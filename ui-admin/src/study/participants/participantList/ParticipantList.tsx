import React, { useState, useEffect, useMemo } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { Link, useSearchParams } from 'react-router-dom'
import {
  getDatasetListViewPath,
  getExportDataBrowserPath,
  StudyEnvContextT, studyEnvMetricsPath
} from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable, VisibilityState
} from '@tanstack/react-table'
import { ColumnVisibilityControl, IndeterminateCheckbox, tableHeader } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import ExportDataControl from '../export/ExportDataControl'
import AdHocEmailModal from '../AdHocEmailModal'
import EnrolleeSearchFacets, {} from './facets/EnrolleeSearchFacets'
import { facetValuesFromString, facetValuesToString, SAMPLE_FACETS, FacetValue }
  from 'api/enrolleeSearch'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchResult[]>([])
  const [showExportModal, setShowExportModal] = useState(false)
  const [showEmailModal, setShowEmailModal] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'givenName': false,
    'familyName': false,
    'contactEmail': false
  })
  const [searchParams, setSearchParams] = useSearchParams()

  const facetValues = facetValuesFromString(searchParams.get('facets') ?? '{}', SAMPLE_FACETS)


  const columns = useMemo<ColumnDef<EnrolleeSearchResult, string>[]>(() => [{
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
    header: 'Shortcode',
    accessorKey: 'enrollee.shortcode',
    cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue()}</Link>
  }, {
    id: 'familyName',
    header: 'Family name',
    accessorKey: 'profile.familyName'
  }, {
    id: 'givenName',
    header: 'Given name',
    accessorKey: 'profile.givenName'
  }, {
    id: 'contactEmail',
    header: 'Contact email',
    accessorKey: 'profile.contactEmail'
  }, {
    header: 'Consented',
    accessorFn: data => data.enrollee.consented.toString(),
    cell: info => info.getValue() === 'true' ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Kit status',
    filterFn: 'includesString', //the occasional presence of an undefined value seems to require a filter type to be set
    accessorKey: 'mostRecentKitStatus'
  }], [study.shortcode])


  const table = useReactTable({
    data: participantList,
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
    getFilteredRowModel: getFilteredRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const searchEnrollees = async (facetValues: FacetValue[]) => {
    setIsLoading(true)
    try {
      const response = await Api.searchEnrollees(portal.shortcode, study.shortcode, currentEnv.environmentName,
        facetValues)
      setParticipantList(response)
    } catch (e) {
      Store.addNotification(failureNotification('Error loading participants'))
    }
    setIsLoading(false)
  }

  const updateFacetValues = (facetValues: FacetValue[]) => {
    searchEnrollees(facetValues)
    searchParams.set('facets', facetValuesToString(facetValues))
    setSearchParams(searchParams)
  }

  useEffect(() => {
    searchEnrollees(facetValues)
  }, [])

  const numSelected = Object.keys(rowSelection).length
  const allowSendEmail = numSelected > 0
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => participantList[parseInt(key)].enrollee.shortcode)

  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12 align-items-baseline d-flex">
        <h2 className="h4 text-center me-4">{study.name} Participants</h2>
        <div className="d-flex align-items-center justify-content-between">
          <div className="d-flex align-items-center">
            <Link to={studyEnvMetricsPath(portal.shortcode, currentEnv.environmentName, study.shortcode)}
              className="mx-2">Metrics</Link>
            <span className="px-1">|</span>
            <Link to={getExportDataBrowserPath(currentEnvPath)} className="mx-2">Export preview</Link>
            <span className="px-1">|</span>
            <button className="btn btn-secondary" onClick={() => setShowExportModal(!showExportModal)}>
              Download
            </button>
            <span className="px-1">|</span>
            <Link to={getDatasetListViewPath(currentEnvPath)} className="mx-2">Terra Data Repo</Link>
            <ExportDataControl studyEnvContext={studyEnvContext} show={showExportModal} setShow={setShowExportModal}/>
          </div>
        </div>
      </div>
      <div className="col-3">
        <EnrolleeSearchFacets facets={SAMPLE_FACETS} facetValues={facetValues} updateFacetValues={updateFacetValues} />
      </div>
      <div className="col-9">
        <LoadingSpinner isLoading={isLoading}>
          <div>
            <div className="d-flex align-items-center">
              <span className="me-2">
                {numSelected} of{' '}
                {table.getPreFilteredRowModel().rows.length} selected
              </span>
              <span className="me-2">
                <button onClick={() => setShowEmailModal(allowSendEmail)}
                  aria-disabled={!allowSendEmail} className="btn btn-secondary"
                  title={allowSendEmail ? 'Send email' : 'Select at least one participant'}>
                  Send email
                </button>
              </span>
              { showEmailModal && <AdHocEmailModal enrolleeShortcodes={enrolleesSelected}
                studyEnvContext={studyEnvContext}
                onDismiss={() => setShowEmailModal(false)}/> }
              <ColumnVisibilityControl table={table}/>
            </div>
          </div>
          <table className="table table-striped">
            <thead>
              <tr>
                {table.getFlatHeaders().map(header => tableHeader(header, { sortable: true, filterable: true }))}
              </tr>
            </thead>
            <tbody>
              {table.getRowModel().rows.map(row => {
                return (
                  <tr key={row.id}>
                    {row.getVisibleCells().map(cell => {
                      return (
                        <td key={cell.id}>
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </td>
                      )
                    })}
                  </tr>
                )
              })}
            </tbody>
          </table>
          { participantList.length === 0 && <span className="text-muted fst-italic">No participants</span>}
        </LoadingSpinner>
      </div>
    </div>
  </div>
}


export default ParticipantList
