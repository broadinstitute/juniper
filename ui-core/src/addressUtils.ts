import { AddressComponent, AddressValidationResult, MailingAddress } from 'src/types/address'
import { isEmpty, isNil } from 'lodash'


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
  'POSTAL_CODE': 'Postal Code',
  'SUBPREMISE': 'Unit Number',
  'STREET_TYPE': 'Street Type',
  'STATE_PROVINCE': 'State/Province'
}


/**
 * Determines whether a field should be in an error state based on the address validation result.
 */
export function isAddressFieldValid(
  validation: AddressValidationResult | undefined, field: keyof MailingAddress
) : boolean {
  if (!validation || validation.valid) {
    return true
  }

  // not valid but no invalid component specified
  if (!validation.valid && isEmpty(validation.invalidComponents)) {
    return false
  }

  if (validation.invalidComponents) {
    const addrComponents = FIELD_TO_ADDR_COMPONENTS[field]
    if (addrComponents?.some(comp => validation?.invalidComponents?.includes(comp))) {
      return false
    }
  }

  return true
}


/**
 * Creates a list of strings where each string is a simple explanation of the validation results, e.g.:
 * 'Address is missing the state field. Please fill it out and try again.'
 */
export function explainAddressValidationResults(
  validation: AddressValidationResult | undefined
): string[] {
  const out: string[] = []

  if (!validation) {
    return out
  }

  const invalidComponents = validation.invalidComponents || []

  const missingComponentNames = invalidComponents
    .map(comp => ADDR_COMPONENT_TO_NAME[comp])
    .map(val => val.toLowerCase())
    .filter(val => !isNil(val))

  if (missingComponentNames.length > 0) {
    if (missingComponentNames.length === 1) {
      out.push(
        `The ${
            missingComponentNames[0]
        } field could not be verified. Please check and try again.`
      )
    } else {
      out.push(
        `The ${
            missingComponentNames.slice(0, missingComponentNames.length - 1).join(', ')
          } and ${
            missingComponentNames[missingComponentNames.length - 1]
        } fields could not be verified. Please check them and try again.`
      )
    }
  } else {
    out.push(
      `The address could not be verified. Please verify that the information is correct and try again.`
    )
  }

  return out
}
