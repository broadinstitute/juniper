import React, {
  useMemo,
  useState
} from 'react'
import { EnrolleeSearchExpressionResult } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getExpandedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  Row,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  renderEmptyMessage,
  useRoutableTablePaging
} from 'util/tableUtils'
import {
  Family,
  instantToDefaultString
} from '@juniper/ui-core'
import TableClientPagination from 'util/TablePagination'
import { uniqBy } from 'lodash'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { getFamilyNames } from 'util/familyUtils'
import { NavLink } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faChevronDown,
  faChevronUp
} from '@fortawesome/free-solid-svg-icons'

type FamilyWithMembers = Family & { members: EnrolleeSearchExpressionResult[] }

/** Shows a list of (for now) enrollees */
function ParticipantListGroupedByFamily({
  participantList,
  studyEnvContext
}: {
  participantList: EnrolleeSearchExpressionResult[],
  studyEnvContext: StudyEnvContextT
}) {
  const { paginationState, preferredNumRowsKey } = useRoutableTablePaging('participantList')

  const [sorting, setSorting] = useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const columns = useMemo<ColumnDef<FamilyWithMembers>[]>(() => [{
    header: '',
    accessorKey: 'expanded',
    enableColumnFilter: false,
    enableColumnSort: false,
    cell: ({ row }) => {
      return <button
        className="btn btn-link m-0 p-0"
        onClick={() => row.toggleExpanded()}>
        {row.getIsExpanded()
          ? <FontAwesomeIcon icon={faChevronUp}/>
          : <FontAwesomeIcon icon={faChevronDown}/>
        }
      </button>
    }
  }, {
    header: 'Shortcode',
    accessorKey: 'shortcode',
    meta: {
      columnType: 'string'
    },
    cell: ({ row }) => {
      return <NavLink to={`${studyEnvContext.currentEnvPath}/families/${row.original.shortcode}`}>
        {row.original.shortcode}
      </NavLink>
    }
  },
  {
    header: 'Family Name',
    accessorKey: 'familyName',
    accessorFn: family => `${getFamilyNames(family)} Family`
  },  {
    header: 'Created At',
    accessorKey: 'createdAt',
    enableColumnFilter: false,
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }], [])

  const families = useMemo<FamilyWithMembers[]>(() => {
    return uniqBy<Family>(
      participantList
        .flatMap(participant => participant.families),
      fam => fam.id)
      .map<FamilyWithMembers>(family => {
        return {
          ...family,
          members: participantList.filter(participant => participant.families.some(fam => fam.id === family.id))
        }
      })
  }, [participantList])


  const table = useReactTable({
    data: families,
    columns,
    state: {

    },
    initialState: {
      pagination: paginationState,
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getExpandedRowModel: getExpandedRowModel()
  })

  const renderFamilyParticipantTable = (row: Row<FamilyWithMembers>) => {
    if (!row.getIsExpanded()) { return <></> }
    return <tr >
      <td colSpan={row.getAllCells().length}>
        <div className={'border rounded-3 shadow-sm bg-light'}>
          <ParticipantListTable
            participantList={row.original.members}
            studyEnvContext={studyEnvContext}
            familyId={row.original.id}
            disablePagination={true}
            disableRowVisibilityCount={true}
            disableColumnFiltering={true}
            header={`${getFamilyNames(row.original)} Family`}
            tableClass={'table table-light'}
          />

        </div>
      </td>
    </tr>
  }

  return <div className="ParticipantList container-fluid px-4 py-2">
    {basicTableLayout(table, {
      filterable: true,
      customRowFooter: renderFamilyParticipantTable
    })}
    { renderEmptyMessage(participantList, 'No families') }
    <TableClientPagination table={table} preferredNumRowsKey={preferredNumRowsKey}/>
  </div>
}


export default ParticipantListGroupedByFamily
