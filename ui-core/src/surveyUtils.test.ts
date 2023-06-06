import { extractSurveyContent } from './surveyUtils'
import { RadiogroupQuestion, VersionedForm } from './types/forms'

describe('extractSurveyContent', () => {
  it('transforms models with question templates', () => {
    const survey = {
      stableId: 'testSurvey',
      version: 1,
      content: JSON.stringify({
        questionTemplates: [
          {
            type: 'radiogroup', title: 'what is their favorite color?', name: 'colorPicker',
            choices: [{ text: 'Green', value: 'green' }, { text: 'Blue', value: 'blue' }]
          }
        ],
        pages: [
          {
            elements: [
              { type: 'html', html: '<span>Talk about your brother</span>' },
              { name: 'brotherFavoriteColor', questionTemplateName: 'colorPicker' }
            ]
          },
          {
            elements: [
              { type: 'html', html: '<span>Talk about your sister</span>' },
              { name: 'sisterFavoriteColor', questionTemplateName: 'colorPicker' }
            ]
          }
        ]
      })
    }

    const surveyModel = extractSurveyContent(survey as VersionedForm)
    expect(surveyModel.pages).toHaveLength(2)
    const firstTemplatedQuestion = surveyModel.pages[0].elements[1] as RadiogroupQuestion
    expect(firstTemplatedQuestion.name).toEqual('brotherFavoriteColor')
    expect(firstTemplatedQuestion.title).toEqual('what is their favorite color?')
    expect(firstTemplatedQuestion.choices).toHaveLength(2)
    const secondTemplatedQuestion = surveyModel.pages[1].elements[1] as RadiogroupQuestion
    expect(secondTemplatedQuestion.name).toEqual('sisterFavoriteColor')
  })
})
