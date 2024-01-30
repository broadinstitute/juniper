import React, { useEffect, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCalendarDays } from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import MetricView from './MetricView'
import { LabeledDateRangeMode, MetricDateRange, modeToDateRange } from './metricUtils'
import MetricDateRangeModal from './MetricDateRangeModal'
import SurveyInsightsView from './SurveyInsightsView'

export type MetricChartType = 'line' | 'pie' | 'bar'

export type MetricInfo = {
  name: string,
  title: string,
  type: MetricChartType
  tooltip?: string
}

const metricMetadata: MetricInfo[] = [
  {
    name: 'STUDY_ENROLLMENT',
    title: 'Accounts Registered',
    type: 'line',
    tooltip: 'Users who have completed the pre-registration form and created an account.'
  },
  { name: 'STUDY_ENROLLEE_CONSENTED', title: 'Consents Completed', type: 'line' },
  { name: 'STUDY_REQUIRED_SURVEY_COMPLETION', title: 'Required Surveys Completed', type: 'line' },
  { name: 'STUDY_SURVEY_COMPLETION', title: 'Total Surveys Completed', type: 'line' }
]

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvMetricsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [showDateRangePicker, setShowDateRangePicker] = useState(false)
  const [selectedDateRangeMode, setSelectedDateRangeMode] =
    useState<LabeledDateRangeMode>({ label: 'Last Month', mode: 'LAST_MONTH' })
  const [dateRange, setDateRange] = useState<MetricDateRange>()

  useEffect(() => {
    if (selectedDateRangeMode.mode !== 'CUSTOM') {
      setDateRange(modeToDateRange({ dateRangeMode: selectedDateRangeMode }))
    }
  }, [selectedDateRangeMode])

  const metricsByName = metricMetadata.reduce<Record<string, MetricInfo>>((prev,
    current) => {
    prev[current.name] = current
    return prev
  }, {})
  return <div className="container-fluid px-4 py-2 pb-5">
    { renderPageHeader('Participant Analytics') }
    <div className="d-flex align-items-center justify-content-between">
      <h4>{studyEnvContext.study.name} Summary
        <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
      </h4>
      <div className="me-2 ms-2">
        <Button onClick={() => setShowDateRangePicker(!showDateRangePicker)}
          variant="light" className="border mb-1">
          <FontAwesomeIcon icon={faCalendarDays} className="fa-lg"/> Edit date range
        </Button>
        { showDateRangePicker &&
          <MetricDateRangeModal
            onDismiss={() => setShowDateRangePicker(false)}
            setDateRange={setDateRange}
            dateRange={dateRange}
            setSelectedDateRangeMode={setSelectedDateRangeMode}
            selectedDateRangeMode={selectedDateRangeMode}
          /> }
      </div>
    </div>
    <div className="row my-4 w-75">
      { metricMetadata.map(metric => {
        return <MetricView key={metric.name}
          studyEnvContext={studyEnvContext}
          metricInfo={metricsByName[metric.name]}
          dateRange={dateRange}
          dateRangeMode={selectedDateRangeMode}/>
      })}
      <SurveyInsightsView studyEnvContext={studyEnvContext}/>
    </div>
  </div>
}
