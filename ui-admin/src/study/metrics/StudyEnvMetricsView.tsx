import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import MetricGraph from './MetricGraph'

export type MetricInfo = {
  name: string,
  title: string
}

const metricMetadata: MetricInfo[] = [
  { name: 'STUDY_ENROLLMENT', title: 'Registered (pre-reg + created account)' },
  { name: 'STUDY_ENROLLEE_CONSENTED', title: 'Completed consent' },
  { name: 'STUDY_REQUIRED_SURVEY_COMPLETION', title: 'Required survey completions' },
  { name: 'STUDY_SURVEY_COMPLETION', title: 'Total survey completions' }
]

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvMetricsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const metricsByName = metricMetadata.reduce<Record<string, MetricInfo>>((prev,
    current) => {
    prev[current.name] = current
    return prev
  }, {})
  return <div className="container row justify-content-center">
    <div className="col-md-8 p-4">
      <h1 className="h4">{studyEnvContext.study.name} Summary
        <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
      </h1>
      <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLMENT']}/>
      <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLEE_CONSENTED']}/>
      <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_REQUIRED_SURVEY_COMPLETION']}/>
      <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_SURVEY_COMPLETION']}/>
    </div>
  </div>
}
