import React, { useState, useMemo } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Link, useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'
import {
  basicTableLayout,
  ColumnVisibilityControl,
  DownloadControl,
  IndeterminateCheckbox, RowVisibilityCount,
  useRoutableTablePaging
} from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faEnvelope } from '@fortawesome/free-solid-svg-icons'
import AdHocEmailModal from '../AdHocEmailModal'
import {
  ALL_FACETS,
  FacetValue,
  facetValuesFromString,
  facetValuesToString,
  KEYWORD_FACET
} from 'api/enrolleeSearch'
import { currentIsoDate, instantToDefaultString } from 'util/timeUtils'
import { useLoadingEffect } from 'api/api-utils'
import { FacetView, getUpdatedFacetValues } from './facets/EnrolleeSearchFacets'
import TableClientPagination from 'util/TablePagination'
import { Button } from 'components/forms/Button'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchResult[]>([])
  const [showEmailModal, setShowEmailModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'createdAt', desc: true }
  ])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'givenName': false,
    'familyName': false,
    'contactEmail': false
  })
  const [searchParams, setSearchParams] = useSearchParams()

  const facetValues = facetValuesFromString(searchParams.get('facets') ?? '{}', ALL_FACETS)
  const keywordFacetIndex = facetValues.findIndex(facet => facet.facet.category === 'keyword')
  const keywordFacetValue = facetValues[keywordFacetIndex]
  const { paginationState, preferredNumRowsKey } = useRoutableTablePaging('participantList')

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
    meta: {
      columnType: 'string'
    },
    cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue()}</Link>
  }, {
    header: 'Created',
    id: 'createdAt',
    accessorKey: 'enrollee.createdAt',
    enableColumnFilter: false,
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }, {
    id: 'lastLogin',
    header: 'Last login',
    accessorKey: 'participantUser.lastLogin',
    enableColumnFilter: false,
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }, {
    id: 'familyName',
    header: 'Family name',
    accessorKey: 'profile.familyName',
    meta: {
      columnType: 'string'
    }
  }, {
    id: 'givenName',
    header: 'Given name',
    accessorKey: 'profile.givenName',
    meta: {
      columnType: 'string'
    }
  }, {
    id: 'contactEmail',
    header: 'Contact email',
    accessorKey: 'profile.contactEmail',
    meta: {
      columnType: 'string'
    }
  }, {
    header: 'Consented',
    accessorKey: 'enrollee.consented',
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Consented' },
        { value: false, label: 'Not Consented' }
      ]
    },
    filterFn: 'equals',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Kit status',
    filterFn: 'includesString', //an undefined value in a cell seems to switch the filter table away from the default
    accessorKey: 'mostRecentKitStatus',
    meta: {
      columnType: 'string'
    }
  }], [study.shortcode, currentEnv.environmentName])

  const table = useReactTable({
    data: participantList,
    columns,
    state: {
      sorting,
      rowSelection,
      columnVisibility
    },
    initialState: {
      pagination: paginationState
    },
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const updateFacetValues = (facetValues: FacetValue[]) => {
    searchParams.set('facets', facetValuesToString(facetValues))
    setSearchParams(searchParams)
  }

  const updateKeywordFacet = (facetValue: FacetValue | null) => {
    updateFacetValues(getUpdatedFacetValues(facetValue ?? null, keywordFacetIndex, facetValues))
  }

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.searchEnrollees(portal.shortcode,
      study.shortcode, currentEnv.environmentName, facetValues)
    setParticipantList(response)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchParams.get('facets')])

  const numSelected = Object.keys(rowSelection).length
  const allowSendEmail = numSelected > 0
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => participantList[parseInt(key)].enrollee.shortcode)

  return <div className="ParticipantList container-fluid px-4 py-2">
    <div className="align-items-baseline d-flex mb-2">
      <h2 className="text-center me-4 fw-bold">Participant List</h2>
    </div>
    <div className="align-items-baseline d-flex mb-2">
      <FacetView facet={KEYWORD_FACET} facetValue={keywordFacetValue} updateValue={updateKeywordFacet}/>
    </div>
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <Button onClick={() => setShowEmailModal(allowSendEmail)}
            variant="light" className="border m-1" disabled={!allowSendEmail}
            tooltip={allowSendEmail ? 'Send email' : 'Select at least one participant'}>
            <FontAwesomeIcon icon={faEnvelope} className="fa-lg"/> Send email
          </Button>
          <DownloadControl table={table} fileName={`${portal.shortcode}-ParticipantList-${currentIsoDate()}`}/>
          <ColumnVisibilityControl table={table}/>
          { showEmailModal && <AdHocEmailModal enrolleeShortcodes={enrolleesSelected}
            studyEnvContext={studyEnvContext}
            onDismiss={() => setShowEmailModal(false)}/> }
        </div>
      </div>
      { basicTableLayout(table, { filterable: true })}
      { participantList.length === 0 &&
        <span className="d-flex justify-content-center text-muted fst-italic">No participants</span> }
      <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>
    </LoadingSpinner>
  </div>
}


export default ParticipantList
