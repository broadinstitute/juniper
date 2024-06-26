import React from 'react'
import {
  EnrolleeRelation,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import Creatable from 'react-select/creatable'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'

/**
 * Editable view of the relationships within a family.
 */
export const FamilyRelations = ({
  family, studyEnvContext
}:{
  family: Family, studyEnvContext: StudyEnvContextT
}) => {
  const columns: ColumnDef<EnrolleeRelation>[] = [{
    header: 'Enrollee',
    accessorKey: 'enrolleeId',
    cell: ({ row }) => {
      const enrollee = family.members?.find(m => m.id === row.original.enrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  }, {
    header: 'Relation',
    accessorKey: 'familyRelationship',
    cell: ({ row }) => {
      const relationship = row.original.familyRelationship
      return <span className="fst-italic">
        is the <Creatable
          isDisabled
          options={[]}
          value={{ value: relationship, label: relationship }}
          className={'d-inline-block'}
        /> of
      </span>
    }
  }, {
    header: 'Target',
    accessorKey: 'targetEnrolleeId',
    cell: ({ row }) => {
      const enrollee = family.members?.find(m => m.id === row.original.targetEnrolleeId)
      return <EnrolleeLink studyEnvContext={studyEnvContext} enrollee={enrollee}/>
    }
  }]


  const table = useReactTable({
    data: family.relations || [],
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <div>
    {basicTableLayout(table, { trClass: 'align-middle' })}
    <div className="d-flex justify-content-end">
      <button className="btn btn-link" disabled>+ Add new family relation</button>
    </div>
  </div>
}
