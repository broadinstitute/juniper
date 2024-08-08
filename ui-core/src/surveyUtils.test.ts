import {
  getSurveyJsAnswerList,
  surveyJSModelFromFormContent
} from './surveyUtils'
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

describe('answers', () => {
  it('creates answers from survey results', () => {
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
    const surveyModel = surveyJSModelFromFormContent(formContent)
    surveyModel.data = { question1: 'answer' }

    const answers = getSurveyJsAnswerList(surveyModel)

    expect(answers).toHaveLength(1)
    expect(answers[0].questionStableId).toEqual('question1')
    expect(answers[0].stringValue).toEqual('answer')
  })
  it('creates extra answers for questions with dynamic panels', () => {
    const formContent: FormContent = {
      title: 'Test form',
      pages: [
        {
          elements: [
            {
              type: 'paneldynamic',
              name: 'dynamicPanel',
              title: 'Dynamic Panel',
              templateElements: [
                { name: 'question1', type: 'text', title: 'What is the answer?' }
              ]
            }
          ]
        }
      ]
    }
    const surveyModel = surveyJSModelFromFormContent(formContent)
    surveyModel.data = { dynamicPanel: [{ question1: 'answer' }, { question1: 'answer2' }] }

    const answers = getSurveyJsAnswerList(surveyModel)

    expect(answers).toHaveLength(3)
    expect(answers[0].questionStableId).toEqual('dynamicPanel[0].question1')
    expect(answers[0].stringValue).toEqual('answer')
    expect(answers[1].questionStableId).toEqual('dynamicPanel[1].question1')
    expect(answers[1].stringValue).toEqual('answer2')
    expect(answers[2].questionStableId).toEqual('dynamicPanel')
    expect(answers[2].objectValue).toEqual('[{"question1":"answer"},{"question1":"answer2"}]')
  })
})
