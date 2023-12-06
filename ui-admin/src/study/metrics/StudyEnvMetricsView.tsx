import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCalendarDays } from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import MetricView from './MetricView'
import { LabeledDateRangeMode } from './metricUtils'

export type MetricInfo = {
  name: string,
  title: string,
  tooltip?: string
}

const metricMetadata: MetricInfo[] = [
  {
    name: 'STUDY_ENROLLMENT',
    title: 'Accounts Registered',
    tooltip: 'Users who have completed the pre-registration form and created an account.'
  },
  { name: 'STUDY_ENROLLEE_CONSENTED', title: 'Consents Completed' },
  { name: 'STUDY_REQUIRED_SURVEY_COMPLETION', title: 'Required Surveys Completed' },
  { name: 'STUDY_SURVEY_COMPLETION', title: 'Total Surveys Completed' }
]

const dateRangeRadioPicker = ({ selectedDateRangeMode, onDateSelect, onDismiss } : {
  selectedDateRangeMode: LabeledDateRangeMode
  onDateSelect: (dateRangeMode: LabeledDateRangeMode) => void
  onDismiss: () => void
}) => {
  const dateRangeOptions: LabeledDateRangeMode[] = [
    { label: 'All Time', mode: 'ALL_TIME' },
    { label: 'Last Month', mode: 'LAST_MONTH' },
    { label: 'Last Week', mode: 'LAST_WEEK' },
    { label: 'Last 24 Hours', mode: 'LAST_24_HOURS' }
  ]

  return dateRangeOptions.map((dateRangeOption, index) => {
    return <div className="form-check" key={index}>
      <input className="form-check-input" type="radio" name="plotTimeRange" id={`dateRange-${index}`}
        checked={selectedDateRangeMode.mode === dateRangeOption.mode}
        onChange={() => {
          onDateSelect(dateRangeOption)
          onDismiss()
        }}/>
      <label className="form-check-label" htmlFor={`dateRange-${index}`}>
        {dateRangeOption.label}
      </label>
    </div>
  })
}

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvMetricsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [showDateRangePicker, setShowDateRangePicker] = useState(false)
  const [selectedDateRangeMode,
    setSelectedDateRangeMode] = useState<LabeledDateRangeMode>({ label: 'Last Month', mode: 'LAST_MONTH' })

  const metricsByName = metricMetadata.reduce<Record<string, MetricInfo>>((prev,
    current) => {
    prev[current.name] = current
    return prev
  }, {})
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant Analytics') }
    <div className="d-flex align-items-center justify-content-between">
      <h4>{studyEnvContext.study.name} Summary
        <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
      </h4>
      <div className="position-relative ms-auto me-2 ms-2">
        <Button onClick={() => setShowDateRangePicker(!showDateRangePicker)}
          variant="light" className="border mb-1">
          <FontAwesomeIcon icon={faCalendarDays} className="fa-lg"/> Edit date range
        </Button>
        { showDateRangePicker && <div className="position-absolute border border-gray rounded bg-white p-3"
          style={{ right: 0 }}>
          <div className="border-b border-black">
            { dateRangeRadioPicker({
              selectedDateRangeMode,
              onDateSelect: setSelectedDateRangeMode,
              onDismiss: () => setShowDateRangePicker(false)
            }) }
          </div>
        </div> }
      </div>
    </div>
    <div className="row my-2">
      <div className="mt-2">
        { metricMetadata.map(metric => {
          return <MetricView key={metric.name}
            studyEnvContext={studyEnvContext}
            metricInfo={metricsByName[metric.name]}
            dateRangeMode={selectedDateRangeMode}/>
        })}
      </div>
    </div>
  </div>
}
