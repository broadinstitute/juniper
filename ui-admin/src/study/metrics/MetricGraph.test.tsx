import { JITTER_AMOUNT, makePlotlyTraces } from './MetricGraph'

test('makes single trace for non categorized data', () => {
  const traces = makePlotlyTraces([
    { name: 'foo', time: 1683717253.544972 },
    { name: 'foo', time: 1683737253.544972 },
    { name: 'foo', time: 1683757253.544972 },
    { name: 'foo', time: 1683787253.544972 }
  ])
  expect(traces).toHaveLength(1)
  expect(traces[0].x).toHaveLength(4)
  expect(traces[0].y).toStrictEqual([1, 2, 3, 4])
})

test('makes multiple traces for categorized data', () => {
  const traces = makePlotlyTraces([
    { name: 'foo', subcategory: 'a', time: 1683717253.544972 },
    { name: 'foo', subcategory: 'b', time: 1683737253.544972 },
    { name: 'foo', subcategory: 'a', time: 1683757253.544972 },
    { name: 'foo', subcategory: 'a', time: 1683787253.544972 }
  ])
  expect(traces).toHaveLength(2)
  const traceA = traces.find(trace => trace.name === 'a')
  expect(traceA?.y).toStrictEqual([1, 2, 3])

  const traceB = traces.find(trace => trace.name === 'b')
  expect(traceB?.y).toStrictEqual([1 + JITTER_AMOUNT])
})
