/** renders a java instant as a time string.  this should be sensitive to the user's locale */
export function instantToDefaultString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleString()
}
/** renders a java LocalDate as a date string.  this should be sensitive to the user's locale */
export function dateToDefaultString(date: number) {
  return new Date(date).toLocaleDateString()
}
