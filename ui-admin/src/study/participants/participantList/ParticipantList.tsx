import React, { useState, useMemo } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Link, useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable, VisibilityState
} from '@tanstack/react-table'
import { basicTableLayout, ColumnVisibilityControl, IndeterminateCheckbox } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faSearch } from '@fortawesome/free-solid-svg-icons'
import AdHocEmailModal from '../AdHocEmailModal'
import {
  ALL_FACETS,
  FacetValue,
  facetValuesFromString,
  facetValuesToString,
  KEYWORD_FACET,
  newFacetValue, StringFacetValue
} from 'api/enrolleeSearch'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'
import { useLoadingEffect } from 'api/api-utils'
import { getUpdatedFacetValues } from './facets/EnrolleeSearchFacets'


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
  const keywordFacet = facetValues.find(facet => facet.facet.category === 'keyword') as StringFacetValue
  const initialKeywordVal = keywordFacet?.values[0] ?? ''
  const [keywordFieldValue, setKeywordFieldValue] = useState(initialKeywordVal)


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
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const updateFacetValues = (facetValues: FacetValue[]) => {
    searchParams.set('facets', facetValuesToString(facetValues))
    setSearchParams(searchParams)
  }

  const updateKeyword = (keyword: string) => {
    const keywordFacetIndex = facetValues.findIndex(facet => facet.facet.category === 'keyword')
    let keywordFacetValue = null
    if (keyword && keyword.length) {
      keywordFacetValue = newFacetValue(KEYWORD_FACET, { values: [keyword] })
    }
    updateFacetValues(getUpdatedFacetValues(keywordFacetValue, keywordFacetIndex, facetValues))
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

  return <div className="ParticipantList container-fluid pt-2">
    <div className="row ps-3">
      <div className="col-12 align-items-baseline d-flex mb-2">
        <h2 className="h4 text-center me-4 fw-bold">Participant List</h2>
      </div>
      <div className="col-12 align-items-baseline d-flex mb-3">
        <form className="rounded-5" onSubmit={e => {
          e.preventDefault()
          updateKeyword(keywordFieldValue)
        }} style={{ border: '1px solid #bbb', backgroundColor: '#fff', padding: '0.25em 0.75em 0em' }}>
          <button type="submit" title="submit search" className="btn btn-secondary">
            <FontAwesomeIcon icon={faSearch}/>
          </button>
          <input type="text" value={keywordFieldValue} size={40}
            title="search name, email and shortcode"
            style={{ border: 'none', outline: 'none' }}
            placeholder="Search names, email, or shortcode..."
            onChange={e => setKeywordFieldValue(e.target.value)}/>

        </form>

      </div>
      <div className="col-12">
        <LoadingSpinner isLoading={isLoading}>
          <div>
            <div className="d-flex align-items-center">
              <span className="me-2">
                {numSelected} of{' '}
                {table.getPreFilteredRowModel().rows.length} selected ({table.getFilteredRowModel().rows.length} shown)
              </span>
              <span className="me-2">
                <Button onClick={() => setShowEmailModal(allowSendEmail)}
                  variant="link" disabled={!allowSendEmail}
                  tooltip={allowSendEmail ? 'Send email' : 'Select at least one participant'}>
                  Send email
                </Button>
              </span>
              { showEmailModal && <AdHocEmailModal enrolleeShortcodes={enrolleesSelected}
                studyEnvContext={studyEnvContext}
                onDismiss={() => setShowEmailModal(false)}/> }
              <ColumnVisibilityControl table={table}/>
            </div>
          </div>
          { basicTableLayout(table, { filterable: true })}
          { participantList.length === 0 && <span className="text-muted fst-italic">No participants</span>}
        </LoadingSpinner>
      </div>
    </div>
  </div>
}


export default ParticipantList
