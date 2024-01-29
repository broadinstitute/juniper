import React, { useState } from 'react'
import Api, { DataChangeRecord, Enrollee } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'
import { useLoadingEffect } from '../../api/api-utils'
import { findDifferencesBetweenObjects, ObjectDiff } from '../../util/objectUtils'
import { isEmpty } from 'lodash'
import ExpandableText from '../../components/ExpandableText'


// some records contain whole objects, so we want
// to manually look into the object to see what
// fields changed, which could more than one field
const calcChanges = (record: DataChangeRecord): ObjectDiff[] => {
  try {
    const newObject: { [index: string]: object } = isEmpty(record.newValue) ? {} : JSON.parse(record.newValue)
    const oldObject: { [index: string]: object } = isEmpty(record.newValue) ? {} : JSON.parse(record.oldValue)

    if ((newObject && typeof newObject === 'object') && (oldObject && typeof oldObject === 'object')) {
      return findDifferencesBetweenObjects(oldObject, newObject)
    }

    return [{ oldValue: record.oldValue, newValue: record.newValue, fieldName: record.fieldName || '' }]
  } catch (e: unknown) {
    return [{ oldValue: record.oldValue, newValue: record.newValue, fieldName: record.fieldName || '' }]
  }
}

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function DataChangeRecords({ enrollee, studyEnvContext }:
                                                {enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const [notifications, setNotifications] = useState<DataChangeRecord[]>([])

  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const [adminNames] = useState<Map<string, string>>(new Map)

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
      header: 'Updates',
      cell: ({ row }) => {
        const changes = calcChanges(row.original)
        return (
          <div>
            {changes.map((change, idx) =>
              <p key={idx} className="mb-0">
                {change.fieldName}: {change.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {change.newValue}
              </p>
            )}
          </div>
        )
      }
    },
    {
      header: 'Justification',
      accessorKey: 'justification',
      cell: ({ row }) => <ExpandableText text={row.original.justification || ''} maxLen={60}/>
    },
    {
      header: 'Source',
      accessorFn: originalRow => {
        if (originalRow.responsibleAdminUserId) {
          return `Admin (${adminNames.get(originalRow.responsibleAdminUserId)})`
        }
        return 'Participant'
      }
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
    getSortedRowModel: getSortedRowModel(),
    debugTable: true
  })


  const { isLoading } = useLoadingEffect(async () => {
    const [dataChangeRecords, adminUsers] = await Promise.all([
      await Api.fetchEnrolleeChangeRecords(
        portal.shortcode,
        study.shortcode,
        currentEnv.environmentName,
        enrollee.shortcode
      ),
      Api.fetchAdminUsers()
    ])
    adminUsers.forEach(adminUser => {
      adminNames.set(adminUser.id, adminUser.username.split('@')[0])
    })
    setNotifications(dataChangeRecords)
  }, [enrollee.shortcode])
  return <div>
    <h5>Audit history</h5>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

