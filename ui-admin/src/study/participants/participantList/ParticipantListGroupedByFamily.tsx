import React, {
  useMemo,
  useState
} from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
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
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { getFamilyNames } from 'util/familyUtils'
import { NavLink } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faChevronDown,
  faChevronUp
} from '@fortawesome/free-solid-svg-icons'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'

type FamilyWithSearchResults = Family & { searchResults: EnrolleeSearchExpressionResult[] }

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
  const [families, setFamilies] = useState<Family[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const loadedFamilies = await Api.getAllFamilies(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName)
    setFamilies(loadedFamilies)
    console.log(loadedFamilies)
  })

  const columns = useMemo<ColumnDef<FamilyWithSearchResults>[]>(() => [{
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
  }, {
    header: '# Members',
    accessorKey: 'members',
    enableColumnFilter: true,
    accessorFn: family => family.members?.length
  }, {
    header: 'Created At',
    accessorKey: 'createdAt',
    enableColumnFilter: false,
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }], [])

  const familiesWithSearchResults = useMemo<FamilyWithSearchResults[]>(() => {
    return families.map(family => {
      return {
        ...family,
        searchResults: participantList
          .filter(participant =>
            participant
              .families
              .some(participantFamily => participantFamily.shortcode === family.shortcode))
      }
    }).filter(family => family.searchResults.length > 0)
  }, [participantList, families])


  const table = useReactTable({
    data: familiesWithSearchResults,
    columns,
    state: {
      pagination: paginationState,
      sorting
    },
    enableRowSelection: true,
    enableColumnFilters: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getExpandedRowModel: getExpandedRowModel()
  })

  const renderFamilyParticipantTable = (row: Row<FamilyWithSearchResults>) => {
    if (!row.getIsExpanded()) { return <></> }
    return <tr>
      <td colSpan={row.getAllCells().length}>
        <div className={'border rounded-3 shadow-sm bg-light'}>
          <ParticipantListTable
            participantList={row.original.searchResults}
            studyEnvContext={studyEnvContext}
            familyId={row.original.id}
            disablePagination={true}
            disableRowVisibilityCount={true}
            disableColumnFiltering={true}
            header={<div>
              <h5>{getFamilyNames(row.original)} Family</h5>
              {row.original.members?.length !== row.original.searchResults.length &&
                  <p className="fst-italic">
                      Showing {row.original.searchResults.length}/{row.original.members?.length || 0} members
                  </p>}
            </div>}
            tableClass={'table table-light'}
          />
        </div>
      </td>
    </tr>
  }

  if (isLoading) {
    return <LoadingSpinner/>
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
