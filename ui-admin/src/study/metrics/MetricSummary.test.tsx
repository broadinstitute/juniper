import React from 'react'
import MetricSummary from './MetricSummary'
import { render, screen } from '@testing-library/react'
import { dateMinusDays } from 'util/timeUtils'
import { LabeledDateRangeMode, modeToDateRange } from './metricUtils'

const currentDate = new Date()

const MOCK_METRIC_DATA = [
  { name: 'foo', time: currentDate.getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 5).getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 15).getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 31).getTime() / 1000 }
]

test('MetricSummary should display No Change trend text for an empty metric set', () => {
  const dateRangeMode = { label: 'Last Month', mode: 'LAST_MONTH' } as LabeledDateRangeMode
  const lastMonthDateRange = modeToDateRange({ dateRangeMode })
  render(<MetricSummary dateRange={lastMonthDateRange} metricData={[]} dateRangeMode={dateRangeMode} />)
  expect(screen.getByText('Last Month')).toBeInTheDocument()
  expect(screen.getByText('No change')).toBeInTheDocument()
})

test('MetricSummary should display all-time summary data', () => {
  const dateRangeMode = { label: 'All Time', mode: 'ALL_TIME' } as LabeledDateRangeMode
  const allTimeDateRange = modeToDateRange({ dateRangeMode })
  render(<MetricSummary dateRange={allTimeDateRange} metricData={MOCK_METRIC_DATA} dateRangeMode={dateRangeMode} />)
  expect(screen.queryByText('All Time')).not.toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last month', () => {
  const dateRangeMode = { label: 'Last Month', mode: 'LAST_MONTH' } as LabeledDateRangeMode
  const lastMonthDateRange = modeToDateRange({ dateRangeMode })
  render(<MetricSummary dateRange={lastMonthDateRange} metricData={MOCK_METRIC_DATA} dateRangeMode={dateRangeMode} />)
  expect(screen.getByText('Last Month')).toBeInTheDocument()
  expect(screen.getByText('3')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last week', () => {
  const dateRangeMode = { label: 'Last Week', mode: 'LAST_WEEK' } as LabeledDateRangeMode
  const lastWeekDateRange = modeToDateRange({ dateRangeMode })
  render(<MetricSummary dateRange={lastWeekDateRange} metricData={MOCK_METRIC_DATA} dateRangeMode={dateRangeMode} />)
  expect(screen.getByText('Last Week')).toBeInTheDocument()
  expect(screen.getByText('2')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last 24 hours', () => {
  const dateRangeMode = { label: 'Last 24 Hours', mode: 'LAST_24_HOURS' } as LabeledDateRangeMode
  const last24HoursDateRange = modeToDateRange({ dateRangeMode })
  render(<MetricSummary dateRange={last24HoursDateRange} metricData={MOCK_METRIC_DATA} dateRangeMode={dateRangeMode} />)
  expect(screen.getByText('Last 24 Hours')).toBeInTheDocument()
  expect(screen.getByText('1')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})
