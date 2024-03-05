import { dateMinusDays, instantToDefaultString } from '@juniper/ui-core'
import { BasicMetricDatum } from '../../api/api'
import { MetricInfo } from './StudyEnvMetricsView'

export type DateRangeMode = 'ALL_TIME' | 'LAST_MONTH' | 'LAST_WEEK' | 'LAST_24_HOURS' | 'CUSTOM'

export type LabeledDateRangeMode = {
  label: string,
  mode: DateRangeMode
}

export type MetricDateRange = {
  startDate: number,
  endDate: number
}

export const METRIC_EXPORT_DELIMITER = '\t'

/**
 * Returns a date range given a date range mode.
 */
export function modeToDateRange({ dateRangeMode }: {
  dateRangeMode: LabeledDateRangeMode
}): MetricDateRange | undefined {
  const currentDate = new Date()
  switch (dateRangeMode.mode) {
    case 'ALL_TIME':
      return undefined
    case 'LAST_MONTH':
      return { startDate: dateMinusDays(currentDate, 30).getTime(), endDate: currentDate.getTime() }
    case 'LAST_WEEK':
      return { startDate: dateMinusDays(currentDate, 7).getTime(), endDate: currentDate.getTime() }
    case 'LAST_24_HOURS':
      return { startDate: dateMinusDays(currentDate, 1).getTime(), endDate: currentDate.getTime() }
    case 'CUSTOM':
      return undefined
  }
}

/**
 * Converts a MetricDateRange to a tuple of ISO date strings, suitable for passing to Plotly
 */
export const unixToPlotlyDateRange = (dateRange: MetricDateRange): [string, string] => {
  const startDate = dateRange.startDate ? new Date(dateRange.startDate) : undefined
  const endDate = dateRange.endDate ? new Date(dateRange.endDate) : undefined
  return [startDate?.toISOString() ?? '', endDate?.toISOString() ?? '']
}

/**
 *
 */
export const copyRawData = (metricInfo: MetricInfo, metricData: BasicMetricDatum[]) => {
  if (!metricData) {
    return
  }
  let dataString = `${['name', 'subcategory', 'time'].join(METRIC_EXPORT_DELIMITER)  }\n`
  dataString += metricData.map(metricDatum =>
    [metricInfo.name, metricDatum.subcategory, instantToDefaultString(metricDatum.time)].join(METRIC_EXPORT_DELIMITER)
  ).join('\n')
  navigator.clipboard.writeText(dataString)
}
