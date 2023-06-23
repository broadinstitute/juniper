export interface IFacetValue {
  isDefault: () => boolean
  facet: Facet
}
export type StringFacetValueFields = { values: string[] }
export class StringFacetValue implements IFacetValue {
  values: string[]

  facet: StringFacet

  constructor(facet: StringFacet, facetVal: StringFacetValueFields = { values: [] }) {
    this.values = facetVal.values
    this.facet = facet
  }

  isDefault() {
    return this.values.length === 0
  }
}
export type IntRangeFacetValueFields = {
  min: number | null
  max: number | null
}
export class IntRangeFacetValue implements IFacetValue {
  min: number | null

  max: number | null

  facet: IntRangeFacet

  constructor(facet: IntRangeFacet, facetVal: IntRangeFacetValueFields = { min: null, max: null }) {
    this.min = facetVal.min
    this.max = facetVal.max
    this.facet = facet
  }

  isDefault() {
    return this.max === null && this.min === null
  }
}

export class StableIdStringValue {
  stableId: string | null

  values: string[]

  constructor(stableId: string | null = null, values: string[] = []) {
    this.values = values
    this.stableId = stableId
  }

  isDefault() {
    return this.values.length === 0
  }
}
export type StableIdStringArrayFacetValueFields = { values: StableIdStringValue[] }
export class StableIdStringArrayFacetValue implements IFacetValue {
  values: StableIdStringValue[]

  facet: StableIdStringArrayFacet

  constructor(facet: StableIdStringArrayFacet, facetVal: StableIdStringArrayFacetValueFields = { values: [] }) {
    this.values = facetVal.values
    this.facet = facet
  }

  isDefault() {
    return this.values.length === 0 || !this.values.some(val => !val.isDefault())
  }
}

export type FacetValue =  StringFacetValue | IntRangeFacetValue | StableIdStringArrayFacetValue

export type FacetType = | 'INT_RANGE' | 'STRING' | 'STABLEID_STRING'

export type BaseFacet = {
  keyName: string,
  category: string,
  label: string,
  type: FacetType,
}

export type IntRangeFacet = BaseFacet & {
  type: 'INT_RANGE'
  min: number | null
  max: number | null
}

export type FacetOption = {
  label: string, value: string
}

export type StringFacet = BaseFacet & {
  type: 'STRING',
  options: FacetOption[]
}

export type StableIdStringArrayFacet = BaseFacet & {
  type: 'STABLEID_STRING',
  options: FacetOption[]
  stableIdOptions: FacetOption[]
}

export type Facet = StringFacet | StableIdStringArrayFacet | IntRangeFacet

export const SAMPLE_FACETS: Facet[] = [{
  category: 'profile',
  keyName: 'age',
  label: 'Age',
  type: 'INT_RANGE',
  max: 150,
  min: 0
}, {
  category: 'profile',
  keyName: 'sexAtBirth',
  label: 'Sex at birth',
  type: 'STRING',
  options: [
    { value: 'male', label: 'male' },
    { value: 'female', label: 'female' },
    { value: 'other', label: 'other' },
    { value: 'unknown', label: 'unknown' }
  ]
}, {
  category: 'participantTask',
  keyName: 'status',
  label: 'Task status',
  type: 'STABLEID_STRING',
  options: [
    { value: 'COMPLETE', label: 'Complete' },
    { value: 'IN_PROGRESS', label: 'In progress' },
    { value: 'NEW', label: 'New' }
  ],
  stableIdOptions: [
    { value: 'oh_oh_consent', label: 'Consent' },
    { value: 'oh_oh_basicInfo', label: 'Basics' },
    { value: 'oh_oh_cardioHx', label: 'Cardio History' }
  ]
}]

/** helper function for making sure a function addresses all facet types.
 * returnPlaceholder isn't used, but is helpful for when this is needed at the end of a function for return type */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const checkExhaustiveFacetType = <T>(facetType: never, returnPlaceholder?: T): T => {
  throw new Error(`Unhandled facet type ${facetType}`)
}

/**
 * helper constructor to return the right facet value class for the given raw value object
 */
export const newFacetValue = (facet: Facet, facetValue?: object): FacetValue => {
  const facetType = facet.type
  if (facetType === 'INT_RANGE') {
    return new IntRangeFacetValue(facet, facetValue as IntRangeFacetValueFields)
  } else if (facetType === 'STABLEID_STRING') {
    const newValues = facetValue ? (facetValue as StableIdStringArrayFacetValueFields).values.map(stableIdVal =>
      new StableIdStringValue(stableIdVal.stableId, stableIdVal.values)
    ) : []
    return new StableIdStringArrayFacetValue(facet, { values: newValues })
  } else if (facetType === 'STRING') {
    return new StringFacetValue(facet, facetValue as StringFacetValueFields)
  }
  return checkExhaustiveFacetType(facetType, new IntRangeFacetValue(facet))
}

/**
 * converts an array of FacetValues to a string, such as for including in a URL.  this does not handle url escaping
 */
export const facetValuesToString = (facetValues: FacetValue[]): string => {
  const paramObj: Record<string, Record<string, object>> = {}
  facetValues
    .filter(facetValue => !facetValue.isDefault())
    .forEach(facetValue => {
      const category = facetValue.facet.category
      const keyName = facetValue.facet.keyName
      // strip out the 'facet' property since we don't need to send that
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { facet: _, ...values } = facetValue
      paramObj[category] = paramObj[category] ?? {}
      paramObj[category][keyName] = values
    })
  return JSON.stringify(paramObj)
}

/**
 * Converts a string into an array of FacetValues, (this does not manage url un-escaping)
 */
export const facetValuesFromString = (paramString: string, facets: Facet[]): FacetValue[] => {
  const facetValues = []
  const paramObj = JSON.parse(paramString)

  for (const categoryName in paramObj) {
    const category = paramObj[categoryName]
    for (const keyName in category) {
      const matchedFacet = facets.find(facet => facet.category === categoryName && facet.keyName === keyName) as Facet
      if (!matchedFacet) {
        // for now, just drop unknown facets
        continue
      }
      facetValues.push(newFacetValue(matchedFacet, category[keyName]))
    }
  }
  return facetValues
}
