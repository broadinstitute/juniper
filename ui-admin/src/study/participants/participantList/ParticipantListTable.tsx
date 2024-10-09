import React, {
  useMemo,
  useState
} from 'react'
import { EnrolleeSearchExpressionResult } from 'api/api'
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
  faEnvelope,
  faPlus
} from '@fortawesome/free-solid-svg-icons'
import AdHocEmailModal from '../AdHocEmailModal'
import {
  currentIsoDate,
  instantToDefaultString
} from '@juniper/ui-core'
import {
  Button,
  EllipsisDropdownButton
} from 'components/forms/Button'
import { FamilyLink } from 'study/families/FamilyLink'
import { isEmpty } from 'lodash'
import TableClientPagination from 'util/TablePagination'
import CreateSyntheticEnrolleeModal from './CreateSyntheticEnrolleeModal'

/**
 * Participant table used by the participant list. Does not include searching functionality, just table
 * rendering.
 */
function ParticipantListTable({
  participantList,
  studyEnvContext,
  familyId,
  disablePagination,
  disableRowVisibilityCount,
  disableColumnFiltering,
  header,
  tableClass,
  reload
}: {
  participantList: EnrolleeSearchExpressionResult[],
  studyEnvContext: StudyEnvContextT,
  familyId?: string,
  disablePagination?: boolean
  disableRowVisibilityCount?: boolean
  disableColumnFiltering?: boolean,
  header?: React.ReactNode,
  tableClass?: string,
  reload: () => void
}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [showEmailModal, setShowEmailModal] = useState(false)
  const [showSyntheticModal, setShowSyntheticModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'createdAt', desc: true }
  ])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'givenName': false,
    'familyName': false,
    'contactEmail': false
  })


  const { paginationState, preferredNumRowsKey } = useRoutableTablePaging(
    `participantList${familyId ? `-${familyId}` : ''}`
  )

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const columns = useMemo<ColumnDef<EnrolleeSearchExpressionResult>[]>(() => {
    const columns: ColumnDef<EnrolleeSearchExpressionResult>[] = [{
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
      cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue() as string}</Link>
    },
    {
      header: 'Created',
      id: 'createdAt',
      accessorKey: 'enrollee.createdAt',
      enableColumnFilter: false,
      meta: {
        columnType: 'instant'
      },
      cell: info => instantToDefaultString(info.getValue() as unknown as number)
    },
    {
      id: 'lastLogin',
      header: 'Last login',
      enableColumnFilter: false,
      meta: {
        columnType: 'instant'
      },
      accessorFn: row => row.portalParticipantUser?.lastLogin,
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
        return <>{row.original.families.map((family, idx) => <div key={idx}><FamilyLink family={family}
          studyEnvContext={studyEnvContext}/>
        </div>)}</>
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
    }]

    return columns.filter(col => familyLinkageEnabled ? true : col.id !== 'familyShortcode')
  }, [study.shortcode, currentEnv.environmentName, familyLinkageEnabled])

  const table = useReactTable({
    data: participantList,
    columns,
    state: {
      sorting,
      rowSelection,
      columnVisibility
    },
    initialState: {
      pagination: useMemo(() => disablePagination ? undefined : paginationState, [disablePagination])
    },
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    enableColumnFilters: !disableColumnFiltering,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const numSelected = Object.keys(rowSelection).length
  const allowSendEmail = numSelected > 0
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => participantList[parseInt(key)].enrollee.shortcode)

  return <div className="ParticipantList container-fluid px-4 py-2">
    <div className="d-flex align-items-center justify-content-between">
      {header && <>{header}</>}
      {!disableRowVisibilityCount && <div className="d-flex">
        <RowVisibilityCount table={table}/>
      </div>}
      <div className="d-flex">
        <EllipsisDropdownButton variant={'light'} aria-label="Actions" text="Actions" className="ms-auto border my-1"/>
        <ul className="dropdown-menu">
          <ul className="list-unstyled">
            <li>
              <Button onClick={() => setShowEmailModal(allowSendEmail)}
                variant="light"
                className={'dropdown-item d-flex align-items-center'}
                disabled={!allowSendEmail}
                tooltip={allowSendEmail ? 'Send email' : 'Select at least one participant'}>
                <FontAwesomeIcon icon={faEnvelope} className="fa-lg me-2"/> Send email
              </Button>
            </li>
            <div className="dropdown-divider my-1"></div>
            <li>
              <DownloadControl table={table} buttonClass={'dropdown-item'}
                fileName={`${portal.shortcode}-ParticipantList-${currentIsoDate()}`}/>
            </li>
            {(currentEnv.environmentName != 'live') && <><div className="dropdown-divider my-1"></div><li>
              <Button variant="light" className={'dropdown-item d-flex align-items-center'}
                tooltip={'Add a synthetic participant to this study environment'}
                onClick={() => setShowSyntheticModal(!showSyntheticModal)}>
                <FontAwesomeIcon icon={faPlus} className={'fa-lg me-2'}/> Add synthetic participant
              </Button>
            </li></>
            }
          </ul>
        </ul>


        <ColumnVisibilityControl table={table}/>
        {showEmailModal && <AdHocEmailModal enrolleeShortcodes={enrolleesSelected}
          studyEnvContext={studyEnvContext}
          onDismiss={() => setShowEmailModal(false)}/>}
        {showSyntheticModal &&
            <CreateSyntheticEnrolleeModal studyEnvContext={studyEnvContext}
              onDismiss={() => setShowSyntheticModal(false)}
              onSubmit={() => {
                setShowSyntheticModal(false)
                reload()
              }}
            />}
      </div>
    </div>
    {basicTableLayout(table, { filterable: !disableColumnFiltering, tableClass })}
    {renderEmptyMessage(participantList, 'No participants')}
    {!disablePagination && <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>}
  </div>
}


export default ParticipantListTable
