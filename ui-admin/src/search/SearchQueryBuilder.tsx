import React, { useEffect } from 'react'
import { Field, FieldSelectorProps, formatQuery, QueryBuilder, RuleGroupType } from 'react-querybuilder'
import { ruleProcessorEnrolleeSearchExpression } from '../util/formatQueryBuilderAsSearchExp'
import { useLoadingEffect } from '../api/api-utils'
import Api, { SearchValueType } from '../api/api'
import { StudyEnvContextT } from '../study/StudyEnvironmentRouter'
import Select from 'react-select'
import LoadingSpinner from '../util/LoadingSpinner'
import { keys } from 'lodash'

const operators = [
  { name: '=', label: '=' },
  { name: '!=', label: '!=' },
  { name: '<', label: '<' },
  { name: '<=', label: '<=' },
  { name: '>', label: '>' },
  { name: '>=', label: '>=' }
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
  console.log('reload')
  return <div className="d w-100" key='test' id={'test'}>
    <Select
      key={'test2'} id={'test2'}
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


/**
 *
 */
export const SearchQueryBuilder = ({ studyEnvContext, onSearchExpressionChange }: {
  studyEnvContext: StudyEnvContextT, onSearchExpressionChange: (searchExpression: string) => void
}) => {
  const [query, setQuery] = React.useState<RuleGroupType>({
    combinator: 'and',
    rules: []
  })


  const [facets, setFacets] = React.useState<{ facet: string, type: SearchValueType }[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const facets = await Api.getSearchFacetsV2(
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


  useEffect(() => {
    const enrolleeSearchExpression = query.rules.length > 0 ? formatQuery(query, {
      format: 'spel', // not the actual format, but formatquery requires you specify one of their formats
      ruleProcessor: ruleProcessorEnrolleeSearchExpression
    }) : ''

    onSearchExpressionChange(enrolleeSearchExpression)
  }, [query])


  return <LoadingSpinner isLoading={isLoading}>
    <QueryBuilder
      controlClassnames={{
        fields: 'form-select',
        value: 'form-control',
        operators: 'form-select',
        removeRule: 'btn btn-outline-dark',
        removeGroup: 'btn btn-outline-dark',
        addRule: 'btn btn-outline-dark',
        addGroup: 'btn btn-outline-dark d-none',
        combinators: 'form-select w-25'
      }}
      fields={facets.map(facet => facetToReactQueryField(facet.facet, facet.type))}
      controlElements={{
        fieldSelector: CustomFieldSelector
      }}
      operators={operators}
      query={query}
      onQueryChange={q => setQuery(q)}/>
  </LoadingSpinner>
}


