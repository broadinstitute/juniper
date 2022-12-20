import React from 'react'
import Select, { ActionMeta } from 'react-select'
import { useNavigate } from 'react-router-dom'
import { StudyEnvironment, Study } from 'api/api'

type EnvOption = {label: string, value: string}

function EnvironmentSelector({ study, currentEnv }: {study: Study, currentEnv: StudyEnvironment | undefined}) {
  const envOptions = study.studyEnvironments.map(env => {
    return { label: env.environmentName.toLowerCase(), value: env.environmentName }
  })
  let value = null
  if (currentEnv) {
    value = { label: currentEnv.environmentName.toLowerCase(), value: currentEnv.environmentName }
  }

  const navigate = useNavigate()

  function selectEnv(option: EnvOption | null, actionMeta: ActionMeta<EnvOption>) {
    const nextEnv: string = option ? option.value : ''
    navigate(`/${study.shortcode}/env/${nextEnv.toLowerCase()}`)
  }

  return <Select className="react-select" options={envOptions} value={value} onChange={selectEnv} />
}

export default EnvironmentSelector


