import React, {
  useCallback,
  useMemo,
  useState
} from 'react'
import {
  Field,
  FieldSelectorProps,
  formatQuery,
  OperatorSelectorProps,
  QueryBuilder,
  RuleGroupType,
  RuleGroupTypeAny,
  ValueEditor,
  ValueEditorProps
} from 'react-querybuilder'
import { ruleProcessorEnrolleeSearchExpression } from '../util/formatQueryBuilderAsSearchExp'
import { useLoadingEffect } from '../api/api-utils'
import Api, {
  EnrolleeSearchExpressionResult,
  SearchValueTypeDefinition
} from '../api/api'
import { StudyEnvContextT } from '../study/StudyEnvironmentRouter'
import Select from 'react-select'
import LoadingSpinner from '../util/LoadingSpinner'
import {
  debounce,
  isEmpty,
  keys
} from 'lodash'
import { parseExpression } from '../util/searchExpressionParser'
import { toReactQueryBuilderState } from '../util/searchExpressionUtils'
import 'react-querybuilder/dist/query-builder.scss'
import {
  DocsKey,
  ZendeskLink
} from '../util/zendeskUtils'
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Creatable from 'react-select/creatable'

/**
 * Frontend for building an enrollee search expression; can either
 * use react-querybuilder for a more user-friendly experience, or
 * a simple text area to directly input a search expression.
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
  const [advancedMode, setAdvancedMode] = useState(false)

  const parseSearchExpError = useMemo(() => {
    try {
      if (isEmpty(searchExpression)) {
        return undefined
      }
      parseExpression(searchExpression)
    } catch (e) {
      return e as Error
    }
  }, [searchExpression])


  const [searchResults, setSearchResults] = useState<EnrolleeSearchExpressionResult[]>([])

  const {
    isLoading: isLoadingSearchResults
  } = useLoadingEffect(async () => {
    try {
      const newResults = await Api.executeSearchExpression(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        searchExpression)

      setSearchResults(newResults)
    } catch (_) {
      setSearchResults([])
    }
  }, [searchExpression])

  return <div>
    <div className="mb-2 d-flex flex-row align-items-center justify-content-between">
      <div>
        Create a <ZendeskLink doc={DocsKey.SEARCH_EXPRESSIONS}>
          <FontAwesomeIcon icon={faInfoCircle} className={'me-1'}/>
        search expression
        </ZendeskLink> that filters to enrollees that meet a specific criteria.
      </div>
      <button
        className="btn btn-link"
        onClick={() => setAdvancedMode(!advancedMode)}>
        {advancedMode ? '(switch to basic view)' : '(switch to advanced view)'}
      </button>
    </div>
    {parseSearchExpError && <div className="alert alert-danger mb-2">
      {parseSearchExpError.message}
    </div>}
    <div className="mb-2">
      {
        advancedMode
          ? <AdvancedQueryBuilder
            onSearchExpressionChange={onSearchExpressionChange}
            searchExpression={searchExpression}/>
          : <BasicQueryBuilder
            studyEnvContext={studyEnvContext}
            onSearchExpressionChange={onSearchExpressionChange}
            searchExpression={searchExpression}
          />
      }
    </div>

    <LoadingSpinner isLoading={isLoadingSearchResults}>
      <span>
        {searchResults.length} matching enrollee{searchResults.length !== 1 ? 's' : ''}
      </span>
    </LoadingSpinner>
  </div>
}

/**
 * Allows the user to directly input a search expression.
 */
const AdvancedQueryBuilder = ({
  onSearchExpressionChange,
  searchExpression
}: {
  onSearchExpressionChange: (searchExpression: string) => void,
  searchExpression: string
}) => {
  const [localExpression, setLocalExpression] = useState(searchExpression)

  const debouncedSetSearchExpression = useCallback(debounce(val => {
    if (val === searchExpression) {
      return
    }
    onSearchExpressionChange(val)
  }, 500), [])

  return <div className="">
    <textarea
      className="form-control w-100"
      aria-label={'Search expression'}
      value={localExpression}
      onChange={e => {
        setLocalExpression(e.target.value)
        debouncedSetSearchExpression(e.target.value)
      }}
    />
  </div>
}

/**
 * Uses react-querybuilder to build a search expression.
 */
const BasicQueryBuilder = ({
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


  const [facets, setFacets] = React.useState<{ facet: string, typeDef: SearchValueTypeDefinition }[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const facets = await Api.getExpressionSearchFacets(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName)
    const facetArr = keys(facets).map(facet => {
      return {
        facet,
        typeDef: facets[facet]
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

    onSearchExpressionChange(enrolleeSearchExpression)
  }

  return <LoadingSpinner isLoading={isLoading}>
    <div>
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
        fields={useMemo(() => facets.map(facet => facetToReactQueryField(facet.facet, facet.typeDef)), [facets])}
        controlElements={{
          fieldSelector: CustomFieldSelector,
          valueEditor: CustomValueEditor,
          operatorSelector: OperatorSelector
        }}
        operators={operators}
        query={query || { combinator: 'and', rules: [] }}
        onQueryChange={q => updateQuery(q)}/>
    </div>
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

// Converts our facet type to a react-querybuilder field definition. Works in
// tandem with CustomFieldSelector and CustomValueEditor to render our custom
// query builder.
const facetToReactQueryField = (facet: string, typeDef: SearchValueTypeDefinition): Field => {
  const field: Field = {
    name: facet,
    label: facet
  }
  if (!isEmpty(typeDef.choices)) {
    field.valueEditorType = 'select'
    field.values = typeDef.choices?.map(choice => {
      return { name: choice.stableId, label: choice.text }
    })

    return field
  }

  switch (typeDef.type) {
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

  return <div className="w-100">
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

const CustomValueEditor = (props: ValueEditorProps) => {
  if (props.fieldData.valueEditorType === 'select') {
    const options = props.fieldData.values?.map(option => {
      return { label: option.label, value: (option as { name: string }).name || option.label }
    }) || []

    return (
      <div className="w-100">
        {/*
          * Creatable in case there is an 'other' value or a value we're missing. No need for multiselect,
          * as 'or'ing values can be accomplished with a group.
          */}
        <Creatable
          value={options.find(o => o.value === props.value) || { label: props.value, value: props.value }}
          onChange={v => props.handleOnChange(v?.value)}
          options={options}
        />
      </div>

    )
  }
  return <ValueEditor {...props} />
}

const OperatorSelector = (props: OperatorSelectorProps) => {
  const options = props.options.map(op => {
    return { label: op.label, value: op.label }
  })

  return <div className="w-50">
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
