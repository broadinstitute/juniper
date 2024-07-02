import React, {
  useMemo,
  useState
} from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  Link,
  useSearchParams
} from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getGroupedRowModel,
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
import { FamilyLink } from 'study/families/FamilyLink'
import { isEmpty } from 'lodash'

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

  const [searchParams, setSearchParams] = useSearchParams()

  const groupByFamily = searchParams.get('groupByFamily') === 'true'
  const setGroupByFamily = (groupByFamily: boolean) => {
    setSearchParams({ ...searchParams, groupByFamily: groupByFamily.toString() })
  }

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

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
    id: 'familyShortcode',
    header: 'Family shortcode',
    enableColumnFilter: false,
    meta: {
      columnType: 'string'
    },
    accessorFn: row => row.families.map(family => family.shortcode),
    cell: ({ row }) => {
      if (isEmpty(row.original.families)) {
        return <span className='fst-italic'>None</span>
      }
      return <>{row.original.families.map((family, idx) => <FamilyLink key={idx} family={family}
        studyEnvContext={studyEnvContext}/>)}</>
    }
  }, {
    id: 'contactEmail',
    size: 2,
    maxSize: 2,
    header: 'Contact email',
    accessorKey: 'profile.contactEmail',
    meta: {
      columnType: 'string'
    }
  }, {
    header: 'Consented',
    size: 1,
    maxSize: 1,
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

  const flattenFamilies = (participants: EnrolleeSearchExpressionResult[]) => {
    return participants.flatMap(participant => {
      if (participant.families.length === 0) {
        return [participant]
      }
      return participant.families.map(family => {
        return {
          ...participant,
          families: [family]
        }
      })
    })
  }

  const table = useReactTable({
    data: useMemo(
      () => groupByFamily
        ? flattenFamilies(participantList)
        : participantList,
      [groupByFamily, participantList]),
    columns,
    state: {
      sorting,
      rowSelection: useMemo(() => groupByFamily ? {
        ...rowSelection,
        'familyShortcode': true
      } : rowSelection, [groupByFamily, rowSelection]),
      grouping: useMemo(() => groupByFamily ? ['familyShortcode'] : [], [groupByFamily]),
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
    getGroupedRowModel: getGroupedRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const { isLoading } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      // if families exist, adding these expression guarantees that we get all families.
      // might be a better way to do it, but this works for now
      familyLinkageEnabled
        ? concatSearchExpressions([`(include({family.shortcode}))`, searchExpression])
        : searchExpression)
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
          {familyLinkageEnabled && <Button onClick={() => setGroupByFamily(!groupByFamily)}>
            {groupByFamily ? 'Ungroup families' : 'Group families'}
          </Button>}
        </div>
      </div>
      {basicTableLayout(table, { filterable: true })}
      { renderEmptyMessage(participantList, 'No participants') }
      <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>
    </LoadingSpinner>
  </div>
}


export default ParticipantList
