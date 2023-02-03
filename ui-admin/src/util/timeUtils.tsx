/** renders a java instant as a time string.  this should be sensitive to the user's locale */
export function instantToDefaultString(instant: number) {
  return new Date(instant * 1000).toLocaleString()
}
