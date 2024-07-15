import { Family } from '@juniper/ui-core'
import { uniq } from 'lodash/fp'
import { isEmpty } from 'lodash'

/**
 * Returns a string of family names, separated by commas and an 'and' before the last name.
 * For example, a family with the members 'Alice Tester', 'Bob Reviewer', and 'Charlie Examiner'
 * would return 'Tester, Reviewer and Examiner'.
 */
export const getFamilyNameString = (family: Family): string => {
  const familyNames = family
    ?.members
    ?.filter(enrollee => !isEmpty(enrollee?.profile?.familyName))
    ?.map(enrollee => enrollee?.profile?.familyName) || ['']
  const uniqNames = uniq(familyNames)
  if (uniqNames.length === 1) {
    return uniqNames[0]
  } else if (uniqNames.length === 2) {
    return `${uniqNames[0]} and ${uniqNames[1]}`
  } else {
    return `${uniqNames.slice(0, uniqNames.length - 1).join(', ')} and ${uniqNames[uniqNames.length - 1]}`
  }
}
