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


// Given a parent DataChangeRecord that records a whole object's changes,
// create one sub DataChangeRecord that records a single change between those
// objects
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

const internalFields = ['id', 'createdAt', 'lastUpdatedAt']
// Gets all  the fields of an object that are non-internal
// (e.g., 'id', 'createdAt', 'mailingAddressId', etc.)
const getNonInternalFields = (obj: object): string[] => {
  if (isNil(obj)) {
    return []
  }

  return Object.keys(obj)
    .filter(field => !field.endsWith('Id'))
    .filter(field => !internalFields.includes(field))
}

// Get all non-internal fields across n objects
const getAllNonInternalFields = (...objs: object[]): Set<string> => {
  const allFields: string[] = []
  objs.forEach(obj => {
    allFields.push(...getNonInternalFields(obj))
  })

  return new Set(allFields)
}

/**
 * Recursive helper function that traverses the provided objects and returns a separate DataChangeRecord
 * for every field that was changed (added, removed, or updated). This function compares deeply, i.e.,
 * will check the fields of nested objects.
 */
const traverseObjectAndCreateDataChangeRecords = (
  parent: DataChangeRecord,
  newObject: { [index: string]: object },
  oldObject: { [index: string]: object },
  nestedFields: string[] = []
): ReadonlyArray<DataChangeRecord> => {
  const changes: DataChangeRecord[] = []
  const fieldPrefix = nestedFields.join('.') + (nestedFields.length > 0 ? '.' : '')

  // go through every possible field to see what fields changed
  getAllNonInternalFields(newObject, oldObject).forEach((field: string) => {
    // case 1: either of the fields is an object - need to recurse
    //         another level deeper into the object
    if ((!isNil(newObject) && typeof newObject[field] === 'object' && !Array.isArray(newObject[field]))
      || (!isNil(oldObject) && typeof oldObject[field] === 'object' && !Array.isArray(oldObject[field]))) {
      // if either is an object, we should recurse deeper
      changes.push(...traverseObjectAndCreateDataChangeRecords(parent, newObject && newObject[field] as {
        [index: string]: object
      }, oldObject && oldObject[field] as { [index: string]: object }, nestedFields.concat(field)))
      return
    }

    const oldValue = (isNil(oldObject) ? '' : oldObject[field])
    const newValue = (isNil(newObject) ? '' : newObject[field])

    // case 2: neither are an object, so now it's a simple string
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

