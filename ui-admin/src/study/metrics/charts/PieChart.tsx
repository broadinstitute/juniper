import React from 'react'
import { BasicMetricDatum } from 'api/api'
import Plot from 'react-plotly.js'

/**
 * Shows a plot for a specified metric.  Handles fetching the raw metrics from the server, transforming them to
 * plotly traces, and then rendering a graph
 */

export type PieChartData = {
    values: number[],
    labels: string[]
}

/**
 *
 */
export default function PieChart({ metricData }: {
    metricData?: BasicMetricDatum[]
}) {
  function groupByCount(input: BasicMetricDatum[]): PieChartData {
    const counts = input.reduce((acc, curr) => {
      // @ts-ignore
      acc[curr.subcategory] = (acc[curr.subcategory] || 0) + 1
      return acc
    }, {})

    return {
      values: Object.values(counts),
      labels: Object.keys(counts)
    }
  }

  const piechartData = [{
    values: groupByCount(metricData || []).values,
    labels: groupByCount(metricData || []).labels,
    type: 'pie'
  }]

  return <>
    { groupByCount(metricData || []).values.length > 0 ?
      <Plot
        config={{ responsive: true }}
        className="w-100"
        // eslint-disable-next-line
                data={piechartData as any ?? []}
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
