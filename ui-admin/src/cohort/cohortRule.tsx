import React, { useState } from 'react'
import { Field, QueryBuilder, RuleGroupType } from 'react-querybuilder/dist/cjs/react-querybuilder.cjs.development'

const BUILT_IN_FIELDS: Field[] = [
  { name: 'profile.age', label: 'Age', operators: [{ name: '>', label: '>' }, { name: '<=', label: '<=' }] },
  {
    name: 'profile.sexAtBirth', label: 'Sex at birth', valueEditorType: 'select',
    operators: [{ name: '=', label: '=' }, { name: '!=', label: '!=' }],
    values: [{ name: 'M', label: 'Male' }, { name: 'F', label: 'Female' }]
  },
  {
    name: 'enrollee.subject', label: 'Is primary subject', valueEditorType: 'select',
    operators: [{ name: '=', label: 'is' }],
    values: [{ name: 'true', label: 'True' }, { name: 'false', label: 'False' }]
  }
]

/**
 *
 */
export function useRuleDesigner(rule: string | undefined, setRule: (rule: string | undefined) => void) {
  const [ruleQuery, setRuleQuery] = useState<RuleGroupType>()

  // logic goes here to transform strings <-> RuleGroupType, some form of json parsing probably

  const ruleDesigner = <QueryBuilder
    fields={BUILT_IN_FIELDS}
    query={ruleQuery}
    onQueryChange={q => setRuleQuery(q)}
    controlClassnames={{ queryBuilder: 'queryBuilder-branches' }}
  />
  return { ruleDesigner, ruleQuery, setRuleQuery }
}
