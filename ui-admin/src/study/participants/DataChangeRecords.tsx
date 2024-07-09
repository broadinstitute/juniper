import React, { useState } from 'react'
import Api, { DataChangeRecord } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  Enrollee,
  instantToDefaultString
} from '@juniper/ui-core'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { useLoadingEffect } from 'api/api-utils'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { renderDiff } from 'util/changeRecordUtils'


/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function DataChangeRecords({ enrollee, studyEnvContext }:
                                                {enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const { users } = useAdminUserContext()
  const [notifications, setNotifications] = useState<DataChangeRecord[]>([])

  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const columns: ColumnDef<DataChangeRecord>[] = [
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
      header: 'Update',
      cell: ({ row }) => {
        return renderDiff(row.original)
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

  const table = useReactTable({
    data: notifications,
    columns,
    state: {
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })


  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchEnrolleeChangeRecords(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      enrollee.shortcode
    )
    setNotifications(response)
  }, [enrollee.shortcode])
  return <div>
    <h5>Audit history</h5>
    <dl >
      <dt className="fw-semibold">Enrollee internal ID</dt>
      <dd>{enrollee.id}</dd>
      <dt className="fw-semibold">Enrollee created</dt>
      <dd>{instantToDefaultString(enrollee.createdAt)}</dd>
    </dl>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

