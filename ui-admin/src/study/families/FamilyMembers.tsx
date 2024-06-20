import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable
} from '@tanstack/react-table'
import { Link } from 'react-router-dom'
import React from 'react'
import { basicTableLayout } from 'util/tableUtils'

/**
 *
 */
export const FamilyMembers = ({
  family, studyEnvContext
}:{
  family: Family, studyEnvContext: StudyEnvContextT
}) => {
  const columns: ColumnDef<Enrollee>[] = [{
    id: 'shortcode',
    header: 'Shortcode',
    accessorKey: 'member.shortcode',
    cell: ({ row }) => {
      return <Link to={`${studyEnvContext.currentEnvPath}/participants/${row.original.shortcode}`}>
        {row.original.shortcode}
      </Link>
    }
  },
  {
    header: 'Given Name',
    accessorKey: 'givenName',
    cell: ({ row }) => {
      return row.original.profile?.givenName
    }
  },
  {
    header: 'Family Name',
    accessorKey: 'familyName',
    cell: ({ row }) => {
      return row.original.profile?.familyName
    }
  }]


  const table = useReactTable({
    data: family.members || [],
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div>
    {basicTableLayout(table)}
    <div className="d-flex justify-content-end">
      <button className="btn btn-link" disabled>+ Add new family member</button>
    </div>
  </div>
}
