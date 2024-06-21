import {
  Enrollee,
  EnrolleeRelation,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import React, {
  useEffect,
  useMemo,
  useState
} from 'react'
import {
  ColumnDef,
  ExpandedState,
  getCoreRowModel,
  getExpandedRowModel,
  getSortedRowModel,
  Row,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Link } from 'react-router-dom'
import {
  isNil,
  lowerCase,
  startCase
} from 'lodash'
import { basicTableLayout } from 'util/tableUtils'
import {
  faCaretDown,
  faCaretUp
} from '@fortawesome/free-solid-svg-icons'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { EnrolleeLink } from './FamilyOverview'

type FamilyMemberWithSubrows = {
  member: Enrollee,
  relationToParentRow?: EnrolleeRelation
  subrows: FamilyMemberWithSubrows[]
}

/**
 * Renders a table of family members grouped by their relationships to each other.
 */
export const FamilyStructureTable = (
  { family, studyEnvContext }: { family: Family, studyEnvContext: StudyEnvContextT }
) => {
  const members = useMemo(() => groupFamilyMembersWithSubrows(family), [family])

  const [sorting, setSorting] = useState<SortingState>([])
  const [expanded, setExpanded] = useState<ExpandedState>({})


  const columns: ColumnDef<FamilyMemberWithSubrows>[] = [
    {
      id: 'expanded',
      header: '',
      accessorKey: 'expanded',
      cell: ({ row }) => {
        return <div
          style={{
            paddingLeft: `${row.depth * 2}rem`
          }}
        >
          <button
            className="btn btn-link"
            onClick={() => row.toggleExpanded()}
            disabled={!row.getCanExpand()}
          >
            {row.getIsExpanded() ? <FontAwesomeIcon icon={faCaretUp}/> : <FontAwesomeIcon icon={faCaretDown}/>}
          </button>
        </div>
      }
    },
    {
      id: 'shortcode',
      header: 'Shortcode',
      accessorKey: 'member.shortcode',
      cell: ({ row }) => {
        return <Link to={`${studyEnvContext.currentEnvPath}/participants/${row.original.member.shortcode}`}>
          {row.original.member.shortcode}
        </Link>
      }
    },
    {
      id: 'name',
      header: 'Name',
      accessorKey: 'member.profile.givenName',
      cell: ({ row }) => {
        return `${row.original.member.profile?.givenName || ''} ${row.original.member.profile?.familyName || ''}`.trim()
      }
    },
    {
      id: 'proxyRelation',
      header: 'Proxies',
      cell: ({ row }) => {
        return <ProxyList
          member={row.original.member}
          family={family}
          studyEnvContext={studyEnvContext}/>
      }
    }
  ]

  const table = useReactTable({
    data: members,
    columns,
    state: {
      expanded,
      sorting
    },
    onExpandedChange: setExpanded,
    enableExpanding: true,
    getSubRows: row => row.subrows,

    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getExpandedRowModel: getExpandedRowModel()
  })

  const customRowHeader = (row: Row<FamilyMemberWithSubrows>) => {
    const parentRow = row.getParentRow()
    if (!isNil(parentRow)) {
      const parent = parentRow.original
      const relation = row.original.relationToParentRow?.familyRelationship
      const subRowIdx = parent.subrows.findIndex(s => s.member.id === row.original.member.id)
      const showHeader = (
        subRowIdx === 0 || parent.subrows[subRowIdx - 1].relationToParentRow?.familyRelationship != relation
      )
      if (showHeader) {
        return <tr>
          <td colSpan={table.getFlatHeaders().length}>
            <div style={{
              paddingLeft: `${row.depth * 2 }rem`
            }}>
              <span className='fst-italic'>
                {startCase(lowerCase(relation))} of:
              </span>
            </div>
          </td>
        </tr>
      }
    }
    return <></>
  }

  useEffect(() => {
    table.toggleAllRowsExpanded(true)
  }, [])

  return <div>
    {basicTableLayout(table, { customRowHeader })}
  </div>
}

// Groups family members by their relationships to each other; since we're showing a tree structure,
// we can't guarantee every relationship is represented if there are cycles in the family graph.
const groupFamilyMembersWithSubrows = (family: Family): FamilyMemberWithSubrows[] => {
  const out: FamilyMemberWithSubrows[] = []
  // sort relations by number of parent relations
  // to ensure that parents are created before children
  const relations = (family.relations || []).sort(
    (a, b) => getNumParentRelations(a, family) - getNumParentRelations(b, family)
  )

  relations.forEach(r => {
    const parentExisting = findRowOrSubrow(out, r.enrolleeId)
    const childExisting = findRowOrSubrow(out, r.targetEnrolleeId)

    if (parentExisting && !childExisting) {
      parentExisting.subrows.push({
        member: family.members?.find(m => m.id === r.targetEnrolleeId),
        relationToParentRow: r,
        subrows: []
      })
      sortByFamilyRelationship(parentExisting.subrows)
    } else if (!parentExisting && !childExisting) {
      out.push({
        member: family.members?.find(m => m.id === r.enrolleeId),
        subrows: [{
          member: family.members?.find(m => m.id === r.targetEnrolleeId),
          relationToParentRow: r,
          subrows: []
        }]
      })
    }
  })

  return out
}

const getNumParentRelations = (member: Enrollee, family: Family): number => {
  return family.relations?.filter(r => r.targetEnrolleeId === member.id).length || 0
}

const findRowOrSubrow = (data: FamilyMemberWithSubrows[], id: string): FamilyMemberWithSubrows | undefined => {
  return data.find(d => {
    if (d.member.id === id) {
      return d
    }
    return findRowOrSubrow(d.subrows, id)
  })
}

const sortByFamilyRelationship = (rows: FamilyMemberWithSubrows[]): void => {
  rows.sort((a, b) => {
    if (a.relationToParentRow
      && b.relationToParentRow
      && a.relationToParentRow.familyRelationship
      && b.relationToParentRow.familyRelationship) {
      return a.relationToParentRow.familyRelationship.localeCompare(b.relationToParentRow.familyRelationship)
    }
    return 0
  })
}

// Renders a list of proxies of a family member
const ProxyList = ({ member, family, studyEnvContext }: {
  member: Enrollee,
  family: Family,
  studyEnvContext: StudyEnvContextT
}) => {
  const [proxies, setProxies] = useState<Enrollee[]>([])
  const { isLoading } = useLoadingEffect(async () => {
    const loadedProxies = await Promise.all((member.relations || [])
      .filter(r => r.targetEnrolleeId === member.id && r.relationshipType === 'PROXY')
      .map(async r => {
        // see if the proxy is in the family, if so we already have it loaded
        const familyProxy: Enrollee = family.members?.find(m => m.id === r.enrolleeId)
        if (!isNil(familyProxy)) {
          return Promise.resolve(familyProxy)
        }

        // in the rare case someone has a proxy outside the family,
        // let's grab it from the API. Since this seems an usual case,
        // we don't need to worry about performance here.
        return Api.getEnrollee(
          studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          r.enrolleeId)
      }))

    setProxies(loadedProxies)
  })

  return <>
    {isLoading ? <LoadingSpinner/> : <div>
      {proxies.map(p => <div key={p.id}>
        <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={p}/>
      </div>)}
    </div>}
  </>
}
