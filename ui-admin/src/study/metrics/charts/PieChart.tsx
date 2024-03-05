import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 * Returns a Pie chart for a specified metric
 */
export default function PieChart({ data }: {
  data: BasicMetricDatum[]
}) {
  function groupByCount(data: BasicMetricDatum[]): { values: number[], labels: string[] } {
    const counts = new Map<string, number>()

    data.forEach(datum => {
      if (datum.subcategory != null) {
        counts.set(datum.subcategory, (counts.get(datum.subcategory) || 0) + 1)
      }
    })

    return {
      values: Array.from(counts.values()),
      labels: Array.from(counts.keys())
    }
  }

  const trace = [{
    values: groupByCount(data).values,
    labels: groupByCount(data).labels,
    type: 'pie',
    texttemplate: '%{percent} (%{value})'
  }]

  const layout = {
    autosize: true
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
