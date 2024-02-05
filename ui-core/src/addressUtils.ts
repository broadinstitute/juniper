import { AddressComponent, AddressValidationResult } from 'src/types/address'
import { isNil } from 'lodash'


const FIELD_TO_ADDR_COMPONENTS : { [index: string]: AddressComponent[] } = {
  'city': ['CITY'],
  'street1': ['STREET_TYPE', 'HOUSE_NUMBER', 'STREET_NAME'],
  'street2': ['SUBPREMISE'],
  'country': ['COUNTRY'],
  'state': ['STATE_PROVINCE'],
  'postalCode': ['POSTAL_CODE']
}

const ADDR_COMPONENT_TO_NAME = {
  'CITY': 'City',
  'HOUSE_NUMBER': 'House Number',
  'STREET_NAME': 'Street Name',
  'COUNTRY': 'Country',
  'POSTAL_CODE': 'Postal/ZIP Code',
  'SUBPREMISE': 'Unit Number',
  'STREET_TYPE': 'Street Type',
  'STATE_PROVINCE': 'State/Province'
}


/**
 *
 */
export function isAddressFieldValid(
  validation: AddressValidationResult | undefined, field: string, val: string
) : boolean {
  if (validation?.valid) {
    return true
  }

  if (validation?.unresolvedTokens) {
    if (validation?.unresolvedTokens?.some(token => val?.includes(token))) {
      return false
    }
  }

  if (validation?.missingComponents) {
    const addrComponents = FIELD_TO_ADDR_COMPONENTS[field]
    if (addrComponents?.some(comp => validation?.missingComponents?.includes(comp))) {
      return false
    }
  }

  return true
}

/**
 *
 */
export function explainAddressValidationResults(validation: AddressValidationResult | undefined) : string[] {
  const out = []

  if (validation?.missingComponents) {
    const missingComponentNames = validation
      .missingComponents
      .map(comp => ADDR_COMPONENT_TO_NAME[comp])
      .filter(val => !isNil(val))

    if (missingComponentNames.length > 0) {
      out.push(`The address is missing: ${missingComponentNames.join(', ')}`)
    }
  }

  if (validation?.unresolvedTokens && validation.unresolvedTokens.length > 0) {
    out.push(`Could not resolve: ${validation.unresolvedTokens.join(', ')}`)
  }

  return out
}
