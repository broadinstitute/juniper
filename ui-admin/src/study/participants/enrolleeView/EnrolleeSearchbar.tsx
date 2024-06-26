import React from 'react'
import AsyncSelect from 'react-select/async'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Enrollee } from '@juniper/ui-core'
import { isEmpty } from 'lodash'

/**
 *
 */
export const EnrolleeSearchbar = ({
  studyEnvContext,
  onEnrolleeSelected,
  selectedEnrollee
}: {
  studyEnvContext: StudyEnvContextT,
  onEnrolleeSelected: (enrollee: Enrollee | null) => void,
  selectedEnrollee: Enrollee | null,
  searchExpFilter?: string
}) => {
  const searchEnrollees = async (value: string): Promise<Enrollee[]> => {
    const results = await Api.executeSearchExpression(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      `{enrollee.shortcode} contains '${value}' or {profile.name} contains '${value}'`)

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
    cacheOptions
    loadOptions={async (value: string) => {
      const enrollees = await searchEnrollees(value)
      return enrollees.map(enrollee => ({ value: enrollee, label: labelForEnrollee(enrollee) }))
    }}
    value={selectedEnrollee ? { value: selectedEnrollee, label: labelForEnrollee(selectedEnrollee) } : null}
    onChange={selectedOption => {
      const newSelectedEnrollee = selectedOption ? selectedOption.value : null
      onEnrolleeSelected(newSelectedEnrollee)
    }}
  />
}
