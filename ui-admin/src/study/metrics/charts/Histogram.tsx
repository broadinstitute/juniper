import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 * Shows a plot for a specified metric.  Handles fetching the raw metrics from the server, transforming them to
 * plotly traces, and then rendering a graph
 */

/**
 *
 */
export default function Histogram({ metricData }: {
    metricData?: BasicMetricDatum[]
}) {
  const histogramData = [{
    x: metricData ? metricData.map(x => x.subcategory) : [],
    type: 'histogram',
    marker: {
      line: {
        width: 1
      }
    }
  }]

  return <>
    { (metricData || []).length > 0 ?
      <Plot
        config={{ responsive: true }}
        className="w-100"
        // eslint-disable-next-line
        data={histogramData as any ?? []}
        layout={{
          xaxis: { title: 'Value' },
          yaxis: { title: 'Count' },
          autosize: false
        }}
      /> :
      <div className="d-flex justify-content-center align-items-center h-100">
        <span className="text-muted fst-italic">No data</span>
      </div>
    }
  </>
}
