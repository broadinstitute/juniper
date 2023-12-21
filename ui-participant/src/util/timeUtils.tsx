
// TODO: this is identical to a method in ui-admin/src/util/timeUtils.tsx
// move that to ui-core and use it here (there are other methods that should
// also be moved so: JN-781 ) -DC
/** Returns a locale date string given a java Instant. */
export function instantToDateString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleDateString()
}
