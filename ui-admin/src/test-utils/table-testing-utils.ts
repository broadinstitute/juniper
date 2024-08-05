
/**
 * gets the element of the table cell at the intersection of the row and column headers
 * specify rowHaderIndex if the row header is not in the first column
 */
export function getTableCell(table: HTMLTableElement, rowHeader: string, colHeader: string, rowHeaderIndex = 0) {
  const colIndex = Array.from(table.rows[0].cells).findIndex(cell => cell.textContent === colHeader)
  const rowIndex = Array.from(table.rows).findIndex(row => row.cells[rowHeaderIndex].textContent === rowHeader)
  return table.rows[rowIndex].cells[colIndex]
}

/** expects the table cell at the intersection of the row and column headers to have the given text */
export function expectCellToHaveText(table: HTMLTableElement, rowHeader: string, colHeader: string, text: string) {
  expect(getTableCell(table, rowHeader, colHeader).textContent).toBe(text)
}
