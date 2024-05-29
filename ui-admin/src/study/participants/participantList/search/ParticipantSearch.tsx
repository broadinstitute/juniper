import React, { useEffect, useState } from 'react'
import { Button } from 'components/forms/Button'
import AdvancedSearchModal from './AdvancedSearchModal'
import BasicSearch from './BasicSearch'
import { concatSearchExpressions } from '../../../../util/searchExpressionUtils'
import SearchCriteriaView from './SearchCriteriaView'
import { isEmpty } from 'lodash/fp'
import { StudyEnvContextT } from '../../../StudyEnvironmentRouter'

export type ParticipantSearchState = {
  basicSearch: string,
  minAge?: number,
  maxAge?: number,
  sexAtBirth: string[],
  tasks: { task: string, status: string }[],
  latestKitStatus: string[],
  custom: string
}

/**
 * Converts the search state to a search expression.
 */
export const toExpression = (searchState: ParticipantSearchState) => {
  const expressions: string[] = []
  if (!isEmpty(searchState.basicSearch)) {
    expressions.push(`({profile.name} contains '${searchState.basicSearch}' `
      + `or {profile.contactEmail} contains '${searchState.basicSearch}' `
      + `or {enrollee.shortcode} contains '${searchState.basicSearch}')`)
  }

  if (searchState.minAge) {
    expressions.push(`{age} >= ${searchState.minAge}`)
  }

  if (searchState.maxAge) {
    expressions.push(`{age} <= ${searchState.maxAge}`)
  }

  if (searchState.sexAtBirth.length > 0) {
    const sexAtBirthExpression = `(${
      concatSearchExpressions(
        searchState.sexAtBirth.map((sexAtBirth: string) => {
          return `{profile.sexAtBirth} = '${sexAtBirth}'`
        }),
        'or')
    })`

    expressions.push(sexAtBirthExpression)
  }

  if (searchState.tasks.length > 0) {
    const taskExpressions = `(${
      concatSearchExpressions(
        searchState.tasks.map(({ task, status }) => {
          return `{task.${task}.status} = '${status}'`
        }))
    })`

    expressions.push(taskExpressions)
  }

  if (searchState.latestKitStatus.length > 0) {
    const latestKitStatusExpression = `(${
      concatSearchExpressions(
        searchState.latestKitStatus.map((kitStatus: string) => {
          return `{latestKit.status} = '${kitStatus}'`
        }),
        'or')
    })`

    expressions.push(latestKitStatusExpression)
  }

  if (!isEmpty(searchState.custom)) {
    expressions.push(searchState.custom)
  }

  return concatSearchExpressions(expressions)
}


/** Participant search component for participant list page */
function ParticipantSearch({ studyEnvContext, updateSearchExpression }: {
  studyEnvContext: StudyEnvContextT, updateSearchExpression: (searchExp: string) => void
}) {
  const [advancedSearch, setAdvancedSearch] = useState(false)


  const [searchState, setSearchState] = useState<ParticipantSearchState>({
    basicSearch: '',
    sexAtBirth: [],
    tasks: [],
    latestKitStatus: [],
    custom: ''
  })

  const updateSearchState = (field: keyof ParticipantSearchState, value: unknown) => {
    setSearchState(oldState => {
      return { ...oldState, [field]: value }
    })
  }

  useEffect(() => {
    updateSearchExpression(toExpression(searchState))
  }, [searchState])

  return <div>
    <div className="align-items-baseline d-flex mb-2">
      {advancedSearch && <AdvancedSearchModal
        studyEnvContext={studyEnvContext}
        onDismiss={() => setAdvancedSearch(false)}
        searchState={searchState}
        setSearchState={setSearchState}/>}
      <div className="mb-2">
        <BasicSearch
          searchState={searchState}
          updateSearchState={updateSearchState}/>
      </div>
      <div className="ms-2">
        <Button variant="light" className="border btn-sm"
          onClick={() => setAdvancedSearch(true)}>
        Advanced Search
        </Button>
      </div>
    </div>
    <SearchCriteriaView
      searchState={searchState}
      updateSearchState={updateSearchState}/>

  </div>
}

export default ParticipantSearch
