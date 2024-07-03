import React, { useMemo } from 'react'
import {
  Enrollee,
  Family,
  instantToDefaultString
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DataChangeRecord } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { get } from 'lodash'
import { renderDiff } from 'util/changeRecordUtils'

type DataChangeRecordWithEnrollees = DataChangeRecord & {
  enrollee?: Enrollee
  targetEnrollee?: Enrollee
}

/**
 * Renders a table of audit records for a family.
 */
export const FamilyAuditTable = ({
  family, studyEnvContext
} : {
  family: Family, studyEnvContext: StudyEnvContextT
}) => {
  const { users } = useAdminUserContext()
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [changeRecords, setChangeRecords] = React.useState<DataChangeRecordWithEnrollees[]>([])

  const {
    isLoading
  } = useLoadingEffect(async () => {
    const newChangeRecords = await Api.fetchFamilyChangeRecords(
      studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName,
      family.shortcode
    )
    const enrichedChangeRecords = await Promise.all(newChangeRecords.map(fetchEnrollees))

    setChangeRecords(enrichedChangeRecords)
  })

  const getFamilyMemberOrFetch = async (enrolleeId: string): Promise<Enrollee> => {
    const familyMember = family.members?.find(m => m.id === enrolleeId)
    if (!familyMember) {
      return await Api.getEnrollee(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrolleeId)
    }

    return Promise.resolve(familyMember)
  }

  const tryGetProperty = (obj: string, prop: string): string | undefined => {
    const parsed = JSON.parse(obj)
    return get(parsed, prop, undefined)
  }

  const fetchEnrollees = async (record: DataChangeRecord): Promise<DataChangeRecordWithEnrollees> => {
    if (['FamilyEnrollee', 'EnrolleeRelation', 'Family'].includes(record.modelName)) {
      const oldValue = record.oldValue || '{}'
      const newValue = record.newValue || '{}'

      const enrolleeId = tryGetProperty(newValue, 'enrolleeId')
        || tryGetProperty(oldValue, 'enrolleeId')
        || tryGetProperty(newValue, 'probandEnrolleeId')
        || tryGetProperty(oldValue, 'probandEnrolleeId')
      const targetEnrolleeId = tryGetProperty(newValue, 'targetEnrolleeId')
        || tryGetProperty(oldValue, 'targetEnrolleeId')

      // fetch the enrollee (only if they aren't in family); the assumption is that
      // there shouldn't be too many of these, so it's not a big deal to fetch
      // individually
      const enrollee = enrolleeId ? await getFamilyMemberOrFetch(enrolleeId) : undefined
      const targetEnrollee = targetEnrolleeId ? await getFamilyMemberOrFetch(targetEnrolleeId) : undefined

      return {
        ...record,
        enrollee,
        targetEnrollee
      }
    }

    return record
  }

  const columns = useMemo<ColumnDef<DataChangeRecordWithEnrollees>[]>(() => {
    return [
      {
        header: 'Time',
        accessorKey: 'createdAt',
        cell: info => instantToDefaultString(info.getValue() as number)
      },
      {
        header: 'Model',
        accessorKey: 'modelName'
      },
      {
        header: 'Type',
        cell: ({ row }) => {
          const oldValue = row.original.oldValue
          const newValue = row.original.newValue
          if (oldValue && !newValue) {
            return 'Deleted'
          } else if (!oldValue && newValue) {
            return 'Created'
          } else {
            return 'Updated'
          }
        }
      },
      {
        header: 'Update',
        cell: ({ row }) => {
          return renderDiff(row.original)
        }
      },
      {
        header: 'Enrollee',
        cell: ({ row }) => {
          const enrollee = row.original.enrollee
          return enrollee && <EnrolleeLink enrollee={enrollee} studyEnvContext={studyEnvContext}/>
        }
      },
      {
        header: 'Target Enrollee',
        cell: ({ row }) => {
          const enrollee = row.original.targetEnrollee
          return enrollee && <EnrolleeLink enrollee={enrollee} studyEnvContext={studyEnvContext}/>
        }
      },
      {
        header: 'Justification',
        accessorKey: 'justification'
      },
      {
        header: 'Source',
        cell: ({ row }) => (
          row.original.responsibleUserId ? 'Participant' :
            row.original.responsibleAdminUserId &&
            `Admin (${(users.find(u => u.id === row.original.responsibleAdminUserId)?.username)})`
        )
      }
    ]
  }, [users])


  const table = useReactTable({
    data: changeRecords,
    columns,
    state: {
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  if (isLoading) {
    return <LoadingSpinner/>
  }
  return <div>
    {basicTableLayout(table)}
  </div>
}
