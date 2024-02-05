import { AddressComponent, AddressValidationResult, MailingAddress } from 'src/types/address'
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
  'POSTAL_CODE': 'Postal Code',
  'SUBPREMISE': 'Unit Number',
  'STREET_TYPE': 'Street Type',
  'STATE_PROVINCE': 'State/Province'
}


/**
 * Determines whether a field should be in an error state based on the address validation result.
 */
export function isAddressFieldValid(
  validation: AddressValidationResult | undefined, field: keyof MailingAddress, val: string
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
 * Creates a list of strings where each string is a simple explanation of the validation results, e.g.:
 * 'Address is missing the state field. Please fill it out and try again.'
 */
export function explainAddressValidationResults(validation: AddressValidationResult | undefined) : string[] {
  const out = []

  if (validation?.missingComponents) {
    const missingComponentNames = validation
      .missingComponents
      .map(comp => ADDR_COMPONENT_TO_NAME[comp])
      .map(val => val.toLowerCase())
      .filter(val => !isNil(val))

    if (missingComponentNames.length > 0) {
      if (missingComponentNames.length === 1) {
        out.push(
          `The address is missing the ${
            missingComponentNames[0]
          }. Please add this information and try again.`)
      } else {
        out.push(
          `The address is missing the ${
            missingComponentNames.slice(0, missingComponentNames.length - 1).join(', ')
          } and ${
            missingComponentNames[missingComponentNames.length - 1]
          }. Please add this information and try again.`)
      }
    }
  }

  if (validation?.unresolvedTokens && validation.unresolvedTokens.length > 0) {
    const unresolvedTokens = validation.unresolvedTokens
    if (unresolvedTokens.length === 1) {
      out.push(
        `A part of the address ('${
          unresolvedTokens[0]
        }') could not be verified. Please correct it and try again.`)
    } else {
      out.push(
        `Some parts of the address ('${
          unresolvedTokens.slice(0, unresolvedTokens.length - 1).join(',\' \'')
        }' and '${
          unresolvedTokens[unresolvedTokens.length - 1]
        }') could not be verified. Please correct them and try again.`)
    }
  }

  return out
}
