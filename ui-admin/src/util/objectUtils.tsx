import { isNil } from 'lodash'

export type ObjectDiff = {
  fieldName: string,
  oldValue: string,
  newValue: string
}
// Given a parent DataChangeRecord that records a whole object's changes,
// create one sub DataChangeRecord that records a single change between those
// objects
const createDiff = (
  fieldName: string, oldValue: string, newValue: string
): ObjectDiff => {
  return {
    fieldName,
    oldValue,
    newValue
  }
}


// Get all non-internal fields across n objects
const getAllFields = (...objs: object[]): Set<string> => {
  const allFields: string[] = []
  objs.forEach(obj => {
    if (!isNil(obj)) {
      allFields.push(...Object.keys(obj))
    }
  })

  return new Set(allFields)
}

/**
 * Recursive helper function that traverses the provided objects and returns an ObjectDiff
 * for every field that was changed (added, removed, or updated). This function compares deeply, i.e.,
 * will check the fields of nested objects.
 */
export const findDifferencesBetweenTwoObjects = (
  newObject: { [index: string]: object | string | boolean },
  oldObject: { [index: string]: object | string | boolean },
  nestedFields: string[] = []
): ObjectDiff[] => {
  const changes: ObjectDiff[] = []
  const fieldPrefix = nestedFields.join('.') + (nestedFields.length > 0 ? '.' : '')

  // go through every possible field to see what fields changed
  getAllFields(newObject, oldObject).forEach((field: string) => {
    // case 1: either of the fields is an object - need to recurse
    //         another level deeper into the object
    if ((!isNil(newObject) && typeof newObject[field] === 'object' && !Array.isArray(newObject[field]))
      || (!isNil(oldObject) && typeof oldObject[field] === 'object' && !Array.isArray(oldObject[field]))) {
      // if either is an object, we should recurse deeper
      changes.push(...findDifferencesBetweenTwoObjects(
        newObject && newObject[field] as { [index: string]: object },
        oldObject && oldObject[field] as { [index: string]: object },
        nestedFields.concat(field)))
      return
    }

    const oldValue = (isNil(oldObject) ? '' : oldObject[field])
    const newValue = (isNil(newObject) ? '' : newObject[field])

    // case 2: neither are an object, so now it's a simple string
    //         conversion and comparison
    const oldValueString = (isNil(oldValue) ? '' : oldValue.toString())
    const newValueString = (isNil(newValue) ? '' : newValue.toString())

    if (oldValueString !== newValueString) {
      changes.push(createDiff(fieldPrefix + field, oldValueString, newValueString))
    }
  })
  return changes
}
