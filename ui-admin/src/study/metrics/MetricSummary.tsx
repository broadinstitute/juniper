import React from 'react'
import { LabeledDateRangeMode, makePlotlyXAxisRange } from './MetricGraph'
import { BasicMetricDatum } from 'api/api'

/**
 *
 */
export default function MetricSummary({ metrics, dateRangeMode }: {
  metrics: BasicMetricDatum[], dateRangeMode: LabeledDateRangeMode
}) {
  const getFilteredMetrics = () => {
    if (dateRangeMode.mode === 'ALL_TIME') { return metrics }
    const startDate = makePlotlyXAxisRange({ dateRangeMode }).startDate! / 1000
    const endDate = makePlotlyXAxisRange({ dateRangeMode }).endDate! / 1000

    return metrics.filter(metric => {
      if (startDate && endDate) {
        return metric.time >= startDate && metric.time <= endDate
      }
      return true
    })
  }

  return <div className="container-fluid">
    <h4 className="my-3 align-center">Summary</h4>
    <div className="row">
      { dateRangeMode.mode !== 'ALL_TIME' && <MetricSummaryCard title={dateRangeMode.label}
        value={`${getFilteredMetrics().length < 0 ? '' : '+'}${getFilteredMetrics().length.toString()}`}/> }
      <MetricSummaryCard title="Cumulative" value={metrics.length.toString()}/>
    </div>
  </div>
}

/**
 *
 */
export const MetricSummaryCard = ({ title, value }: { title: string, value: string }) => {
  return <div className="card mb-2">
    <div className="card-body">
      <h5 className="card-title">{title}</h5>
      <p className="card-text">{value}</p>
    </div>
  </div>
}
