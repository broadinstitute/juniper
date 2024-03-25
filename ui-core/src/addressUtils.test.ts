import { AddressValidationResult } from 'src/types/address'
import { isAddressFieldValid } from 'src/addressUtils'

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

// TODO add back

// describe('explainAddressValidationResults', () => {
//   it('explains singular missing components', () => {
//     const validation: AddressValidationResult = {
//       valid: false,
//       invalidComponents: ['COUNTRY']
//     }
//
//     const explanation = explainAddressValidationResults(validation)
//
//     expect(explanation).toHaveLength(1)
//
//     expect(explanation[0]).toEqual(
//       'The country could not be verified.'
//     )
//   })
//   it('explains two missing components', () => {
//     const validation: AddressValidationResult = {
//       valid: false,
//       invalidComponents: ['COUNTRY', 'CITY']
//     }
//
//     const explanation = explainAddressValidationResults(validation)
//
//     expect(explanation).toHaveLength(1)
//
//     expect(explanation[0]).toEqual(
//       'The country and city could not be verified.'
//     )
//   })
//   it('explains n missing components', () => {
//     const validation: AddressValidationResult = {
//       valid: false,
//       invalidComponents: ['COUNTRY', 'CITY', 'SUBPREMISE']
//     }
//
//     const explanation = explainAddressValidationResults(validation)
//
//     expect(explanation).toHaveLength(1)
//
//     expect(explanation[0]).toEqual(
//       'The country, city and unit number could not be verified.'
//     )
//   })
//   it('explains 0 missing components', () => {
//     const validation: AddressValidationResult = {
//       valid: false
//     }
//
//     const explanation = explainAddressValidationResults(validation)
//
//     expect(explanation).toHaveLength(1)
//
//     expect(explanation[0]).toEqual(
//       'The address could not be verified. Please verify that the information is correct and try again.'
//     )
//   })
// })a
