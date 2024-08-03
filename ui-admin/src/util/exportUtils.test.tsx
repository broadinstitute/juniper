import { buildFilter } from 'util/exportUtils'

describe('buildFilter', () => {
  it('has expected default filter', () => {
    expect(buildFilter()).toBe('{enrollee.subject} = true and {enrollee.consented} = true')
  })
  it('includesProxiesAsRows', () => {
    expect(buildFilter({ includeProxiesAsRows: true })).toBe('{enrollee.consented} = true')
  })
  it('includesUnconsented', () => {
    expect(buildFilter({ includeUnconsented: true })).toBe('{enrollee.subject} = true')
  })
  it('includesProxiesAsRows and includesUnconsented', () => {
    expect(buildFilter({ includeProxiesAsRows: true, includeUnconsented: true })).toBe('')
  })
})
