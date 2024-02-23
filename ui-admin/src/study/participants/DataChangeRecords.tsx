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
      header: 'Update',
      cell: ({ row }) => {
        const diffs: ObjectDiff[] = []

        try {
          const newObject: {
            [index: string]: object
          } = !isEmpty(row.original.newValue) ? JSON.parse(row.original.newValue) : {}
          const oldObject: {
            [index: string]: object
          } = !isEmpty(row.original.oldValue) ? JSON.parse(row.original.oldValue) : {}

          if ((newObject && typeof newObject === 'object') && (oldObject && typeof oldObject === 'object')) {
            diffs.push(...findDifferencesBetweenObjects(oldObject, newObject))
          } else {
            diffs.push({
              fieldName: row.original.fieldName || row.original.modelName,
              oldValue: row.original.oldValue,
              newValue: row.original.newValue
            })
          }
        } catch (e: unknown) {
          diffs.push({
            fieldName: row.original.fieldName || row.original.modelName,
            oldValue: row.original.oldValue,
            newValue: row.original.newValue
          })
        }

        return (
          <div>
            {
              diffs.map((diff, idx) => (
                <div key={idx}>
                  {diff.fieldName}: {diff.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {diff.newValue}
                </div>
              ))
            }
          </div>
        )
      }
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
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

