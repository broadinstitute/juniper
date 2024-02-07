import { AddressValidationResult } from 'src/types/address'
import { explainAddressValidationResults, isAddressFieldValid } from 'src/addressUtils'

describe('isAddressFieldValid tests', () => {
  it('considers fields valid by default if there are invalid components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['CITY']
    }

    expect(
      isAddressFieldValid(validation, 'street1')
    ).toBeTruthy()
  })

  it('considers fields invalid by default if there are no invalid components', () => {
    const validation: AddressValidationResult = {
      valid: false
    }

    expect(
      isAddressFieldValid(validation, 'street1')
    ).toBeFalsy()
  })
  it('considers fields invalid if they are in missingComponents', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY']
    }

    expect(
      isAddressFieldValid(validation, 'country')
    ).toBeFalsy()
    expect(
      isAddressFieldValid(validation, 'city')
    ).toBeTruthy()
  })
})

describe('explainAddressValidationResults', () => {
  it('explains singular missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY']
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
      invalidComponents: ['COUNTRY', 'CITY']
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
      invalidComponents: ['COUNTRY', 'CITY', 'SUBPREMISE']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The address is missing the country, city and unit number. Please add this information and try again.'
    )
  })
})
