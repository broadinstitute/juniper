import { AddressValidationResult } from 'src/types/address'
import { explainAddressValidationResults, isAddressFieldValid } from 'src/addressUtils'

describe('isAddressFieldValid tests', () => {
  it('considers fields valid by default', () => {
    const validation: AddressValidationResult = {
      valid: false
    }

    expect(
      isAddressFieldValid(validation, 'street1', 'value')
    ).toBeTruthy()
  })
  it('considers fields invalid if they are in missingComponents', () => {
    const validation: AddressValidationResult = {
      valid: false,
      missingComponents: ['COUNTRY']
    }

    expect(
      isAddressFieldValid(validation, 'country', '')
    ).toBeFalsy()
    expect(
      isAddressFieldValid(validation, 'city', '')
    ).toBeTruthy()
  })
  it('considers fields invalid if they have an unresolved token', () => {
    const validation: AddressValidationResult = {
      valid: false,
      unresolvedTokens: ['asdf']
    }

    expect(
      isAddressFieldValid(validation, 'country', 'asdf')
    ).toBeFalsy()
    expect(
      isAddressFieldValid(validation, 'country', 'hjkl')
    ).toBeTruthy()
  })
})

describe('explainAddressValidationResults', () => {
  it('explains singular missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      missingComponents: ['COUNTRY']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The address is missing the country. Please add this information and try again.'
    )
  })
  it('explains two missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      missingComponents: ['COUNTRY', 'CITY']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The address is missing the country and city. Please add this information and try again.'
    )
  })
  it('explains n missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      missingComponents: ['COUNTRY', 'CITY', 'SUBPREMISE']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The address is missing the country, city and unit number. Please add this information and try again.'
    )
  })


  it('explains singular unresolved token', () => {
    const validation: AddressValidationResult = {
      valid: false,
      unresolvedTokens: ['asdf']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'A part of the address (\'asdf\') could not be verified. Please correct it and try again.'
    )
  })
  it('explains two unresolved tokens', () => {
    const validation: AddressValidationResult = {
      valid: false,
      unresolvedTokens: ['asdf', 'hjkl']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'Some parts of the address (\'asdf\' and \'hjkl\') could not be verified. Please correct them and try again.'
    )
  })
  it('explains n unresolved tokens', () => {
    const validation: AddressValidationResult = {
      valid: false,
      unresolvedTokens: ['asdf', 'hjkl', 'qwerty']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'Some parts of the address (\'asdf,\' \'hjkl\' and \'qwerty\') could not be verified.'
      +' Please correct them and try again.'
    )
  })
})
