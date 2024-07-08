import React from 'react'
import { DataChangeRecord } from 'api/api'
import {
  findDifferencesBetweenObjects,
  ObjectDiff
} from '@juniper/ui-core'
import { isEmpty } from 'lodash'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'

/**
 * Renders a detailed line-by-line diff between old/new values of a change record.
 */
export const renderDiff = (changeRecord: DataChangeRecord) => {
  const diffs: ObjectDiff[] = []

  try {
    const newObject: {
      [index: string]: object
    } = !isEmpty(changeRecord.newValue) ? JSON.parse(changeRecord.newValue) : {}
    const oldObject: {
      [index: string]: object
    } = !isEmpty(changeRecord.oldValue) ? JSON.parse(changeRecord.oldValue) : {}

    if ((newObject && typeof newObject === 'object') && (oldObject && typeof oldObject === 'object')) {
      diffs.push(...findDifferencesBetweenObjects(oldObject, newObject))
    } else {
      diffs.push({
        fieldName: changeRecord.fieldName || changeRecord.modelName,
        oldValue: changeRecord.oldValue,
        newValue: changeRecord.newValue
      })
    }
  } catch (e: unknown) {
    diffs.push({
      fieldName: changeRecord.fieldName || changeRecord.modelName,
      oldValue: changeRecord.oldValue,
      newValue: changeRecord.newValue
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
