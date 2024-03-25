import { Question, SurveyModel } from 'survey-core'
import { AddressValidationResult, MailingAddress } from 'src/types/address'
import { AddressValidationQuestionValue } from 'src/surveyjs/address-validation-modal-question'
import { getI18nErrorKeysByField, isSameAddress } from '../addressUtils'

/**
 * Creates SurveyJS address validator using the provided async function
 * to call the backend. To enable on a survey, there must be a question
 * of type 'addressvalidation' on the page. If provided, the results of
 * address validation will be stored there like any other
 * survey response.
 */
export function createAddressValidator(
  validateAddress: (val: MailingAddress) => Promise<AddressValidationResult>,
  i18n: (val: string) => string = (val: string) => val
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
            addressValidationQuestion,
            i18n)
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
  addressValidationQuestion: Question,
  i18n: (key: string) => string) => {
  const mailingAddress: MailingAddress | undefined = assembleAddressFromFormData(data, addressValidationQuestion)

  if (!mailingAddress) {
    // could not assemble mailing address (likely due to invalid survey set up)
    return Promise.resolve()
  }

  const existingValidationState: AddressValidationQuestionValue = addressValidationQuestion.value

  // if user has already validated this address, and it had a suggestion which they denied.
  // we don't need to revalidate, we can just let them keep going.
  if (shouldSkipValidation(existingValidationState, mailingAddress)) {
    return Promise.resolve()
  }

  // hit API
  const results = await validateAddress(mailingAddress)

  results.invalidComponents = [
    'SUBPREMISE', 'HOUSE_NUMBER', 'STREET_NAME', 'CITY', 'STATE_PROVINCE', 'POSTAL_CODE', 'COUNTRY'
  ]

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
    displayAppropriateErrors(addressValidationQuestion, newValidationState.addressValidationResult, errors, i18n)
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
    && (
      isSameAddress(existingValidationState.inputAddress, mailingAddress)
      || (
        existingValidationState.addressValidationResult.suggestedAddress
        && isSameAddress(existingValidationState.addressValidationResult.suggestedAddress, mailingAddress)
      )
    )
    && existingValidationState.modalDismissed
}

// TODO: this is _not_ internationalized, and only works well for US address formats. See JN-935
const displayAppropriateErrors = (
  addressValidationQuestion: Question,
  validationResult: AddressValidationResult,
  errors: {
    [index: string]: any // eslint-disable-line @typescript-eslint/no-explicit-any
  },
  i18n: (key: string) => string
) => {
  if (validationResult.invalidComponents && validationResult.invalidComponents.length > 0) {
    const explanation = getI18nErrorKeysByField(validationResult)

    // TODO make nicer
    if (explanation['street1']) {
      errors[addressValidationQuestion.street1] = explanation['street1']?.map(i18n).join('\n')
    }
    if (explanation['street2']) {
      errors[addressValidationQuestion.street2] = explanation['street2']?.map(i18n).join('\n')
    }
    if (explanation['country']) {
      errors[addressValidationQuestion.country] = explanation['country']?.map(i18n).join('\n')
    }
    if (explanation['city']) {
      errors[addressValidationQuestion.city] = explanation['city']?.map(i18n).join('\n')
    }
    if (explanation['postalCode']) {
      errors[addressValidationQuestion.postalCode] = explanation['postalCode']?.map(i18n).join('\n')
    }
    if (explanation['state']) {
      errors[addressValidationQuestion.stateProvince] = explanation['state']?.map(i18n).join('\n')
    }
  } else {
    errors[addressValidationQuestion.street1] = i18n('addressFailedToValidate')
    errors[addressValidationQuestion.street2] = i18n('addressFailedToValidate')
    errors[addressValidationQuestion.country] = i18n('addressFailedToValidate')
    errors[addressValidationQuestion.city] = i18n('addressFailedToValidate')
    errors[addressValidationQuestion.postalCode] = i18n('addressFailedToValidate')
    errors[addressValidationQuestion.stateProvince] = i18n('addressFailedToValidate')
  }
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
