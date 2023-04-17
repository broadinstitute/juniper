import React from 'react'

import {act, render, screen} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {setupRouterTest} from 'test-utils/router-testing-utils'
import {Profile, SurveyJSForm} from 'api/api'

import {extractSurveyContent, getAnswerList, useRoutablePageNumber, useSurveyJSModel} from './surveyJsUtils'
import {Survey as SurveyComponent} from 'survey-react-ui'
import {
  generateSurvey,
  generateTemplatedQuestionSurvey,
  generateThreePageSurvey
} from '../test-utils/test-survey-factory'
import {Model} from 'survey-core'

/** does nothing except render a survey using the hooks from surveyJSUtils */
function PlainSurveyComponent({formModel, profile}: { formModel: SurveyJSForm, profile?: Profile }) {
  const pager = useRoutablePageNumber()
  const {surveyModel} = useSurveyJSModel(formModel, null, () => 1, pager, profile)

  return <div>
    {surveyModel && <SurveyComponent model={surveyModel}/>}
  </div>
}

test('it starts on the first page', () => {
  const {RoutedComponent} = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
})

test('handles page numbers in initial url', () => {
  const {RoutedComponent} = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>,
    ['/foo?page=2'])
  render(RoutedComponent)
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
})

test('updates urls on page navigation', async () => {
  const {RoutedComponent, router} = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  userEvent.click(screen.getByText('Next'))
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
  expect(router.state.location.search).toEqual('?page=2')
  userEvent.click(screen.getByText('Next'))
  expect(screen.getByText('You are on page3')).toBeInTheDocument()
  expect(router.state.location.search).toEqual('?page=3')
  userEvent.click(screen.getByText('Previous'))
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
  expect(router.state.location.search).toEqual('?page=2')

  // check back button works to navigate the survey too
  act(() => {
    router.navigate(-1)
  })
  expect(router.state.location.search).toEqual('?page=3')
  expect(screen.getByText('You are on page3')).toBeInTheDocument()
})

const dynamicSurvey = generateSurvey({
  content: JSON.stringify({
    pages: [
      {
        elements: [
          {type: 'html', html: '<span>You are on page1</span>'},
          {
            type: 'html',
            visibleIf: '{profile.sexAtBirth} = female',
            html: '<span>You have a sex of female</span>'
          }
        ]
      }
    ]
  })
})

test('enables hide on profile attributes', () => {
  const maleProfile: Profile = {sexAtBirth: 'male'}
  const {RoutedComponent} = setupRouterTest(<PlainSurveyComponent formModel={dynamicSurvey}
                                                                  profile={maleProfile}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  const dynamicText = screen.queryByText('You have a sex of female')
  expect(dynamicText).toBeNull()
})

test('enables show on profile attributes', () => {
  const femaleProfile: Profile = {sexAtBirth: 'female'}
  const {RoutedComponent} = setupRouterTest(<PlainSurveyComponent formModel={dynamicSurvey}
                                                                  profile={femaleProfile}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  expect(screen.getByText('You have a sex of female')).toBeInTheDocument()
})


test('transforms models with question templates', () => {
  const formModel = generateTemplatedQuestionSurvey()
  const surveyModel = extractSurveyContent(formModel)
  expect(surveyModel.pages).toHaveLength(2)
  const firstTemplatedQuestion = surveyModel.pages[0].elements[1]
  expect(firstTemplatedQuestion.name).toEqual('brotherFavoriteColor')
  expect(firstTemplatedQuestion.title).toEqual('what is their favorite color?')
  expect(firstTemplatedQuestion.choices).toHaveLength(2)
  const secondTemplatedQuestion = surveyModel.pages[1].elements[1]
  expect(secondTemplatedQuestion.name).toEqual('sisterFavoriteColor')
})

const sampleSurvey = {
  pages: [{
    elements: [{
      name: 'radioQ',
      type: 'radiogroup',
      choices: [{text: 'A', value: 'a'}, {text: 'B', value: 'b'}]
    }, {
      name: 'textQ',
      type: 'text'
    }, {
      name: 'numberQ',
      type: 'dropdown',
      choices: [{text: '35', value: 35}, {text: '40', value: 40}]
    }, {
      name: 'checkboxQ',
      type: 'checkbox',
      choices: [{text: 'X', value: 'x'}, {text: 'Y', value: 'y'}]
    }]
  }],
  calculatedValues: [
    {
      name: "qualified",
      expression: "{radioQ} = 'b'",
      "includeIntoResult": true
    }
  ]
}

test('gets text answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = {'textQ': 'some text'}
  const answers = getAnswerList(model)
  expect(answers).toHaveLength(2)
  expect(answers).toContainEqual({questionStableId: 'textQ', stringValue: 'some text'})
  expect(answers).toContainEqual({questionStableId: 'qualified', booleanValue: false})
})

test('gets choice answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = {'radioQ': 'b'}
  const answers = getAnswerList(model)
  expect(answers).toHaveLength(2)
  expect(answers).toContainEqual({questionStableId: 'radioQ', stringValue: 'b'})
  expect(answers).toContainEqual({questionStableId: 'qualified', booleanValue: true})
})

test('gets numeric answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = {'numberQ': 40}
  const answers = getAnswerList(model)
  expect(answers).toContainEqual({questionStableId: 'numberQ', numberValue: 40})
})



