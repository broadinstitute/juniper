export interface IFacetValue {
  isDefault: () => boolean
  facet: Facet
}
export type StringFacetValueFields = { values: string[] }
export class StringOptionsFacetValue implements IFacetValue {
  values: string[]

  facet: StringOptionsFacet

  constructor(facet: StringOptionsFacet, facetVal: StringFacetValueFields = { values: [] }) {
    this.values = facetVal.values
    this.facet = facet
  }

  isDefault() {
    return this.values.length === 0
  }
}

export class StringFacetValue implements IFacetValue {
  values: string[]

  facet: StringFacet

  constructor(facet: StringFacet, facetVal: StringFacetValueFields = { values: [] }) {
    this.values = this.normalizeValues(facetVal)
    this.facet = facet
  }

  normalizeValues(facetValues: StringFacetValueFields) {
    return facetValues.values.map(val => val.trim()).filter(val => val.length > 0)
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

export class EntityOptionsValue {
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
export type EntityOptionsArrayFacetValueFields = { values: EntityOptionsValue[] }
export class EntityOptionsArrayFacetValue implements IFacetValue {
  values: EntityOptionsValue[]

  facet: EntityOptionsArrayFacet

  constructor(facet: EntityOptionsArrayFacet, facetVal: EntityOptionsArrayFacetValueFields = { values: [] }) {
    this.values = facetVal.values
    this.facet = facet
  }

  isDefault() {
    return this.values.length === 0 || !this.values.some(val => !val.isDefault())
  }
}

export type FacetValue =  StringOptionsFacetValue | IntRangeFacetValue |
    EntityOptionsArrayFacetValue | StringFacetValue

export type FacetType = | 'INT_RANGE' | 'STRING' | 'STRING_OPTIONS' | 'ENTITY_OPTIONS'

export type BaseFacet = {
  keyName: string,
  category: string,
  label: string,
  facetType: FacetType,
}

export type IntRangeFacet = BaseFacet & {
  facetType: 'INT_RANGE'
  min: number | null
  max: number | null
}

export type FacetOption = {
  label: string, value: string
}

export type StringFacet = BaseFacet & {
  facetType: 'STRING',
  title: string,
  placeholder: string
}

export type StringOptionsFacet = BaseFacet & {
  facetType: 'STRING_OPTIONS',
  options: FacetOption[]
}

export type EntityOptionsArrayFacet = BaseFacet & {
  facetType: 'ENTITY_OPTIONS',
  options: FacetOption[]
  entities: FacetOption[]
}

export type Facet = StringFacet | StringOptionsFacet | EntityOptionsArrayFacet | IntRangeFacet

export const SEX_FACET: StringOptionsFacet = {
  category: 'profile',
  keyName: 'sexAtBirth',
  label: 'Sex at birth',
  facetType: 'STRING_OPTIONS',
  options: [
    { value: 'male', label: 'Male' },
    { value: 'female', label: 'Female' },
    { value: 'other', label: 'Other' },
    { value: 'unknown', label: 'Unknown' }
  ]
}

export const AGE_FACET: IntRangeFacet = {
  category: 'profile',
  keyName: 'age',
  label: 'Age',
  facetType: 'INT_RANGE',
  max: 150,
  min: 0
}

export const KEYWORD_FACET: StringFacet = {
  category: 'keyword',
  keyName: 'keyword',
  label: 'Keyword',
  facetType: 'STRING',
  title: 'search name, email and shortcode',
  placeholder: 'Search name, email and shortcode...'
}

export const ALL_FACETS = [
  KEYWORD_FACET,
  AGE_FACET,
  SEX_FACET
]

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
  const facetType = facet.facetType
  if (facetType === 'INT_RANGE') {
    return new IntRangeFacetValue(facet, facetValue as IntRangeFacetValueFields)
  } else if (facetType === 'ENTITY_OPTIONS') {
    const newValues = facetValue ? (facetValue as EntityOptionsArrayFacetValueFields).values.map(stableIdVal =>
      new EntityOptionsValue(stableIdVal.stableId, stableIdVal.values)
    ) : []
    return new EntityOptionsArrayFacetValue(facet, { values: newValues })
  } else if (facetType === 'STRING_OPTIONS') {
    return new StringOptionsFacetValue(facet, facetValue as StringFacetValueFields)
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
