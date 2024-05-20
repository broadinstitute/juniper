export type MailingAddress = {
  street1: string,
  street2: string,
  city: string,
  state: string,
  country: string,
  postalCode: string
}

export type AddressComponent = 'HOUSE_NUMBER' | 'STREET_NAME' | 'STREET_TYPE' | 'CITY'
  | 'STATE_PROVINCE' | 'COUNTRY' | 'POSTAL_CODE' | 'SUBPREMISE';

export type AddressValidationResult = {
  valid: boolean,
  suggestedAddress?: MailingAddress,
  invalidComponents?: AddressComponent[],
  hasInferredComponents?: boolean,
  vacant?: boolean
}
