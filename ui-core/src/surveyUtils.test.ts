import { surveyJSModelFromFormContent } from './surveyUtils'
import { FormContent } from './types/forms'

describe('surveyJSModelFromFormContent', () => {
  it('generates questions using question templates', () => {
    // Arrange
    const formContent: FormContent = {
      title: 'Test form',
      questionTemplates: [
        {
          type: 'radiogroup', title: 'what is their favorite color?', name: 'colorPicker',
          choices: [{ text: 'Green', value: 'green' }, { text: 'Blue', value: 'blue' }]
        }
      ],
      pages: [
        {
          elements: [
            { type: 'html', name: 'brotherIntro', html: '<span>Talk about your brother</span>' },
            { name: 'brotherFavoriteColor', questionTemplateName: 'colorPicker' }
          ]
        },
        {
          elements: [
            { type: 'html', name: 'sisterIntro', html: '<span>Talk about your sister</span>' },
            { name: 'sisterFavoriteColor', questionTemplateName: 'colorPicker' }
          ]
        }
      ]
    }

    // Act
    const surveyModel = surveyJSModelFromFormContent(formContent)

    // Assert
    const firstTemplatedQuestion = surveyModel.getQuestionByName('brotherFavoriteColor')
    expect(firstTemplatedQuestion.title).toEqual('what is their favorite color?')
    expect(firstTemplatedQuestion.choices).toHaveLength(2)

    const secondTemplatedQuestion = surveyModel.getQuestionByName('sisterFavoriteColor')
    expect(secondTemplatedQuestion.title).toEqual('what is their favorite color?')
    expect(secondTemplatedQuestion.choices).toHaveLength(2)
  })

  it('applies default survey settings', () => {
    // Arrange
    const formContent: FormContent = {
      title: 'Test form',
      pages: [
        {
          elements: [
            { name: 'question1', type: 'text', title: 'What is the answer?' }
          ]
        }
      ]
    }

    // Act
    const surveyModel = surveyJSModelFromFormContent(formContent)

    // Assert
    expect(surveyModel.focusFirstQuestionAutomatic).toBe(false)
    expect(surveyModel.showTitle).toBe(false)
    expect(surveyModel.widthMode).toBe('static')
  })
})
