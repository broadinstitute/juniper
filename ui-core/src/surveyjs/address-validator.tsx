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
    const addressValidationQuestions = sender
      .getAllQuestions(false)
      .filter(q => q.name.endsWith('addressValidation'))
      .filter(q => sender.getPageByQuestion(q).num === sender.currentPage?.num)

    if (addressValidationQuestions.length === 0) {
      complete()
      return
    }

    Promise.all(
      addressValidationQuestions.map(async addressValidationQuestion => {
        const questionNamePrefix = addressValidationQuestion.name.slice(
          0,
          addressValidationQuestion.name.length - 'addressValidation'.length)

        const mailingAddress: MailingAddress | undefined = assembleAddress(data, questionNamePrefix)

        if (!mailingAddress) {
          // could not assemble mailing address due to invalid survey set up
          return Promise.resolve()
        }

        const existingValidationState: AddressValidationQuestionValue = addressValidationQuestion.value


        // if user has already validated this address, and it had a suggestion which they denied.
        // we don't need to revalidate, we can just let them keep going.
        if (existingValidationState && existingValidationState.inputAddress
          && isSameAddress(existingValidationState.inputAddress, mailingAddress)
          && (existingValidationState.canceledSuggestedAddress || existingValidationState.acceptedSuggestedAddress)) {
          return Promise.resolve()
        }
        // clear all of the previous errors (they do not clear automatically :( )
        sender.getQuestionByName(`${questionNamePrefix}street1`)?.clearErrors()
        sender.getQuestionByName(`${questionNamePrefix}street2`)?.clearErrors()
        sender.getQuestionByName(`${questionNamePrefix}country`)?.clearErrors()
        sender.getQuestionByName(`${questionNamePrefix}city`)?.clearErrors()
        sender.getQuestionByName(`${questionNamePrefix}postalCode`)?.clearErrors()
        sender.getQuestionByName(`${questionNamePrefix}state`)?.clearErrors()

        // hit API
        const results = await validateAddress(mailingAddress)
        console.log(results)
        const addressValidationQuestionValue: AddressValidationQuestionValue = {
          inputAddress: mailingAddress,
          canceledSuggestedAddress: false,
          acceptedSuggestedAddress: false,
          addressValidationResult: results
        }
        addressValidationQuestion.value = addressValidationQuestionValue
        if (results.suggestedAddress && isSameAddress(results.suggestedAddress, mailingAddress)) {
          results.suggestedAddress = undefined
        }
        console.log(results.suggestedAddress)
        if (results.suggestedAddress) {
          errors[addressValidationQuestion.name] = 'Please review the suggested address.'
        } else if (!results.valid) {
          // make the fields go red
          errors[`${questionNamePrefix}street1`] = 'Address could not be validated.'
          errors[`${questionNamePrefix}street2`] = 'Address could not be validated.'
          errors[`${questionNamePrefix}country`] = 'Address could not be validated.'
          errors[`${questionNamePrefix}city`] = 'Address could not be validated.'
          errors[`${questionNamePrefix}postalCode`] = 'Address could not be validated.'
          errors[`${questionNamePrefix}state`] = 'Address could not be validated.'
        }
        return await Promise.resolve()
      })
    ).then(() => {
      console.log(errors)
      console.log('completing!!')
      complete()
    })
  }
}

const isSameAddress = (addr1: MailingAddress, addr2: MailingAddress): boolean => {
  return findDifferencesBetweenObjects(addr1, addr2)
    .filter(val => !['id', 'createdAt', 'lastUpdatedAt'].includes(val.fieldName))
    .length === 0
}

const assembleAddress = (data: { [index: string]: any; }, prefix: string): MailingAddress | undefined => {
  console.log(prefix)
  const requiredFields = [
    `${prefix}street1`,
    `${prefix}city`,
    `${prefix}state`,
    `${prefix}postalCode`
  ]

  for (const requiredField of requiredFields) {
    if (!Object.hasOwn(data, requiredField)) {
      return undefined // survey not set up properly; needs all fields
    }
  }

  return {
    street1: data[`${prefix}street1`],
    street2: data[`${prefix}street2`] || '',
    city: data[`${prefix}city`],
    state: data[`${prefix}state`],
    country: data[`${prefix}country`] || 'US',
    postalCode: data[`${prefix}postalCode`]
  }
}
