import React  from 'react'

import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { SurveyJSForm } from 'api/api'

import { extractSurveyContent, useRoutablePageNumber, useSurveyJSModel } from './surveyJsUtils'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { generateTemplatedQuestionSurvey, generateThreePageSurvey } from '../test-utils/test-survey-factory'

/** does nothing except render a survey using the hooks from surveyJSUtils */
function PlainSurveyComponent({ formModel }: {formModel: SurveyJSForm}) {
  const pager = useRoutablePageNumber()
  const { surveyModel } = useSurveyJSModel(formModel, null, () => 1, pager)

  return <div>
    {surveyModel && <SurveyComponent model={surveyModel}/> }
  </div>
}

test('it starts on the first page', () => {
  const { RoutedComponent } = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
})

test('handles page numbers in initial url', () => {
  const { RoutedComponent } = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>,
    ['/foo?page=2'])
  render(RoutedComponent)
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
})

test('updates urls on page navigation', async () => {
  const { RoutedComponent, router } = setupRouterTest(<PlainSurveyComponent formModel={generateThreePageSurvey()}/>)
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


