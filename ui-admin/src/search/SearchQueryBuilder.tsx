import React, {
  useMemo,
  useState
} from 'react'
import {
  Field,
  FieldSelectorProps,
  formatQuery,
  QueryBuilder,
  RuleGroupType,
  RuleGroupTypeAny
} from 'react-querybuilder'
import 'react-querybuilder/dist/query-builder.scss'
import { ruleProcessorEnrolleeSearchExpression } from '../util/formatQueryBuilderAsSearchExp'
import { useLoadingEffect } from '../api/api-utils'
import Api, { SearchValueType } from '../api/api'
import { StudyEnvContextT } from '../study/StudyEnvironmentRouter'
import Select from 'react-select'
import LoadingSpinner from '../util/LoadingSpinner'
import {
  isEmpty,
  keys
} from 'lodash'
import { parseExpression } from '../util/searchExpressionParser'
import { toReactQueryBuilderState } from '../util/searchExpressionUtils'

/**
 * Frontend for building an enrollee search expression.
 */
export const SearchQueryBuilder = ({
  studyEnvContext,
  onSearchExpressionChange,
  searchExpression
}: {
  studyEnvContext: StudyEnvContextT,
  onSearchExpressionChange: (searchExpression: string) => void,
  searchExpression: string
}) => {
  const tryParseExpression = (expression: string): RuleGroupType | undefined => {
    try {
      return toReactQueryBuilderState(parseExpression(expression))
    } catch (_) {
      return undefined
    }
  }

  const initialQuery = useMemo(() => !isEmpty(searchExpression) ? tryParseExpression(searchExpression) : {
    combinator: 'and',
    rules: []
  }, [])

  const [query, setQuery] = useState<RuleGroupTypeAny | undefined>(initialQuery)


  const [facets, setFacets] = React.useState<{ facet: string, type: SearchValueType }[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const facets = await Api.getExpressionSearchFacets(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName)
    const facetArr = keys(facets).map(facet => {
      return {
        facet,
        type: facets[facet]
      }
    })

    setFacets(facetArr.sort((a, b) => a.facet.localeCompare(b.facet)))
  }, [], 'Failed to load cohort criteria options')


  const updateQuery = (query: RuleGroupTypeAny) => {
    setQuery(query)
    const enrolleeSearchExpression = query.rules.length > 0 ? formatQuery(query, {
      format: 'spel', // not the actual format, but formatquery requires you specify one of their formats
      ruleProcessor: ruleProcessorEnrolleeSearchExpression,
      fallbackExpression: '1 = 1'
    }) : ''

    if (enrolleeSearchExpression === '') {
      return // do nothing; not optimal, but it's a quick fix until we implement antlr for rule parsing in the frontend
    }

    onSearchExpressionChange(enrolleeSearchExpression)
  }


  return <LoadingSpinner isLoading={isLoading}>
    <QueryBuilder
      controlClassnames={{
        fields: 'form-select',
        value: 'form-control',
        operators: 'form-select',
        removeRule: 'btn btn-outline-dark',
        removeGroup: 'btn btn-outline-dark',
        addRule: 'btn btn-outline-dark',
        addGroup: 'btn btn-outline-dark',
        combinators: 'form-select w-25'
      }}
      fields={facets.map(facet => facetToReactQueryField(facet.facet, facet.type))}
      controlElements={{
        fieldSelector: CustomFieldSelector
      }}
      operators={operators}
      query={query || { combinator: 'and', rules: [] }}
      onQueryChange={q => updateQuery(q)}/>
  </LoadingSpinner>
}

const operators = [
  { name: '=', label: '=' },
  { name: '!=', label: '!=' },
  { name: '<', label: '<' },
  { name: '<=', label: '<=' },
  { name: '>', label: '>' },
  { name: '>=', label: '>=' },
  { name: 'contains', label: 'contains' }
]


const facetToReactQueryField = (facet: string, searchValueType: SearchValueType): Field => {
  const field: Field = {
    name: facet,
    label: facet
  }
  switch (searchValueType) {
    case 'STRING':
      field.valueEditorType = 'text'
      break
    case 'INTEGER':
      field.inputType = 'number'
      break
    case 'DOUBLE':
      field.inputType = 'number'
      break
    case 'DATE':
      field.inputType = 'date'
      break
    case 'INSTANT':
      field.inputType = 'datetime-local'
      break
  }
  return field
}

const CustomFieldSelector = (props: FieldSelectorProps) => {
  const options = props.options.map(option => {
    return { label: option.label, value: option.label }
  })

  return <div className="d w-100">
    <Select
      options={options}
      value={{ label: props.value || '', value: props.value || '' }}
      onChange={newVal => {
        if (newVal?.label != props.value) {
          props.handleOnChange(newVal?.label || '')
        }
      }}
    />
  </div>
}

export default SearchQueryBuilder
