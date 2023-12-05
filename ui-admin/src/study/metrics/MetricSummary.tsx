import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { MetricInfo } from './StudyEnvMetricsView'
import { LabeledDateRangeMode } from './MetricGraph'
import { BasicMetricDatum } from 'api/api'
import { dateMinusDays } from 'util/timeUtils'


/**
 *
 */
export default function MetricSummary({ metrics, metricInfo, labeledDateRangeMode }: {
  metrics: BasicMetricDatum[], metricInfo: MetricInfo,
  labeledDateRangeMode: LabeledDateRangeMode
}) {

  function getUnixDate({ labeledDateRangeMode }: {
    labeledDateRangeMode: LabeledDateRangeMode
  }) {
    const currentDate = new Date()
    switch (labeledDateRangeMode.value) {
      case 'ALL_TIME':
        return undefined
      case 'LAST_MONTH':
        return [dateMinusDays(currentDate, 30).getTime() / 1000, currentDate.getTime() / 1000]
      case 'LAST_WEEK':
        return [dateMinusDays(currentDate, 7).getTime() / 1000, currentDate.getTime() / 1000]
      case 'LAST_24_HOURS':
        return [dateMinusDays(currentDate, 1).getTime() / 1000, currentDate.getTime() / 1000]
    }
  }

  const startDate = getUnixDate({ labeledDateRangeMode })[0]
  const endDate = getUnixDate({ labeledDateRangeMode })[1]

  const filteredMetrics = metrics.filter(metric => {
    if (!startDate || !endDate) {
      return true
    }
    return metric.time >= startDate && metric.time <= endDate
  })

  return <div className="container-fluid">
    <h4 className="mt-3 align-center">Summary</h4>
    <div className="row">
      <MetricSummaryCard title={labeledDateRangeMode.label}
        value={`${filteredMetrics.length >= 0 ? '+' : ''}${filteredMetrics.length.toString()}`}/>
      <MetricSummaryCard title="Cumulative" value={metrics.length.toString()}/>
    </div>
  </div>
}

export const MetricSummaryCard = ({ title, value }: {
  title: string, value: string
}) => {
  return <div className="card mb-2">
    <div className="card-body">
      <h5 className="card-title">{title}</h5>
      <p className="card-text">{value}</p>
    </div>
  </div>
}
