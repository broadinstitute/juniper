/** renders a java instant as a time string.  this should be sensitive to the user's locale */
export function instantToDefaultString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleString()
}

/** Returns a locale date string given a java Instant. */
export function instantToDateString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleDateString()
}

/** renders a java LocalDate as a date string.  this should be sensitive to the user's locale */
export function dateToDefaultString(date: number[] | undefined): string {
  const jsDate: Date | undefined = javaLocalDateToJsDate(date)
  if (!jsDate) {
    return ''
  }
  return jsDate.toLocaleDateString()
}

/** converts a java LocalDate to a JavaScript Date object */
export function javaLocalDateToJsDate(date: number[] | undefined): Date | undefined {
  if (typeof date === 'undefined' || date.length != 3) {
    return undefined
  }
  // note we need to subtract one from the month parameter since java months are one-indexed and JS is zero-indexed
  return new Date(date[0], date[1] - 1, date[2])
}

/** converts a JavaScript Date object to a java LocalDate (assuming UTC time) */
export function jsDateToJavaLocalDate(date: Date): number[] {
  return [date.getUTCFullYear(), date.getUTCMonth() + 1, date.getUTCDate()]
}

/** returns current date in ISO format, e.g. 2023-04-15 */
export function currentIsoDate() {
  return new Date().toISOString().substring(0, 10)
}

/**
 * Returns the equivilant of a java Instant given an ISO date. Useful for passing an ISO date into other util functions.
 */
export function isoToInstant(isoDate?: string): number | undefined {
  if (!isoDate) {
    return undefined
  }
  return Date.parse(isoDate) / 1000
}

/**
 * Returns the specified date minus the specified number of days.
 */
export function dateMinusDays(date: Date, daysAgo: number) {
  return new Date(date.getTime() - daysAgo * 24 * 60 * 60 * 1000)
}
