import React from 'react'
import MetricSummary from './MetricSummary'
import { render, screen } from '@testing-library/react'
import { dateMinusDays } from '../../util/timeUtils'

const currentDate = new Date()

const MOCK_METRIC_DATA = [
  { name: 'foo', time: currentDate.getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 5).getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 15).getTime() / 1000 },
  { name: 'foo', time: dateMinusDays(currentDate, 31).getTime() / 1000 }
]

test('MetricSummary should display No Change trend text for an empty metric set', () => {
  render(<MetricSummary
    metricData={[]} dateRangeMode={{ label: 'Last Month', mode: 'LAST_MONTH' }} />)
  expect(screen.getByText('Last Month')).toBeInTheDocument()
  expect(screen.getByText('No change')).toBeInTheDocument()
})

test('MetricSummary should display all-time summary data', () => {
  render(<MetricSummary
    metricData={MOCK_METRIC_DATA} dateRangeMode={{ label: 'All Time', mode: 'ALL_TIME' }} />)
  expect(screen.queryByText('All Time')).not.toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last month', () => {
  render(<MetricSummary
    metricData={MOCK_METRIC_DATA} dateRangeMode={{ label: 'Last Month', mode: 'LAST_MONTH' }} />)
  expect(screen.getByText('Last Month')).toBeInTheDocument()
  expect(screen.getByText('3')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last week', () => {
  render(<MetricSummary
    metricData={MOCK_METRIC_DATA} dateRangeMode={{ label: 'Last Week', mode: 'LAST_WEEK' }} />)
  expect(screen.getByText('Last Week')).toBeInTheDocument()
  expect(screen.getByText('2')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})

test('MetricSummary should display summary data for the last 24 hours', () => {
  render(<MetricSummary
    metricData={MOCK_METRIC_DATA} dateRangeMode={{ label: 'Last 24 Hours', mode: 'LAST_24_HOURS' }} />)
  expect(screen.getByText('Last 24 Hours')).toBeInTheDocument()
  expect(screen.getByText('1')).toBeInTheDocument()
  expect(screen.getByText('Cumulative')).toBeInTheDocument()
  expect(screen.getByText('4')).toBeInTheDocument()
})
