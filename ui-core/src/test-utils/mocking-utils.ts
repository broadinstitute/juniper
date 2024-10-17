import {
  Question,
  Survey,
  SurveyResponse
} from 'src/types/forms'
import {
  Enrollee,
  HubResponse,
  Profile
} from 'src/types/user'
import { StudyEnvironmentSurvey } from 'src/types/study'

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
            type: 'text', title: 'text input', name: 'text1', placeholder: 'text placeholder'
          }
        ]
      },
      {
        elements: [
          { type: 'html', html: '<span>You are on page3</span>' }
        ]
      }
    ],
    'calculatedValues': [
      {
        'name': 'colorCode',
        'expression': 'iif({radio1} = \'green\', \'#0F0\', \'#00F\')',
        'includeIntoResult': true
      }
    ]
  }

  const survey = generateSurvey({ content: JSON.stringify(surveyContent) })
  return Object.assign(survey, overrideObj)
}

export function generateSurveyWithQuestion(q: Question): Survey {
  const surveyContent = {
    pages: [
      {
        elements: [
          { type: 'html', html: '<span>You are on page1</span>' },
          q
        ]
      }
    ],
    'calculatedValues': []
  }

  return generateSurvey({ content: JSON.stringify(surveyContent) })
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


/**
 *
 */
export const mockEnrollee: () => Enrollee = () => {
  return {
    shortcode: 'AAABBB',
    consented: true,
    subject: true,
    id: 'enrollee1',
    participantUserId: 'user1',
    profile: {
      sexAtBirth: 'female'
    },
    profileId: 'profile1',
    kitRequests: [],
    surveyResponses: [],
    consentResponses: [],
    preRegResponse: undefined,
    preEnrollmentResponse: undefined,
    participantTasks: [],
    participantNotes: [],
    createdAt: 0,
    lastUpdatedAt: 0,
    studyEnvironmentId: 'studyEnv1'
  }
}

/**
 *
 */
export const mockConfiguredSurvey = (): StudyEnvironmentSurvey => {
  return {
    id: 'fakeGuid',
    surveyId: 'surveyId1',
    surveyOrder: 1,
    survey: generateSurvey()
  }
}

/** mock hub response including no tasks and a mock enrollee */
export const mockHubResponse = (): HubResponse => {
  return {
    enrollee: mockEnrollee(),
    tasks: [],
    response: mockSurveyResponse(),
    profile: mockProfile()
  }
}

/** mock response */
export const mockSurveyResponse = (): SurveyResponse => {
  return {
    id: 'responseId1',
    surveyId: 'survey1',
    resumeData: '{}',
    enrolleeId: 'enrollee1',
    complete: false,
    answers: []
  }
}

/** mock enrollee profile */
export const mockProfile = (): Profile => {
  return {
    sexAtBirth: 'female'
  }
}
