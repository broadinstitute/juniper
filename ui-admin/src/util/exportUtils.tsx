import { concatSearchExpressions } from 'util/searchExpressionUtils'

export type ExportFilterOptions = {
  includeProxiesAsRows?: boolean,
  includeUnconsented?: boolean
}

/**
 * Builds a filter string for the export API. Defaults to only include consented subjects.
 */
export const buildFilter = (
  opts: ExportFilterOptions = {
    includeProxiesAsRows: false,
    includeUnconsented: false
  }): string => {
  const facets: string[] = []
  if (!opts.includeProxiesAsRows) {
    facets.push('{enrollee.subject} = true')
  }
  if (!opts.includeUnconsented) {
    facets.push('{enrollee.consented} = true')
  }
  return concatSearchExpressions(facets)
}
