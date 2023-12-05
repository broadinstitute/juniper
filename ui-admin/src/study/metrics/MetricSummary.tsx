import React from 'react'
import { getDateRangeFromMode, LabeledDateRangeMode } from './MetricGraph'
import { BasicMetricDatum } from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons'

/**
 *
 */
export default function MetricSummary({ metrics, dateRangeMode }: {
  metrics: BasicMetricDatum[], dateRangeMode: LabeledDateRangeMode
}) {
  const getFilteredMetrics = () => {
    const dateRange = getDateRangeFromMode({ dateRangeMode })
    if (dateRangeMode.mode === 'ALL_TIME' || !dateRange) { return metrics }

    return metrics.filter(metric => {
      return metric.time >= dateRange.startDate / 1000 && metric.time <= dateRange.endDate / 1000
    })
  }

  return <div className="container-fluid">
    <h4 className="my-3 align-center">Summary</h4>
    <div className="row">
      { dateRangeMode.mode !== 'ALL_TIME' && <MetricSummaryCard title={dateRangeMode.label} isTrend={true}
        value={getFilteredMetrics().length}/> }
      <MetricSummaryCard title="Cumulative" value={metrics.length}/>
    </div>
  </div>
}

/**
 * Returns a card that shows a metric summary.
 */
export const MetricSummaryCard = ({ title, value, isTrend }: { title: string, value: number, isTrend?: boolean }) => {
  return <div className="card mb-2">
    <div className="card-body">
      <h5 className="card-title">{title}</h5>
      <p className="card-text text-muted">
        {isTrend && value !== 0 ? (
          <>
            <FontAwesomeIcon icon={value >= 0 ? faCaretUp : faCaretDown} color={value >= 0 ? 'green' : 'red'} />
            <span className="px-1">{value}</span>
          </>
        ) : (
          <span>{isTrend ? 'No change' : value}</span>
        )}
      </p>
    </div>
  </div>
}
