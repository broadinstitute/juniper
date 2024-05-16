import { Screen, within } from '@testing-library/react'

/**
 * Assert that a row contains the given text content.
 */
export const assertRowContents = (
  row: Node, ...contains: string[]
) => {
  contains.forEach(shouldBePresent => {
    expect(row).toHaveTextContent(shouldBePresent)
  })
}

/**
 * Assert that a row does not contain the given text content.
 */
export const assertRowDoesNotContain = (
  row: Node, ...contains: string[]
) => {
  contains.forEach(shouldNotBePresent => {
    expect(row).not.toHaveTextContent(shouldNotBePresent)
  })
}


/**
 * Assuming only one table on the screen, get all the rows in the table.
 */
export const getRows = (screen: Screen): Node[] => {
  const table = screen.getByRole('table')

  // first row group is header
  const tbody = within(table).getAllByRole('rowgroup')[1]
  return within(tbody).getAllByRole('row')
}
