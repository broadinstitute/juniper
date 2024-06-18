import React, {
  useMemo,
  useState
} from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardRow,
  InfoCardTitle
} from 'components/InfoCard'
import { Link } from 'react-router-dom'
import { isEmpty } from 'lodash'
import { FamilyMembersList } from 'study/families/FamilyMembersList'
import {
  ColumnDef,
  ExpandedState,
  getCoreRowModel,
  getExpandedRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'

/**
 * Overall information about a family.
 */
export const FamilyOverview = (
  {
    family, studyEnvContext
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
  }) => {
  return <div>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={`Family Overview`}/>
      </InfoCardHeader>
      <InfoCardBody>
        {family.proband && <InfoCardRow title={'Proband'}>
          <div className='row mt-2'>
            <div className="col">
              <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={family.proband}/>
            </div>
          </div>
        </InfoCardRow>}
        {family.members && <InfoCardRow title={'Members'}>
          <FamilyMembersList family={family} studyEnvContext={studyEnvContext}/>
        </InfoCardRow>}
      </InfoCardBody>
    </InfoCard>
    <FamilyMembersTable family={family} studyEnvContext={studyEnvContext}/>
  </div>
}

type FamilyMemberWithRelation = {
  member: Enrollee,
  subrow: FamilyMemberWithRelation[]
}
const FamilyMembersTable = ({ family, studyEnvContext }: { family: Family, studyEnvContext: StudyEnvContextT }) => {
  const findRowOrSubrow = (data: FamilyMemberWithRelation[], id: string): FamilyMemberWithRelation | undefined => {
    return data.find(d => {
      if (d.member.id === id) {
        return d
      }
      return findRowOrSubrow(d.subrow, id)
    })
  }

  const getNumParentRelations = (member: Enrollee, family: Family): number => {
    return family.relations?.filter(r => r.targetEnrolleeId === member.id).length || 0
  }

  const flattenFamilyMembers = (family: Family): FamilyMemberWithRelation[] => {
    const out: FamilyMemberWithRelation[] = []
    // sort relations by number of parent relations
    // to ensure that parents are created before children
    const relations = (family.relations || []).sort(
      (a, b) => getNumParentRelations(a, family) - getNumParentRelations(b, family)
    )

    relations.forEach(r => {
      const parentExisting = findRowOrSubrow(out, r.enrolleeId)
      const childExisting = findRowOrSubrow(out, r.targetEnrolleeId)

      if (parentExisting && !childExisting) {
        parentExisting.subrow.push({
          member: family.members?.find(m => m.id === r.targetEnrolleeId) || {} as Enrollee,
          subrow: []
        })
      } else if (!parentExisting && !childExisting) {
        out.push({
          member: family.members?.find(m => m.id === r.enrolleeId) || {} as Enrollee,
          subrow: [{
            member: family.members?.find(m => m.id === r.targetEnrolleeId) || {} as Enrollee,
            subrow: []
          }]
        })
      }
    })

    return out
  }

  const members = useMemo(() => flattenFamilyMembers(family), [family])

  console.log(members)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [expanded, setExpanded] = useState<ExpandedState>({})

  const columns: ColumnDef<FamilyMemberWithRelation>[] = [
    {
      id: 'expanded',
      header: '',
      accessorKey: 'expanded',
      cell: ({ row }) => {
        return <button
          className="btn"
          onClick={() => row.toggleExpanded()}
          disabled={!row.getCanExpand()}
        >
          {row.getIsExpanded() ? '-' : '+'}
        </button>
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
    getSubRows: row => row.subrow,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getExpandedRowModel: getExpandedRowModel()
  })

  return <div>
    {basicTableLayout(table)}
  </div>
}

/**
 *
 */
export const EnrolleeLink = ({ studyEnvContext, enrollee }: {
  studyEnvContext: StudyEnvContextT,
  enrollee: Enrollee
}) => {
  const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`.trim()
  const path = `${studyEnvContext.currentEnvPath}/participants/${enrollee.shortcode}`
  if (isEmpty(name)) {
    return <Link to={path}>{enrollee.shortcode}</Link>
  }
  return <span>
    {name} <Link
      to={path}>
      <span className=" fst-italic">({enrollee.shortcode})</span>
    </Link>
  </span>
}
