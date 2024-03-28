import React, { useEffect } from 'react'
import { FieldSelectorProps, formatQuery, QueryBuilder, RuleGroupType } from 'react-querybuilder'
import { ruleProcessorEnrolleeSearchExpression } from '../util/formatQueryBuilderAsSearchExp'
import { useLoadingEffect } from '../api/api-utils'
import Api, { ExportData } from '../api/api'
import { StudyEnvContextT } from '../study/StudyEnvironmentRouter'
import Select from 'react-select'

const operators = [
  { name: '=', label: '=' },
  { name: '!=', label: '!=' },
  { name: '<', label: '<' },
  { name: '<=', label: '<=' },
  { name: '>', label: '>' },
  { name: '>=', label: '>=' }
]

const CustomFieldSelector = (props: FieldSelectorProps) => {
  const options = props.options.map(option => {
    return { label: option.label, value: option.label }
  })
  console.log('reload')
  return <div className="w-100">
    <Select
      // options={options}
      options={[{ label: 'profile.givenName', value: 'profile.givenName' }]}
      value={{ label: props.value || '', value: props.value || '' }}
      onChange={newVal => {
        if (newVal?.label != props.value) {
          props.handleOnChange(newVal?.label || '')
        }
      }}/>
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

  const fields = [
    { name: 'profile.givenName', label: 'profile.givenName' },
    { name: 'age', label: 'age' }
  ]

  const [participantFields, setParticipantFields] = React.useState<ExportData>()

  const surveyAnswerFields = (
    participantFields
      ?.columnKeys
      .filter(
        field => !field.startsWith('profile') && !field.startsWith('enrollee') && !field.startsWith('sample_kit')))

  useLoadingEffect(async () => {
    const response = await Api.exportEnrollees(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      'sandbox', { fileFormat: 'JSON', limit: 0 })
    const result = await response.json()
    setParticipantFields(result)
  }, [], 'Failed to load cohort criteria options')


  useEffect(() => {
    const enrolleeSearchExpression = query.rules.length > 0 ? formatQuery(query, {
      format: 'spel', // not the actual format, but formatquery requires you specify one of their formats
      ruleProcessor: ruleProcessorEnrolleeSearchExpression
    }) : ''

    onSearchExpressionChange(enrolleeSearchExpression)
  }, [query])


  return <QueryBuilder
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
    fields={fields.concat(surveyAnswerFields?.map(field => ({ name: field, label: field })) || [])}
    controlElements={{
      fieldSelector: CustomFieldSelector
    }}
    operators={operators}
    query={query}
    onQueryChange={q => setQuery(q)}/>
}


