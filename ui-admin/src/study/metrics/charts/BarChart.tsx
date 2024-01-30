import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 * Shows a plot for a specified metric.  Handles fetching the raw metrics from the server, transforming them to
 * plotly traces, and then rendering a graph
 */

export type BarChartData = {
  y: number[],
  x: string[]
}

/**
 *
 */
export default function BarChart({ metricData }: {
  metricData?: BasicMetricDatum[]
}) {
  function groupByCount(input: BasicMetricDatum[]): BarChartData {
    const counts = input.reduce((acc, curr) => {
      // @ts-ignore
      acc[curr.subcategory] = (acc[curr.subcategory] || 0) + 1
      return acc
    }, {})

    return {
      y: Object.values(counts),
      x: Object.keys(counts)
    }
  }

  const barChartData = [{
    x: groupByCount(metricData || []).x,
    y: groupByCount(metricData || []).y,
    type: 'bar'
  }]

  return <>
    { groupByCount(metricData || []).x.length > 0 ?
      <Plot
        config={{ responsive: true }}
        className="w-100"
        // eslint-disable-next-line
        data={barChartData as any ?? []}
        layout={{
          autosize: false
        }}
      /> :
      <div className="d-flex justify-content-center align-items-center h-100">
        <span className="text-muted fst-italic">No data</span>
      </div>
    }
  </>
}
