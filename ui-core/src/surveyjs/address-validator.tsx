import { SurveyModel } from 'survey-core'
import { AddressValidationResult, MailingAddress } from 'src/types/address'
import { AddressValidationQuestionValue } from 'src/surveyjs/address-validation-modal-question'
import { findDifferencesBetweenObjects } from '../objectUtils'

/**
 *
 */
export function createAddressValidator(validateAddress: (val: MailingAddress) => Promise<AddressValidationResult>) {
  return (
    sender: SurveyModel,
    {
      data,
      errors,
      complete
    }: { data: { [index: string]: any; }, errors: { [index: string]: any; }, complete: () => void }) => {  // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
    const addressValidationQuestions = Object.keys(data).filter(key => key.endsWith('addressValidation'))

    addressValidationQuestions.forEach(addressValidationQuestion => {
      if (sender.getPageByQuestion(sender.getQuestionByName(addressValidationQuestion)).num != sender.currentPageNo) {
        return // address is on different page of survey, let's not worry about it
      }

      const questionNamePrefix = addressValidationQuestion.slice(
        0,
        addressValidationQuestion.length - 'addressValidation'.length)

      const mailingAddress: MailingAddress | undefined = assembleAddress(data, questionNamePrefix)

      if (!mailingAddress) {
        // could not assemble mailing address due to invalid survey set up
        return
      }

      const existingValidationState: AddressValidationQuestionValue = data[addressValidationQuestion]


      // if user has already validated this address, and it had a suggestion which they denied.
      // we don't need to revalidate, we can just let them keep going.
      if (existingValidationState
        && isSameAddress(existingValidationState.inputAddress, mailingAddress)
        && existingValidationState.canceledSuggestedAddress) {
        return
      }


      validateAddress(mailingAddress)
        .then((results: AddressValidationResult) => {
          const addressValidationQuestionValue: AddressValidationQuestionValue = {
            inputAddress: mailingAddress,
            canceledSuggestedAddress: false,
            addressValidationResult: results
          }
          sender.setValue(addressValidationQuestion, addressValidationQuestionValue)

          if (results.suggestedAddress && isSameAddress(results.suggestedAddress, mailingAddress)) {
            results.suggestedAddress = undefined
          }

          if (results.suggestedAddress) {
            errors[addressValidationQuestion] = 'Please review the suggested address.'
          } else if (!results.valid) {
            errors[addressValidationQuestion] = 'Address could not be validated.'
            // make the fields go red
            errors[`${questionNamePrefix}street1`] = ''
            errors[`${questionNamePrefix}street2`] = ''
            errors[`${questionNamePrefix}country`] = ''
            errors[`${questionNamePrefix}city`] = ''
            errors[`${questionNamePrefix}postalCode`] = ''
            errors[`${questionNamePrefix}state`] = ''
          }
        })
    })

    complete()
  }
}

const isSameAddress = (addr1: MailingAddress, addr2: MailingAddress): boolean => {
  return findDifferencesBetweenObjects(addr1, addr2)
    .filter(val => !['id', 'createdAt', 'lastUpdatedAt'].includes(val.fieldName))
    .length === 0
}

const assembleAddress = (data: { [index: string]: any; }, prefix: string): MailingAddress | undefined => {
  const requiredFields = [
    `${prefix}street1`,
    `${prefix}street2`,
    `${prefix}city`,
    `${prefix}state`,
    `${prefix}country`,
    `${prefix}postalCode`
  ]

  for (const requiredField of requiredFields) {
    if (!Object.hasOwn(data, requiredField)) {
      return undefined // survey not set up properly; needs all fields
    }
  }

  return {
    street1: data[`${prefix}street1`],
    street2: data[`${prefix}street2`],
    city: data[`${prefix}city`],
    state: data[`${prefix}state`],
    country: data[`${prefix}country`],
    postalCode: data[`${prefix}postalCode`]
  }
}
