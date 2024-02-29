import { Question, SurveyModel } from 'survey-core'
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
        const mailingAddress: MailingAddress | undefined = assembleAddress(data, addressValidationQuestion)

        console.log('addr')
        console.log(mailingAddress)
        console.log(addressValidationQuestion.street1)
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
        sender.getQuestionByName(addressValidationQuestion.street1)?.clearErrors()
        sender.getQuestionByName(addressValidationQuestion.street2)?.clearErrors()
        sender.getQuestionByName(addressValidationQuestion.country)?.clearErrors()
        sender.getQuestionByName(addressValidationQuestion.city)?.clearErrors()
        sender.getQuestionByName(addressValidationQuestion.postalCode)?.clearErrors()
        sender.getQuestionByName(addressValidationQuestion.stateProvince)?.clearErrors()

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
          errors[addressValidationQuestion.street1] = 'Address could not be validated.'
          errors[addressValidationQuestion.street2] = 'Address could not be validated.'
          errors[addressValidationQuestion.country] = 'Address could not be validated.'
          errors[addressValidationQuestion.city] = 'Address could not be validated.'
          errors[addressValidationQuestion.postalCode] = 'Address could not be validated.'
          errors[addressValidationQuestion.stateProvince] = 'Address could not be validated.'
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

const assembleAddress = (data: { [index: string]: any; }, question: Question): MailingAddress | undefined => { // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
  const requiredFields = [
    question.street1,
    question.city,
    question.stateProvince,
    question.postalCode
  ]

  for (const requiredField of requiredFields) {
    if (!Object.hasOwn(data, requiredField)) {
      return undefined // survey not set up properly; needs all fields
    }
  }

  return {
    street1: data[question.street1],
    street2: data[question.street2] || '',
    city: data[question.city],
    state: data[question.stateProvince],
    country: data[question.country] || 'US',
    postalCode: data[question.postalCode]
  }
}
