/**
 * Detects if the browser doesn't support a set of functions. Right
 * now, we're only testing for Object.hasOwn since that should capture
 * most outdated browsers.
 */
export const isBrowserCompatible = (): boolean => {
  try {
    Object.hasOwn({ testKey: 'testValue' }, 'testKey')
  } catch (e) {
    return false
  }
  return true
}
