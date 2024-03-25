import { AddressComponent, AddressValidationResult, MailingAddress } from 'src/types/address'
import { isEmpty, isNil } from 'lodash'
import { findDifferencesBetweenObjects } from './objectUtils'


const FIELD_TO_ADDR_COMPONENTS: { [index: string]: AddressComponent[] } = {
  'city': ['CITY'],
  'street1': ['STREET_TYPE', 'HOUSE_NUMBER', 'STREET_NAME', 'SUBPREMISE'],
  'street2': [],
  'country': ['COUNTRY'],
  'state': ['STATE_PROVINCE'],
  'postalCode': ['POSTAL_CODE']
}

const ADDR_COMPONENTS_TO_FIELD: { [index: string]: keyof MailingAddress } = {
  'CITY': 'city',
  'STREET_TYPE': 'street1',
  'HOUSE_NUMBER': 'street1',
  'STREET_NAME': 'street1',
  'SUBPREMISE': 'street1',
  'COUNTRY': 'country',
  'STATE_PROVINCE': 'state',
  'POSTAL_CODE': 'postalCode'
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

const ADDR_COMPONENT_TO_I18N_ERROR_KEY = {
  'CITY': 'addressInvalidCity',
  'HOUSE_NUMBER': 'addressInvalidHouseNumber',
  'STREET_NAME': 'addressInvalidStreetName',
  'COUNTRY': 'addressInvalidCountry',
  'POSTAL_CODE': 'addressInvalidPostalCode',
  'SUBPREMISE': 'addressNeedsSubpremise',
  'STREET_TYPE': 'addressInvalidStreetType',
  'STATE_PROVINCE': 'addressInvalidState'
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
 * TODO
 */
export function getErrorsByField(
  validation: AddressValidationResult | undefined,
  i18n: (key: string) => string
): { [index: string]: string[] } {
  const out: { [index: string]: string[] } = {}

  if (!validation) {
    return {}
  }

  for (const missingComponent of validation.invalidComponents || []) {
    const i18nKey = ADDR_COMPONENT_TO_I18N_ERROR_KEY[missingComponent]
    const field = ADDR_COMPONENTS_TO_FIELD[missingComponent]

    if (!out[field]) {
      out[field] = []
    }

    out[field].push(i18n(i18nKey))
  }

  return out
}
/**
 * Compares two addresses deeply. Ignores metadata (id, createdAt, etc.)
 */
export function isSameAddress(addr1: MailingAddress, addr2: MailingAddress): boolean {
  return findDifferencesBetweenObjects(addr1, addr2)
    .filter(val => !['id', 'createdAt', 'lastUpdatedAt'].includes(val.fieldName))
    .length === 0
}

/**
 * todo
 */
export function toAddressLines(address: MailingAddress): string[] {
  return [
    address.street1,
    address.street2,
    `${address.city || ''} ${address.state || ''} ${address.postalCode || ''}`,
    address.country
  ].map(line => line?.trim()).filter(line => !isNil(line) && !isEmpty(line.trim()))
}
