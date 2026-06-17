import { parseShortcodeCsv, parseShortcodes } from './SurveyBulkAssignModal'

describe('parseShortcodes', () => {
  test('trims surrounding whitespace from each shortcode', () => {
    expect(parseShortcodes('ABCDEF, GHIJKL')).toEqual(['ABCDEF', 'GHIJKL'])
  })

  test('uppercases shortcodes', () => {
    expect(parseShortcodes('abcdef,GhIjKl')).toEqual(['ABCDEF', 'GHIJKL'])
  })

  test('splits on commas and newlines', () => {
    expect(parseShortcodes('ABCDEF\nGHIJKL,MNOPQR')).toEqual(['ABCDEF', 'GHIJKL', 'MNOPQR'])
  })

  test('drops entries that are not 6 characters', () => {
    expect(parseShortcodes('ABCDEF,SHORT,TOOLONGCODE,')).toEqual(['ABCDEF'])
  })

  test('dedupes after normalizing', () => {
    expect(parseShortcodes('ABCDEF, abcdef')).toEqual(['ABCDEF'])
  })
})

describe('parseShortcodeCsv', () => {
  test('takes the first column of each row, normalized', () => {
    expect(parseShortcodeCsv('abcdef,Jane Doe\n GHIJKL ,John Smith')).toEqual(['ABCDEF', 'GHIJKL'])
  })

  test('drops rows whose first column is not a valid shortcode', () => {
    expect(parseShortcodeCsv('shortcode,name\nABCDEF,Jane')).toEqual(['ABCDEF'])
  })
})
