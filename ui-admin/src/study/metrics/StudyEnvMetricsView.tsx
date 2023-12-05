import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import MetricGraph, { LabeledDateRangeMode } from './MetricGraph'
import { renderPageHeader } from 'util/pageUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCalendarDays } from '@fortawesome/free-solid-svg-icons'
import { Button } from '../../components/forms/Button'
import { dateMinusDays } from 'util/timeUtils'

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
  return <div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="allTime"
        checked={selectedDateRangeMode.value === 'ALL_TIME'}
        onChange={() => {
          onDateSelect({label: 'All Time', value: 'ALL_TIME'})
          onDismiss()
        }}/>
      <label className="form-check-label" htmlFor="allTime">
          All Time
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="lastMonth"
        checked={selectedDateRangeMode.value === 'LAST_MONTH'}
        onChange={() => {
          onDateSelect({label: 'Last Month', value: 'LAST_MONTH'})
          onDismiss()
        }}/>
      <label className="form-check-label" htmlFor="lastMonth">
          Last Month
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="lastWeek"
        checked={selectedDateRangeMode.value === 'LAST_WEEK'}
        onChange={() => {
          onDateSelect({label: 'Last Week', value: 'LAST_WEEK'})
          onDismiss()
        }}/>
      <label className="form-check-label" htmlFor="lastWeek">
        Last Week
      </label>
    </div>
    <div className="form-check">
      <input className="form-check-input" type="radio" name="plotTimeRange" id="last24Hours"
        checked={selectedDateRangeMode.value === 'LAST_24_HOURS'}
        onChange={() => {
          onDateSelect({label: 'Last 24 Hours', value: 'LAST_24_HOURS'})
          onDismiss()
        }}/>
      <label className="form-check-label" htmlFor="last24Hours">
        Last 24 Hours
      </label>
    </div>
  </div>
}

/** shows summary stats for the study.  very simple for now--this will eventually have charts and graphs */
export default function StudyEnvMetricsView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [showDateRangePicker, setShowDateRangePicker] = useState(false)
  const [selectedDateRangeMode,
    setSelectedDateRangeMode] = useState<LabeledDateRangeMode>({label: 'Last Month', value: 'LAST_MONTH'})

  const metricsByName = metricMetadata.reduce<Record<string, MetricInfo>>((prev,
    current) => {
    prev[current.name] = current
    return prev
  }, {})
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant Analytics') }
    <div className="d-flex align-items-center justify-content-between">
      <div className="d-flex">
        <h4>{studyEnvContext.study.name} Summary
          <span className="fst-italic text-muted ms-3">({studyEnvContext.currentEnv.environmentName})</span>
        </h4>
      </div>
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
    <div className="row">
      <div className="mt-2">
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLMENT']}
          labeledDateRangeMode={selectedDateRangeMode}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_ENROLLEE_CONSENTED']}
          labeledDateRangeMode={selectedDateRangeMode}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_REQUIRED_SURVEY_COMPLETION']}
          labeledDateRangeMode={selectedDateRangeMode}/>
        <MetricGraph studyEnvContext={studyEnvContext} metricInfo={metricsByName['STUDY_SURVEY_COMPLETION']}
          labeledDateRangeMode={selectedDateRangeMode}/>
      </div>
    </div>
  </div>
}
