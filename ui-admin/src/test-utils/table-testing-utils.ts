export function getTableCell(table: HTMLTableElement, rowHeader: string, colHeader: string) {
  const colIndex = Array.from(table.rows[0].cells).findIndex((cell) => cell.textContent === colHeader)
  const rowIndex = Array.from(table.rows).findIndex((row) => row.cells[0].textContent === rowHeader)
  return table.rows[rowIndex].cells[colIndex]
}

export function expectCellToHaveText(table: HTMLTableElement, rowHeader: string, colHeader: string, text: string) {
  expect(getTableCell(table, rowHeader, colHeader).textContent).toBe(text)
}
