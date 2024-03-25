import { AddressValidationResult } from 'src/types/address'
import { getErrorsByField, isAddressFieldValid } from 'src/addressUtils'

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

const mockI18n = (key: string) => `{${key}}`

describe('explainAddressValidationResults', () => {
  it('explains singular missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY']
    }

    const explanation = getErrorsByField(validation, mockI18n)

    expect(explanation).toEqual({
      'country': [
        '{addressInvalidCountry}'
      ]
    })
  })
  it('explains many missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY', 'CITY', 'POSTAL_CODE', 'SUBPREMISE', 'HOUSE_NUMBER']
    }

    const explanation = getErrorsByField(validation, mockI18n)

    expect(explanation).toEqual({
      'city': [
        '{addressInvalidCity}'
      ],
      'country': [
        '{addressInvalidCountry}'
      ],
      'postalCode': [
        '{addressInvalidPostalCode}'
      ],
      'street1': [
        '{addressNeedsSubpremise}',
        '{addressInvalidHouseNumber}'
      ]
    })
  })
  it('explains 0 missing components', () => {
    const validation: AddressValidationResult = {
      valid: false
    }

    const explanation = getErrorsByField(validation, mockI18n)

    expect(explanation).toEqual({})
  })
})
