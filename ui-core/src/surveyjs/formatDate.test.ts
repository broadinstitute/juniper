import { formatDate } from 'src/surveyjs/formatDate'

describe('formatDate', () => {
  it('handles dates', () => {
    const date = new Date()
    const result = formatDate([date])
    expect(result).toBe(date.toLocaleDateString())
  })
  it('handles strings', () => {
    const result = formatDate(['2023-06-21T15:59:46.164Z'])
    expect(result).toBe('6/21/2023')
  })
  it('handles empty string', () => {
    const result = formatDate([''])
    expect(result).toBe(null)
  })
  it('handles empty params', () => {
    const result = formatDate([])
    expect(result).toBe(null)
  })
})
