import { Question, SurveyModel } from 'survey-core'
import { AddressComponent, AddressValidationResult, MailingAddress } from 'src/types/address'
import { AddressValidationQuestionValue } from 'src/surveyjs/address-validation-modal-question'
import { findDifferencesBetweenObjects } from '../objectUtils'

/**
 * Creates SurveyJS address validator using the provided async function
 * to call the backend. To enable on a survey, there must be a question
 * of type 'addressvalidation' on the page. If provided, the results of
 * address validation will be stored there like any other
 * survey response.
 */
export function createAddressValidator(
  validateAddress: (val: MailingAddress) => Promise<AddressValidationResult>
) {
  return (
    sender: SurveyModel,
    {
      data,
      errors,
      complete
    }: { data: { [index: string]: any; }, errors: { [index: string]: any; }, complete: () => void }) => {  // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
    const addressValidationQuestions = findAddressValidationQuestionsOnThisPage(sender)

    // no addresses to validate on this page
    if (addressValidationQuestions.length === 0) {
      complete()
      return
    }

    Promise.all(
      addressValidationQuestions.map(
        async addressValidationQuestion => {
          // clear all the previous errors (they do not clear automatically :( )
          clearExistingValidationErrors(sender, addressValidationQuestion)
          await validateSurveyJsAddress(
            validateAddress,
            data,
            errors,
            addressValidationQuestion)
        })
    ).then(() => {
      complete()
    })
  }
}

/**
 * Validates a single address for surveyjs; fills out any errors in the errors
 * map.
 */
export const validateSurveyJsAddress = async (
  validateAddress: (val: MailingAddress) => Promise<AddressValidationResult>,
  data: { [index: string]: any; }, // eslint-disable-line @typescript-eslint/no-explicit-any
  errors: { [index: string]: any; }, // eslint-disable-line @typescript-eslint/no-explicit-any
  addressValidationQuestion: Question) => {
  const mailingAddress: MailingAddress | undefined = assembleAddressFromFormData(data, addressValidationQuestion)

  if (!mailingAddress) {
    // could not assemble mailing address (likely due to invalid survey set up)
    return Promise.resolve()
  }

  if (mailingAddress.country !== 'US') {
    return Promise.resolve() // only US addresses are supported
  }

  const existingValidationState: AddressValidationQuestionValue = addressValidationQuestion.value

  // if user has already validated this address, and it had a suggestion which they denied.
  // we don't need to revalidate, we can just let them keep going.
  if (shouldSkipValidation(existingValidationState, mailingAddress)) {
    return Promise.resolve()
  }

  // hit API
  const results = await validateAddress(mailingAddress)

  const newValidationState: AddressValidationQuestionValue = {
    inputAddress: mailingAddress,
    canceledSuggestedAddress: false,
    acceptedSuggestedAddress: false,
    modalDismissed: false,
    addressValidationResult: results
  }

  addressValidationQuestion.value = newValidationState

  // if suggested address is the same as the inputted address,
  // remove it so we don't trigger the modal
  if (results.suggestedAddress && isSameAddress(results.suggestedAddress, mailingAddress)) {
    results.suggestedAddress = undefined
  }

  if (results.suggestedAddress) {
    errors[addressValidationQuestion.name] = 'Please review the suggested address.'
  } else if (!results.valid) {
    displayAppropriateErrors(addressValidationQuestion, newValidationState.addressValidationResult, errors)
  }

  return await Promise.resolve()
}

const clearExistingValidationErrors = (sender: SurveyModel, addressValidationQuestion: Question) => {
  sender.getQuestionByName(addressValidationQuestion.street1)?.clearErrors()
  sender.getQuestionByName(addressValidationQuestion.street2)?.clearErrors()
  sender.getQuestionByName(addressValidationQuestion.country)?.clearErrors()
  sender.getQuestionByName(addressValidationQuestion.city)?.clearErrors()
  sender.getQuestionByName(addressValidationQuestion.postalCode)?.clearErrors()
  sender.getQuestionByName(addressValidationQuestion.stateProvince)?.clearErrors()
}

const findAddressValidationQuestionsOnThisPage = (sender: SurveyModel) => {
  return sender
    .getAllQuestions(false)
    .filter(q => q.getType() === 'addressvalidation')
    .filter(q => sender.getPageByQuestion(q).num === sender.currentPage?.num)
}

const shouldSkipValidation = (
  existingValidationState: AddressValidationQuestionValue, mailingAddress: MailingAddress
) => {
  return existingValidationState && existingValidationState.inputAddress
    && isSameAddress(existingValidationState.inputAddress, mailingAddress)
    && (
      existingValidationState.canceledSuggestedAddress
      || existingValidationState.acceptedSuggestedAddress
      || existingValidationState.modalDismissed)
}

const isSameAddress = (addr1: MailingAddress, addr2: MailingAddress): boolean => {
  return findDifferencesBetweenObjects(addr1, addr2)
    .filter(val => !['id', 'createdAt', 'lastUpdatedAt'].includes(val.fieldName))
    .length === 0
}

// TODO: this is _not_ internationalized, and only works well for US address formats. See JN-935
const displayAppropriateErrors = (
  addressValidationQuestion: Question,
  validationResult: AddressValidationResult,
  errors: { [index: string]: any; } // eslint-disable-line @typescript-eslint/no-explicit-any
) => {
  if (validationResult.invalidComponents && validationResult.invalidComponents.length > 0) {
    const invalidComponents = validationResult.invalidComponents

    if (anyInvalid(invalidComponents, 'HOUSE_NUMBER', 'STREET_NAME', 'STREET_TYPE', 'SUBPREMISE')) {
      errors[addressValidationQuestion.street1] = 'The first address line could not be validated.'
    }

    if (anyInvalid(invalidComponents, 'CITY')) {
      errors[addressValidationQuestion.city] = 'City could not be validated.'
    }

    if (anyInvalid(invalidComponents, 'POSTAL_CODE')) {
      errors[addressValidationQuestion.postalCode] = 'ZIP code could not be validated'
    }

    if (anyInvalid(invalidComponents, 'STATE_PROVINCE')) {
      errors[addressValidationQuestion.stateProvince] = 'State could not be validated.'
    }

    if (anyInvalid(invalidComponents, 'COUNTRY')) {
      errors[addressValidationQuestion.stateProvince] = 'Country could not be validated.'
    }
  } else {
    errors[addressValidationQuestion.street1] = 'Address could not be validated.'
    errors[addressValidationQuestion.street2] = 'Address could not be validated.'
    errors[addressValidationQuestion.country] = 'Address could not be validated.'
    errors[addressValidationQuestion.city] = 'Address could not be validated.'
    errors[addressValidationQuestion.postalCode] = 'Address could not be validated.'
    errors[addressValidationQuestion.stateProvince] = 'Address could not be validated.'
  }
}

const anyInvalid = (invalidComponents: AddressComponent[], ...searchComponents: AddressComponent[]) => {
  return searchComponents.some(component => invalidComponents.includes(component))
}

const assembleAddressFromFormData = (data: {
  [index: string]: any; // eslint-disable-line @typescript-eslint/no-explicit-any
}, question: Question): MailingAddress | undefined => {
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
