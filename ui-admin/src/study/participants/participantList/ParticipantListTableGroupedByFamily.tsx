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
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'

type FamilyWithSearchResults = Partial<Family> & { searchResults: EnrolleeSearchExpressionResult[] }

/**
 * Shows a table of participants grouped by their family.
 */
function ParticipantListTableGroupedByFamily({
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
  }, [])

  const columns = useMemo<ColumnDef<FamilyWithSearchResults>[]>(() => [{
    header: '',
    accessorKey: 'expanded',
    enableColumnFilter: false,
    enableColumnSort: false,
    cell: ({ row }) => {
      return <>
        <button
          className="btn btn-link m-0 p-0"
          onClick={() => row.toggleExpanded()}>
          {row.getIsExpanded()
            ? <FontAwesomeIcon icon={faChevronDown}/>
            : <FontAwesomeIcon icon={faChevronUp}/>
          }
        </button>
        <span> ({row.original.searchResults.length})</span></>
    }
  }, {
    header: 'Shortcode',
    accessorKey: 'shortcode',
    meta: {
      columnType: 'string'
    },
    cell: ({ row }) => {
      if (!row.original.shortcode) {
        return <span className="fst-italic">No family</span>
      }
      return <NavLink to={`${studyEnvContext.currentEnvPath}/families/${row.original.shortcode}`}>
        {row.original.shortcode}
      </NavLink>
    }
  },
  {
    header: 'Family Name',
    accessorKey: 'familyName',
    accessorFn: family => family.shortcode && `${getFamilyNames(family as Family)} Family`
  }, {
    header: '# Members',
    accessorKey: 'members',
    enableColumnFilter: true,
    accessorFn: family => family.members?.length
  }, {
    header: 'Proband',
    accessorKey: 'proband',
    enableColumnFilter: false,
    cell: ({ row }) => {
      if (!row.original.proband) {
        return <></>
      }
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={row.original.proband}/>
    }
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
    const familiesWithResults: FamilyWithSearchResults[] = families.map(family => {
      return {
        ...family,
        searchResults: participantList
          .filter(participant =>
            participant
              .families
              .some(participantFamily => participantFamily.shortcode === family.shortcode))
      }
    }).filter(family => family.searchResults.length > 0)

    const participantsWithoutFamily = participantList.filter(participant => !participant.families.length)

    if (participantsWithoutFamily.length > 0) {
      familiesWithResults.push({
        searchResults: participantsWithoutFamily
      })
    }

    return familiesWithResults
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
            disablePagination={row.original.searchResults.length < 10}
            disableRowVisibilityCount={true}
            disableColumnFiltering={true}
            header={row.original.shortcode && <div>
              <h5>{getFamilyNames(row.original as Family)} Family</h5>
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


export default ParticipantListTableGroupedByFamily
