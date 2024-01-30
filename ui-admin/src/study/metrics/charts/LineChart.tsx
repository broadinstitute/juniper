import React from 'react'
import { BasicMetricDatum } from 'api/api'
import { cloneDeep } from 'lodash'
import Plot from 'react-plotly.js'

/**
 * Shows a plot for a specified metric.  Handles fetching the raw metrics from the server, transforming them to
 * plotly traces, and then rendering a graph
 */
export default function LineChart({ metricData }: {
  metricData?: BasicMetricDatum[]
}) {
  const plotlyTraces = makePlotlyTraces(metricData || [])
  const hasDataToPlot = !!plotlyTraces?.length && plotlyTraces[0].x.length

  return <>
    { hasDataToPlot ?
      <Plot
        config={{ responsive: true }}
        className="w-100"
        // eslint-disable-next-line
        data={plotlyTraces as any ?? []}
        layout={{
          autosize: false,
          yaxis: { rangemode: 'tozero', autorange: true },
          xaxis: { range: undefined } //undefined defaults to autorange
        }}
      /> :
      <div className="d-flex justify-content-center align-items-center h-100">
        <span className="text-muted fst-italic">No data</span>
      </div>
    }
  </>
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
