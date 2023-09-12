import { StudyEnvironmentSurvey, Survey } from 'api/api'

/** simplest survey.  one page, no interactive elements */
export function generateSurvey(overrideObj?: any): Survey { // eslint-disable-line @typescript-eslint/no-explicit-any
  const surveyContent = {
    pages: [
      {
        elements: [
          { type: 'html', html: '<span>You are on page1</span>' }
        ]
      }
    ]
  }
  const survey = {
    content: JSON.stringify(surveyContent),
    stableId: '3pageSurvey',
    version: 1
  }
  return Object.assign(survey, overrideObj)
}

/** three page survey, question on first page, static text on the rest */
export function generateThreePageSurvey(overrideObj?: any): Survey { // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
  const surveyContent = {
    pages: [
      {
        elements: [
          { type: 'html', html: '<span>You are on page1</span>' },
          {
            type: 'radiogroup', title: 'radio input', name: 'radio1',
            choices: [{ text: 'Green', value: 'green' }, { text: 'Blue', value: 'blue' }]
          }
        ]
      },
      {
        elements: [
          { type: 'html', html: '<span>You are on page2</span>' },
          {
            type: 'text', title: 'text input', name: 'text1'
          }
        ]
      },
      {
        elements: [
          { type: 'html', html: '<span>You are on page3</span>' }
        ]
      }
    ]
  }
  const survey = generateSurvey({ content: JSON.stringify(surveyContent) })
  return Object.assign(survey, overrideObj)
}

/** survey with a hidden question -- uses surveyjs default clear-on-submit behavior */
export function mockSurveyWithHiddenQuestion(): Survey {
  const surveyContent =  {
    pages: [{
      elements: [
        { type: 'html', html: '<span>You are on page1</span>' },
        {
          type: 'radiogroup', title: 'radio input', name: 'radio1',
          choices: [{ text: 'Green', value: 'green' }, { text: 'Blue', value: 'blue' }]
        },
        {
          type: 'radiogroup', title: 'green follower', name: 'greenFollow',
          choices: [{ text: 'light green', value: 'lightGreen' }, { text: 'forest green', value: 'forest' }],
          visibleIf: '{radio1} = "green"'
        }
      ]
    }]
  }
  return generateSurvey({ content: JSON.stringify(surveyContent) })
}

/** survey with a hidden question and hidden questions set to clear values as soon as they are invisible */
export function mockSurveyWithHiddenQuestionClearOnHidden(): Survey {
  const surveyContent =  {
    'clearInvisibleValues': 'onHiddenContainer',
    pages: [{
      elements: [
        { type: 'html', html: '<span>You are on page1</span>' },
        {
          type: 'radiogroup', title: 'radio input', name: 'radio1',
          choices: [{ text: 'Green', value: 'green' }, { text: 'Blue', value: 'blue' }]
        },
        {
          type: 'radiogroup', title: 'green follower', name: 'greenFollow',
          choices: [{ text: 'light green', value: 'lightGreen' }, { text: 'forest green', value: 'forest' }],
          visibleIf: '{radio1} = "green"'
        }
      ]
    }]
  }
  return generateSurvey({ content: JSON.stringify(surveyContent) })
}


/** mock StudyEnvironmentSurvey object */
export const mockConfiguredSurvey = (): StudyEnvironmentSurvey => {
  return {
    id: 'fakeGuid',
    surveyId: 'surveyId1',
    surveyOrder: 1,
    required: false,
    recur: false,
    recurrenceIntervalDays: 0,
    allowAdminEdit: true,
    allowParticipantStart: true,
    allowParticipantReedit: true,
    prepopulate: true,
    survey: generateSurvey()
  }
}
