import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 * Returns a Histogram chart for a specified metric.
 */
export default function Histogram({ data }: {
  data: BasicMetricDatum[]
}) {
  const trace = [{
    x: data.map(x => x.subcategory),
    type: 'histogram',
    marker: {
      line: {
        width: 1
      }
    }
  }]

  const layout = {
    xaxis: { title: 'Value' },
    yaxis: { title: 'Count', tickformat: 'd', dtick: 1 },
    autosize: false
  }

  return <>
    { data.length > 0 ?
      <Plot
        config={{ responsive: true }}
        className="w-100"
        // eslint-disable-next-line
        data={trace as any ?? []}
        layout={layout}
      /> :
      <div className="d-flex justify-content-center align-items-center h-100">
        <span className="text-muted fst-italic">No data</span>
      </div>
    }
  </>
}
