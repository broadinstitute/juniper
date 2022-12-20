import React from 'react'
import Select from 'react-select'
import { useNavigate } from 'react-router-dom'
import { StudyEnvironment, Study } from 'api/api'

type EnvOption = {label: string, value: string}

/** dropdown for toggling the study environment */
function EnvironmentSelector({ portalShortcode, study, currentEnv }:
                               {portalShortcode: string, study: Study, currentEnv: StudyEnvironment | undefined}) {
  const envOptions = study.studyEnvironments.map(env => {
    return { label: env.environmentName.toLowerCase(), value: env.environmentName }
  })
  let value = null
  if (currentEnv) {
    value = { label: currentEnv.environmentName.toLowerCase(), value: currentEnv.environmentName }
  }

  const navigate = useNavigate()

  /** route to the selected environment */
  function selectEnv(option: EnvOption | null) {
    const nextEnv: string = option ? option.value : ''
    navigate(`/${portalShortcode}/studies/${study.shortcode}/env/${nextEnv.toLowerCase()}`)
  }

  return <Select className="react-select" options={envOptions} value={value} onChange={selectEnv} />
}

export default EnvironmentSelector


