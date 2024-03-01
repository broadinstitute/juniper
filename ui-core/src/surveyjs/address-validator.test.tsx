// eslint-disable-next-line @typescript-eslint/no-unused-vars, unused-imports/no-unused-imports
import { validateSurveyJsAddress } from 'src/surveyjs/address-validator'
import { QuestionAddressValidationModel } from 'src/surveyjs/address-validation-modal-question'


const setupMocks = (question: QuestionAddressValidationModel) => {
  jest
    .spyOn(question, 'street1', 'get')
    .mockImplementation(() => {
      return 'mock_street1_question'
    })
  jest
    .spyOn(question, 'street2', 'get')
    .mockImplementation(() => {
      return 'mock_street2_question'
    })
  jest
    .spyOn(question, 'city', 'get')
    .mockImplementation(() => {
      return 'mock_city_question'
    })
  jest
    .spyOn(question, 'stateProvince', 'get')
    .mockImplementation(() => {
      return 'mock_state_question'
    })
  jest
    .spyOn(question, 'country', 'get')
    .mockImplementation(() => {
      return 'mock_country_question'
    })
  jest
    .spyOn(question, 'postalCode', 'get')
    .mockImplementation(() => {
      return 'mock_postalCode_question'
    })
}


describe('valid address validation', () => {
  it('does nothing on same valid address being returned', async () => {
    const question = new QuestionAddressValidationModel('')
    const errors = {}

    setupMocks(question)

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      return Promise.resolve({
        suggestedAddress: {
          street1: '415 Main St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      })
    }, {
      'mock_street1_question': '415 Main St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(Object.keys(errors)).toHaveLength(0)

    expect(setQuestionStateSpy).toHaveBeenCalledWith({
      addressValidationResult: {
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      },
      inputAddress: {
        street1: '415 Main St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: false,
      acceptedSuggestedAddress: false
    })
  })

  it('alerts the user when there is a suggested address', async () => {
    const question = new QuestionAddressValidationModel('mock_question_validation_name')
    const errors = {}

    setupMocks(question)

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      return Promise.resolve({
        suggestedAddress: {
          street1: '415 Main St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      })
    }, {
      'mock_street1_question': '415 OOPS St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(errors).toEqual({
      'mock_question_validation_name': 'Please review the suggested address.'
    })

    expect(setQuestionStateSpy).toHaveBeenCalledWith({
      addressValidationResult: {
        suggestedAddress: {
          street1: '415 Main St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      },
      inputAddress: {
        street1: '415 OOPS St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: false,
      acceptedSuggestedAddress: false
    })
  })

  it('skips validation if user canceled suggestion', async () => {
    const question = new QuestionAddressValidationModel('mock_question_validation_name')
    const errors = {}

    setupMocks(question)

    question.value = {
      addressValidationResult: {
        suggestedAddress: {
          street1: '415 Main St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      },
      inputAddress: {
        street1: '415 OOPS St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: true, // user cancelled
      acceptedSuggestedAddress: false
    }

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      throw new Error('SHOULD NOT BE CALLED (should skip validation)')
    }, {
      'mock_street1_question': '415 OOPS St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(errors).toEqual({})

    expect(setQuestionStateSpy).not.toHaveBeenCalled()
  })

  it('revalidates if address changed even if user cancelled', async () => {
    const question = new QuestionAddressValidationModel('mock_question_validation_name')
    const errors = {}

    setupMocks(question)

    question.value = {
      addressValidationResult: {
        suggestedAddress: {
          street1: '415 Main St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      },
      inputAddress: {
        street1: '415 OOPS St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: true, // user cancelled
      acceptedSuggestedAddress: false
    }

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      return Promise.resolve({
        suggestedAddress: {
          street1: '415 New Suggestion St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      })
    }, {
      'mock_street1_question': '415 DIFFERENT St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(errors).toEqual({
      'mock_question_validation_name': 'Please review the suggested address.'
    })

    expect(setQuestionStateSpy).toHaveBeenCalledWith({
      addressValidationResult: {
        suggestedAddress: {
          street1: '415 New Suggestion St',
          state: 'MA',
          city: 'Cambridge',
          postalCode: '02142',
          country: 'US',
          street2: ''
        },
        invalidComponents: [],
        hasInferredComponents: false,
        valid: true,
        vacant: false
      },
      inputAddress: {
        street1: '415 DIFFERENT St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: false,
      acceptedSuggestedAddress: false
    })
  })
})


describe('invalid address validation', () => {
  it('errors only specified fields', async () => {
    const question = new QuestionAddressValidationModel('')
    const errors = {}

    setupMocks(question)

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      return Promise.resolve({
        invalidComponents: [
          'HOUSE_NUMBER',
          'CITY'
        ],
        hasInferredComponents: false,
        valid: false,
        vacant: false
      })
    }, {
      'mock_street1_question': '415 BAD St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(errors).toEqual({
      'mock_city_question': 'City could not be validated.',
      'mock_street1_question': 'The first address line could not be validated.'
    })

    expect(setQuestionStateSpy).toHaveBeenCalledWith({
      addressValidationResult: {
        invalidComponents: [
          'HOUSE_NUMBER',
          'CITY'
        ],
        hasInferredComponents: false,
        valid: false,
        vacant: false
      },
      inputAddress: {
        street1: '415 BAD St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: false,
      acceptedSuggestedAddress: false
    })
  })

  it('errors all fields on invalid', async () => {
    const question = new QuestionAddressValidationModel('')
    const errors = {}

    setupMocks(question)

    const setQuestionStateSpy = jest
      .spyOn(question, 'value', 'set')

    await validateSurveyJsAddress(() => {
      return Promise.resolve({
        invalidComponents: [],
        hasInferredComponents: false,
        valid: false,
        vacant: false
      })
    }, {
      'mock_street1_question': '415 BAD St',
      'mock_state_question': 'MA',
      'mock_postalCode_question': '02142',
      'mock_city_question': 'Cambridge'
      // state & country implied
    }, errors, question)

    expect(errors).toEqual({
      'mock_city_question': 'Address could not be validated.',
      'mock_country_question': 'Address could not be validated.',
      'mock_postalCode_question': 'Address could not be validated.',
      'mock_state_question': 'Address could not be validated.',
      'mock_street1_question': 'Address could not be validated.',
      'mock_street2_question': 'Address could not be validated.'
    })

    expect(setQuestionStateSpy).toHaveBeenCalledWith({
      addressValidationResult: {
        invalidComponents: [],
        hasInferredComponents: false,
        valid: false,
        vacant: false
      },
      inputAddress: {
        street1: '415 BAD St',
        state: 'MA',
        city: 'Cambridge',
        postalCode: '02142',
        country: 'US',
        street2: ''
      },
      canceledSuggestedAddress: false,
      acceptedSuggestedAddress: false
    })
  })
})
