import {
  Facet,
  IntRangeFacetValue,
  newFacetValue,
  facetValuesToString, EntityOptionsArrayFacetValue,
  StringOptionsFacetValue, facetValuesFromString
} from './enrolleeSearch'

const rangeFacet: Facet = {
  category: 'profile',
  keyName: 'age',
  label: 'Age',
  facetType: 'INT_RANGE',
  min: 0,
  max: 150
}

const stringFacet: Facet = {
  category: 'profile',
  keyName: 'sexAtBirth',
  label: 'Sex at birth',
  facetType: 'STRING_OPTIONS',
  options: [
    { value: 'male', label: 'male' },
    { value: 'female', label: 'female' },
    { value: 'other', label: 'other' },
    { value: 'unknown', label: 'unknown' }
  ]
}

const stableIdFacet: Facet  = {
  category: 'participantTask',
  keyName: 'status',
  label: 'Task status',
  facetType: 'ENTITY_OPTIONS',
  options: [
    { value: 'COMPLETE', label: 'Complete' },
    { value: 'IN_PROGRESS', label: 'In progress' },
    { value: 'NEW', label: 'New' }
  ],
  entities: [
    { value: 'oh_oh_consent', label: 'Consent' },
    { value: 'oh_oh_basicInfo', label: 'Basics' },
    { value: 'oh_oh_cardioHx', label: 'Cardio History' }
  ]
}

describe('enrolleeSearch newFacetValue', () => {
  it('gets a default facet value for int range facets', () => {
    const facetVal: IntRangeFacetValue = newFacetValue(rangeFacet) as IntRangeFacetValue
    expect(facetVal.isDefault()).toEqual(true)
    expect(facetVal.min).toEqual(null)
    expect(facetVal.max).toEqual(null)
  })

  it('gets a facet value with specified value for int range facets', () => {
    const facetVal: IntRangeFacetValue = newFacetValue(rangeFacet, { max: 5 }) as IntRangeFacetValue
    expect(facetVal.isDefault()).toEqual(false)
    expect(facetVal.min).toEqual(undefined)
    expect(facetVal.max).toEqual(5)
  })

  it('gets a default value for string facets', () => {
    const facetVal: StringOptionsFacetValue = newFacetValue(stringFacet) as StringOptionsFacetValue
    expect(facetVal.isDefault()).toEqual(true)
    expect(facetVal.values).toEqual([])
  })

  it('gets a facet value with specified value for string values', () => {
    const facetVal: StringOptionsFacetValue =
        newFacetValue(stringFacet, { values: ['male'] }) as StringOptionsFacetValue
    expect(facetVal.isDefault()).toEqual(false)
    expect(facetVal.values).toEqual(['male'])
  })

  it('gets a default value for combined string array facets', () => {
    const facetVal: EntityOptionsArrayFacetValue = newFacetValue(stableIdFacet) as EntityOptionsArrayFacetValue
    expect(facetVal.isDefault()).toEqual(true)
    expect(facetVal.values).toEqual([])
  })

  it('gets a facet value with specified value for  combined string array facets', () => {
    const facetVal: EntityOptionsArrayFacetValue = newFacetValue(stableIdFacet,
      { values: [{ stableId: 'oh_oh_consent', values: ['COMPLETE'] }] }) as EntityOptionsArrayFacetValue
    expect(facetVal.isDefault()).toEqual(false)
    expect(facetVal.values).toEqual([{ stableId: 'oh_oh_consent', values: ['COMPLETE'] }])
  })
})

describe('enrolleeSearch facetValuesToString', () => {
  it('renders an empty object for empty facet list', () => {
    expect(facetValuesToString([])).toEqual('{}')
  })

  it('renders an empty object for facet list only containing defaults', () => {
    const facetVal: StringOptionsFacetValue = newFacetValue(stringFacet) as StringOptionsFacetValue
    expect(facetValuesToString([facetVal])).toEqual('{}')
  })

  it('renders an object for facet list containing non-default values', () => {
    const facetVal: StringOptionsFacetValue = newFacetValue(stringFacet,
      { values: ['male'] }) as StringOptionsFacetValue
    expect(facetValuesToString([facetVal])).toEqual('{"profile":{"sexAtBirth":{"values":["male"]}}}')
  })
})


describe('enrolleeSearch facetValuesFromString', () => {
  it('returns an empty array from an empty object', () => {
    expect(facetValuesFromString('{}', [stringFacet, rangeFacet])).toEqual([])
  })

  it('returns an array of a specified facet', () => {
    expect(facetValuesFromString('{"profile":{"sexAtBirth":{"values":["male"]}}}',
      [stringFacet, rangeFacet])).toEqual([newFacetValue(stringFacet, { values: ['male'] })])
  })
})
