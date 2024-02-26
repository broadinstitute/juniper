/**
 * Detects if the browser doesn't support a set of functions
 */
export const isBrowserCompatible = (): boolean => {
  try {
    Object.hasOwn({ foo: 'bar' }, 'foo')
  } catch (e) {
    return false
  }
  return true
}
