import { isNil, parseInt } from 'lodash'

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
export function dateToDefaultString(date: number[] | undefined) {
  if (typeof date === 'undefined' || date.length != 3) {
    return ''
  }
  // note we need to subtract one from the month parameter since java months are one-indexed and JS is zero-indexed
  return new Date(date[0], date[1]  - 1, date[2]).toLocaleDateString()
}

/**
 * Convert java date into the US locale string, which is MM/DD/YYYY.
 */
export function dateToUSLocaleString(date: number[] | undefined): string {
  if (typeof date === 'undefined' || date.length != 3 || !date.every(val => !isNil(val))) {
    return ''
  }

  console.log(date)
  return `${(date[1]).toString().padStart(2, '0')}/${date[2].toString().padStart(2, '0')}/${date[0].toString()}`
}

/**
 * Convert US date format (MM/DD/YYYY) into java date
 */
export function usLocalStringToDate(date: string): number[] | undefined {
  const splitDate: string[] = date.split('/')
  if (splitDate.length != 3) {
    return undefined
  }
  console.log(splitDate)
  if (splitDate.findIndex(val => val === '') >= 0) {
    return undefined
  }

  try {
    const splitDateAsNums = splitDate.map(parseInt)

    const y = splitDateAsNums[2]
    const m = splitDateAsNums[0]
    const d = splitDateAsNums[1]

    // basic sanity check
    if (m > 12 || d > 31 || m <= 0 || d <= 0 || y < 1880) {
      return undefined
    }

    // [yyyy, mm, dd]
    return [y, m, d]
  } catch (e) {
    return undefined
  }
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
