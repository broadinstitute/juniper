import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import MetricGraph from './MetricGraph'
import { renderPageHeader } from 'util/pageUtils'

export type MetricInfo = {
  name: string,
  title: string,
  tooltip?: string
}

const metricMetadata: MetricInfo[] = [
  { name: 'STUDY_ENROLLMENT', title: 'Accounts Registered', tooltip: 'Coming soon...' },
  { name: 'STUDY_ENROLLEE_CONSENTED', title: 'Consents Completed', tooltip: 'Coming soon...' },
  { name: 'STUDY_REQUIRED_SURVEY_COMPLETION', title: 'Required Surveys Completed', tooltip: 'Coming soon...' },
  { name: 'STUDY_SURVEY_COMPLETION', title: 'Total Surveys Completed', tooltip: 'Coming soon...' }
]

const dateRangeRadioPicker = () => {
  return <div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="allTime"
        onChange={() => console.log('all time')}/>
      <label className="form-check-label" htmlFor="allTime">
          All Time
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="lastMonth"
        onChange={() => console.log('last month')}/>
      <label className="form-check-label" htmlFor="lastMonth">
          Last Month
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="lastWeek"/>
      <label className="form-check-label" htmlFor="lastWeek">
        Last Week
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="last24Hours"/>
      <label className="form-check-label" htmlFor="last24Hours">
        Last 24 Hours
      </label>
    </div>
  </div>
}

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvMetricsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [startDate, setStartDate] = React.useState<Date>()
  const [endDate, setEndDate] = React.useState<Date>(new Date())

  const metricsByName = metricMetadata.reduce<Record<string, MetricInfo>>((prev,
    current) => {
    prev[current.name] = current
    return prev
  }, {})
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant Analytics') }
    <div className="row">
      <h4>{studyEnvContext.study.name} Summary
        <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
      </h4>
      {dateRangeRadioPicker()}
      <div className="mt-2">
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLMENT']}
          startDate={startDate} endDate={endDate}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLEE_CONSENTED']}
          startDate={startDate} endDate={endDate}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_REQUIRED_SURVEY_COMPLETION']}
          startDate={startDate} endDate={endDate}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_SURVEY_COMPLETION']}
          startDate={startDate} endDate={endDate}/>
      </div>
    </div>
  </div>
}
