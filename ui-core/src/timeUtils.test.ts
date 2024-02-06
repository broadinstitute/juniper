import { dateToDefaultString } from './timeUtils'

describe('timeUtils dateToDefaultString', () => {
  it('handles realistic dates', () => {
    expect(dateToDefaultString([1984, 3, 14])).toEqual('3/14/1984')
    expect(dateToDefaultString([1984, 12, 14])).toEqual('12/14/1984')
  })

  it('handles nullish values', () => {
    expect(dateToDefaultString([])).toEqual('')
    expect(dateToDefaultString(undefined)).toEqual('')
  })
})
