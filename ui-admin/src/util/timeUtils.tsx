/** renders a java instant as a time string.  this should be sensitive to the user's locale */
export function instantToDefaultString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleString()
}
/** renders a java LocalDate as a date string.  this should be sensitive to the user's locale */
export function dateToDefaultString(date?: number[]) {
  if (typeof date === 'undefined' || date.length != 3) {
    return ''
  }
  // note we need to subtract one from the month parameter since java months are one-indexed and JS is zero-indexed
  return new Date(date[0], date[1]  - 1, date[2]).toLocaleDateString()
}

/** returns current date in ISO format, e.g. 2023-04-15 */
export function currentIsoDate() {
  return new Date().toISOString().substring(0, 10)
}
