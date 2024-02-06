import React, { useState } from 'react'
import Api, { DataChangeRecord, Enrollee } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'
import { useLoadingEffect } from '../../api/api-utils'
import { findDifferencesBetweenObjects, ObjectDiff } from '../../util/objectUtils'


// some records contain whole objects, so we want
// to manually look into the object to see what
// fields changed, which could more than one field
const flattenDataChangeRecords = (record: DataChangeRecord): ReadonlyArray<DataChangeRecord> => {
  try {
    const newObject: { [index: string]: object } = JSON.parse(record.newValue)
    const oldObject: { [index: string]: object } = JSON.parse(record.oldValue)

    if ((newObject && typeof newObject === 'object') || (oldObject && typeof oldObject === 'object')) {
      const diffs: ObjectDiff[] = findDifferencesBetweenObjects(oldObject, newObject)

      return diffs.map<DataChangeRecord>((diff: ObjectDiff) => {
        return {
          ...record,
          ...diff
        }
      })
    }

    return [record]
  } catch (e: unknown) {
    return [record]
  }
}

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function DataChangeRecords({ enrollee, studyEnvContext }:
                                                {enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
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
      header: 'Field',
      accessorKey: 'fieldName'
    },
    {
      header: 'Update',
      cell: ({ row }) => (
        <div>
          {row.original.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {row.original.newValue}
        </div>
      )
    },
    {
      header: 'Justification',
      accessorKey: 'justification'
    },
    {
      header: 'Source',
      cell: ({ row }) => (
        row.original.responsibleUserId ? 'Participant' : 'Admin'
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
    getSortedRowModel: getSortedRowModel(),
    debugTable: true
  })


  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchEnrolleeChangeRecords(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      enrollee.shortcode
    )
    setNotifications(response.flatMap(flattenDataChangeRecords))
  }, [enrollee.shortcode])
  return <div>
    <h5>Audit history</h5>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

