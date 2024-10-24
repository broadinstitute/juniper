import React from 'react'
import AsyncSelect from 'react-select/async'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Enrollee } from '@juniper/ui-core'
import { isEmpty } from 'lodash'
import { concatSearchExpressions } from 'util/searchExpressionUtils'

/**
 * Searchbar for selecting an enrollee; uses search expressions, so you can pass in a filter
 * to narrow down the results.
 */
export const EnrolleeSearchbar = ({
  studyEnvContext,
  onEnrolleeSelected,
  selectedEnrollee,
  searchExpFilter,
  disabled = false
}: {
  studyEnvContext: StudyEnvContextT,
  onEnrolleeSelected: (enrollee?: Enrollee) => void,
  selectedEnrollee?: Enrollee,
  searchExpFilter?: string,
  disabled?: boolean
}) => {
  const searchEnrollees = async (value: string): Promise<Enrollee[]> => {
    const defaultSearchExp = `({enrollee.shortcode} contains '${value}' or {profile.name} contains '${value}')`

    const searchExp = searchExpFilter
      ? concatSearchExpressions([defaultSearchExp, searchExpFilter])
      : defaultSearchExp

    const results = await Api.executeSearchExpression(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      searchExp,
      { limit: 10 })

    return results.map((result: EnrolleeSearchExpressionResult) => {
      const enrollee = result.enrollee
      enrollee.profile = result.profile
      return enrollee
    })
  }

  const labelForEnrollee = (enrollee: Enrollee): string => {
    if (enrollee.profile && (!isEmpty(enrollee.profile.givenName) || !isEmpty(enrollee.profile.familyName))) {
      return `${enrollee.profile.givenName} ${enrollee.profile.familyName} (${enrollee.shortcode})`
    }

    return enrollee.shortcode
  }

  return <AsyncSelect
    isDisabled={disabled}
    cacheOptions
    defaultOptions
    isClearable
    loadOptions={async (value: string) => {
      const enrollees = await searchEnrollees(value)
      return enrollees.map(enrollee => ({ value: enrollee, label: labelForEnrollee(enrollee) }))
    }}
    value={selectedEnrollee ? { value: selectedEnrollee, label: labelForEnrollee(selectedEnrollee) } : null}
    onChange={selectedOption => {
      const newSelectedEnrollee = selectedOption?.value
      onEnrolleeSelected(newSelectedEnrollee)
    }}
  />
}
