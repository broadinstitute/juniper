import React, { useState } from 'react'
import Api, { DataChangeRecord, Enrollee } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { isNil } from 'lodash'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'
import { useLoadingEffect } from '../../api/api-utils'


const createSubDataChangeRecord = (
  original: DataChangeRecord, fieldName: string, oldValue: string, newValue: string
): DataChangeRecord => {
  return {
    ...original,
    fieldName,
    oldValue,
    newValue
  }
}

const getNonInternalFields = (obj: object): string[] => {
  return Object.keys(obj)
    .filter(field => !field.endsWith('Id'))
    .filter(field => !internalFields.includes(field))
}

const internalFields = ['id', 'createdAt', 'lastUpdatedAt']
const getAllFields = (obj1: object, obj2: object): Set<string> => {
  return new Set(
    getNonInternalFields(obj1)
      .concat(...getNonInternalFields(obj2))
  )
}

const traverseObjectAndCreateDataChangeRecords = (
  parent: DataChangeRecord,
  newObject: { [index: string]: object },
  oldObject: { [index: string]: object },
  nestedFields: string[] = []
): ReadonlyArray<DataChangeRecord> => {
  const changes: DataChangeRecord[] = []
  const fieldPrefix = nestedFields.join('.') + (nestedFields.length > 0 ? '.' : '')

  // case 1: new object is null, create deletion records for each field in the new object.
  //         technically, this is a shallow traversal, but we are unlikely to make deeply
  //         nested objects
  if (isNil(newObject)) {
    return getNonInternalFields(oldObject).map((field: string) => {
      return createSubDataChangeRecord(parent, fieldPrefix + field, oldObject[field].toString(), '')
    })
  }

  // case 2: opposite of case one, old object is null, create creation records for each
  if (isNil(oldObject)) {
    return getNonInternalFields(newObject).map(field => {
      return createSubDataChangeRecord(parent, fieldPrefix + field, '', newObject[field].toString())
    })
  }


  // now that we know both objects are valid, recurse through all the
  // fields to get differences between them
  getAllFields(newObject, oldObject).forEach((field: string) => {
    const oldValue = oldObject[field]
    const newValue = newObject[field]
    console.log(oldValue)

    // case 3: either of the fields is an object - need to recurse
    //         another level deeper into the object
    if ((typeof newValue === 'object' && !Array.isArray(newValue))
      || (typeof oldValue === 'object' && !Array.isArray(oldValue))) {
      // if either is an object, we should recurse deeper
      changes.push(...traverseObjectAndCreateDataChangeRecords(parent, newValue as {
        [index: string]: object
      }, oldValue as { [index: string]: object }, nestedFields.concat(field)))
      return
    }

    // case 4: neither are an object, so now it's a simple string
    //         conversion and comparison
    const oldValueString = (isNil(oldValue) ? '' : oldValue.toString())
    const newValueString = (isNil(newValue) ? '' : newValue.toString())

    if (oldValueString !== newValueString) {
      changes.push(createSubDataChangeRecord(parent, fieldPrefix + field, oldValueString, newValueString))
    }
  })
  return changes
}

// some records contain whole objects, so we want
// to manually look into the object to see what
// fields changed, which could more than one field
const flattenDataChangeRecords = (record: DataChangeRecord): ReadonlyArray<DataChangeRecord> => {
  // if a fieldName is specified, then only one field changed,
  // so just return this object
  if (!isNil(record.fieldName) && record.fieldName.length > 0) {
    return [record]
  }

  try {
    const newObject: { [index: string]: object } = JSON.parse(record.newValue)
    const oldObject: { [index: string]: object } = JSON.parse(record.oldValue)

    return traverseObjectAndCreateDataChangeRecords(record, newObject, oldObject)
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

