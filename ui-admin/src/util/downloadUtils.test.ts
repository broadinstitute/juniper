import { escapeCsvValue } from './downloadUtils'

describe('downloadUtils escapeCsvValue', () => {
  it('handles boring strings', () => {
    expect(escapeCsvValue('foobar')).toEqual('foobar')
    expect(escapeCsvValue('foobar')).toEqual('foobar')
    expect(escapeCsvValue(' space test ')).toEqual(' space test ')
  })

  it('quotes strings with special characters', () => {
    expect(escapeCsvValue('line1\nline2')).toEqual('"line1\nline2"')
    expect(escapeCsvValue('1,2')).toEqual('"1,2"')
    expect(escapeCsvValue('foo "quote" bar')).toEqual('"foo ""quote"" bar"')
  })
})
