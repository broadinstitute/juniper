import React, {
  useMemo,
  useState
} from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Link } from 'react-router-dom'
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
  IndeterminateCheckbox,
  renderEmptyMessage,
  RowVisibilityCount,
  useRoutableTablePaging
} from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faEnvelope
} from '@fortawesome/free-solid-svg-icons'
import AdHocEmailModal from '../AdHocEmailModal'
import {
  currentIsoDate,
  instantToDefaultString
} from '@juniper/ui-core'
import { useLoadingEffect } from 'api/api-utils'
import TableClientPagination from 'util/TablePagination'
import { Button } from 'components/forms/Button'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import { concatSearchExpressions } from 'util/searchExpressionUtils'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])
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

  const {
    searchState,
    updateSearchState,
    setSearchState,
    searchExpression
  } = useParticipantSearchState()

  const { paginationState, preferredNumRowsKey } = useRoutableTablePaging('participantList')

  const columns = useMemo<ColumnDef<EnrolleeSearchExpressionResult, string>[]>(() => [{
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
    accessorFn: row => row.participantUser?.lastLogin,
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }, {
    id: 'familyName',
    header: 'Family name',
    accessorFn: row => row.profile?.familyName,
    meta: {
      columnType: 'string'
    }
  }, {
    id: 'givenName',
    header: 'Given name',
    accessorFn: row => row.profile?.givenName,
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

  const { isLoading } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      concatSearchExpressions([searchExpression, 'include({user.lastLogin})']))
    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])

  const numSelected = Object.keys(rowSelection).length
  const allowSendEmail = numSelected > 0
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => participantList[parseInt(key)].enrollee.shortcode)

  return <div className="ParticipantList container-fluid px-4 py-2">
    { renderPageHeader('Participant List') }
    <ParticipantSearch
      key={currentEnv.environmentName}
      studyEnvContext={studyEnvContext}
      searchState={searchState}
      updateSearchState={updateSearchState}
      setSearchState={setSearchState}
    />
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
      { basicTableLayout(table, { filterable: true }) }
      { renderEmptyMessage(participantList, 'No participants') }
      <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>
    </LoadingSpinner>
  </div>
}


export default ParticipantList
