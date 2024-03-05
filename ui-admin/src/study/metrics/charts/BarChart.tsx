import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 *
 */
export default function BarChart({ data }: {
    data: BasicMetricDatum[]
}) {
  function groupByCount(data: BasicMetricDatum[]): { y: number[], x: string[] } {
    const counts = new Map<string, number>()

    data.forEach(datum => {
      if (datum.subcategory != null) {
        counts.set(datum.subcategory, (counts.get(datum.subcategory) || 0) + 1)
      }
    })

    return {
      y: Array.from(counts.values()),
      x: Array.from(counts.keys())
    }
  }

  const trace = [{
    x: groupByCount(data).x,
    y: groupByCount(data).y,
    type: 'bar'
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
