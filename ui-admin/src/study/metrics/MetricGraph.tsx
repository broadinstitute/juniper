import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Api, { BasicMetricDatum } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { cloneDeep } from 'lodash'
import { MetricInfo } from './StudyEnvMetricsView'
import Plot from 'react-plotly.js'
import {dateMinusDays, instantToDefaultString} from 'util/timeUtils'
import { useLoadingEffect } from 'api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClipboard } from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import InfoPopup from 'components/forms/InfoPopup'

const EXPORT_DELIMITER = '\t'

export type DateRangeMode = 'ALL_TIME' | 'LAST_MONTH' | 'LAST_WEEK' | 'LAST_24_HOURS'

/**
 * Shows a plot for a specified metric.  Handles fetching the raw metrics from the server, transforming them to
 * plotly traces, and then rendering a graph
 */
export default function MetricGraph({ studyEnvContext, metricInfo, dateRangeMode }: {
  studyEnvContext: StudyEnvContextT, metricInfo: MetricInfo, dateRangeMode: DateRangeMode
}) {
  const [metricData, setMetricData] = useState<BasicMetricDatum[] | null>(null)
  const [plotlyTraces, setPlotlyTraces] = useState<PlotlyTimeTrace[] | null>(null)

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchMetric(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, metricInfo.name)
    setPlotlyTraces(makePlotlyTraces(result))
    setMetricData(result)
  }, [metricInfo.name, studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  const copyRawData = () => {
    if (!metricData) {
      return
    }
    let dataString = `${['name', 'subcategory', 'time'].join(EXPORT_DELIMITER)  }\n`
    dataString += metricData.map(metricDatum =>
      [metricInfo.name, metricDatum.subcategory, instantToDefaultString(metricDatum.time)].join(EXPORT_DELIMITER)
    ).join('\n')
    navigator.clipboard.writeText(dataString)
  }

  const hasDataToPlot = !!plotlyTraces?.length && plotlyTraces[0].x.length

  return <div className="container p-2">
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-baseline">
        <h2 className="h5">{metricInfo.title}</h2>
        { metricInfo.tooltip && <InfoPopup content={metricInfo.tooltip} /> }
        <Button
          variant="secondary"
          tooltip={'Copy raw data to clipboard'}
          onClick={copyRawData}
        >
          <FontAwesomeIcon icon={faClipboard} className={'fa-regular'} />
        </Button>
      </div>
      <div className="container border">
        { hasDataToPlot ? <Plot
          // eslint-disable-next-line
          data={plotlyTraces as any ?? []}
          layout={{
            autosize: true, yaxis: { rangemode: 'tozero', autorange: true },
            xaxis: { range: makePlotlyXAxisRange({ dateRangeMode }) }
          }}
        /> : <div className="my-5"><span className="text-muted fst-italic">No data</span></div>}
      </div>
    </LoadingSpinner>
  </div>
}

type PlotlyTimeTrace = {
  x: Date[],
  y: number[],
  type: string,
  name: string,
  xbins: { size: number},
  yOffset: number
}

const emptyTrace: PlotlyTimeTrace = {
  x: [],
  y: [],
  type: 'line',
  xbins: { size: 86400000 }, // bin is one day -- eventually this should be changeable via dropdown
  name: 'trace',
  yOffset: 0
}

export const JITTER_AMOUNT = 0.05
/**
 * transform the list of datapoints into plotly traces, one trace per subcategory.  Other than using a for-loop,
 * this is not performance optimized since we're assuming the size of the metrics[] in the near term will be <10000
 * */
export const makePlotlyTraces = (metrics: BasicMetricDatum[]): PlotlyTimeTrace[] => {
  const tracesByName: Record<string, PlotlyTimeTrace> = {}
  for (let i = 0; i < metrics.length; i++) {
    const metric = metrics[i]
    // if there's no subcategory, assume we're plotting a single-trace graph
    const subcategory = metric.subcategory ?? '_all'
    let trace = tracesByName[subcategory]
    if (!trace) {
      trace = {
        ...cloneDeep(emptyTrace),
        name: subcategory,
        yOffset: Object.values(tracesByName).length * JITTER_AMOUNT
      }
      tracesByName[subcategory] = trace
    }
    trace.x.push(new Date(metric.time * 1000))
    // since this is a cumulative graph, the y value is just the number of points so far, plus a jitter for visibility
    trace.y.push(trace.x.length + trace.yOffset)
  }
  return Object.values(tracesByName)
}

export function makePlotlyXAxisRange({ dateRangeMode }: {
  dateRangeMode: DateRangeMode
}) {
  const currentDate = new Date()
  switch (dateRangeMode) {
    case 'ALL_TIME':
      return undefined
    case 'LAST_MONTH':
      return [dateMinusDays(currentDate, 30).toISOString(), currentDate.toISOString()]
    case 'LAST_WEEK':
      return [dateMinusDays(currentDate, 7).toISOString(), currentDate.toISOString()]
    case 'LAST_24_HOURS':
      return [dateMinusDays(currentDate, 1).toISOString(), currentDate.toISOString()]
  }
}
