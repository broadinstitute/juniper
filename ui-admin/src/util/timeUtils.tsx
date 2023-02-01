export function instantToDefaultString(instant: number) {
  return new Date(instant * 1000).toLocaleString()
}
