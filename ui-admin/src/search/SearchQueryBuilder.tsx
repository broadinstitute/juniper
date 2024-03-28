import React from 'react'
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

/**
 *
 */
export const useSearchExpressionQueryBuilder = ({ studyEnvContext } : {studyEnvContext: StudyEnvContextT}) => {
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

  console.log(participantFields)
  useLoadingEffect(async () => {
    const response = await Api.exportEnrollees(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      'sandbox', { fileFormat: 'JSON', limit: 0 })
    const result = await response.json()
    setParticipantFields(result)
  }, [], 'Failed to load cohort criteria options')


  const enrolleeSearchExpression = query.rules.length > 0 ? formatQuery(query, {
    format: 'spel', // not the actual format, but for some reason formatquery requires you specify one of their formats
    ruleProcessor: ruleProcessorEnrolleeSearchExpression
  }) : ''

  const EnrolleeSearchQueryBuilder = <QueryBuilder
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
      fieldSelector: (props: FieldSelectorProps) => {
        const options = props.options.map(option => {
          return { label: option.label, value: option.label }
        })
        return <div className="w-100">
          <Select
            options={options}
            value={{ label: props.value, value: props.value }}
            onChange={newVal => props.handleOnChange(newVal?.label || '')}/>
        </div>
      }
    }}
    operators={operators}
    query={query}
    onQueryChange={q => setQuery(q)}/>

  return {
    query,
    setQuery,
    enrolleeSearchExpression,
    EnrolleeSearchQueryBuilder
  }
}
