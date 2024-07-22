import React from 'react'

import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { Survey } from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import { generateSurvey, generateThreePageSurvey } from '../test-utils/test-survey-factory'
import { Model } from 'survey-core'
import { usePortalEnv } from 'providers/PortalProvider'
import {
  asMockedFn, getSurveyJsAnswerList, getUpdatedAnswers,
  MockI18nProvider,
  Profile,
  setupRouterTest,
  useRoutablePageNumber,
  useSurveyJSModel
} from '@juniper/ui-core'
import { mockUsePortalEnv } from '../test-utils/test-portal-factory'
import { useUser } from '../providers/UserProvider'
import { mockUseActiveUser, mockUseUser } from '../test-utils/user-mocking-utils'
import { useActiveUser } from '../providers/ActiveUserProvider'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))
jest.mock('providers/UserProvider')
jest.mock('providers/ActiveUserProvider')

beforeEach(() => {
  asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
})

/** does nothing except render a survey using the hooks from surveyJsUtils */
function PlainSurveyComponent({ formModel, profile }: { formModel: Survey, profile?: Profile }) {
  const pager = useRoutablePageNumber()
  const { surveyModel } = useSurveyJSModel(formModel, null, () => 1, pager, 'sandbox', profile)

  return <div>
    {surveyModel && <SurveyComponent model={surveyModel}/>}
  </div>
}

test('it starts on the first page', () => {
  asMockedFn(useUser).mockReturnValue(mockUseUser(false))
  asMockedFn(useActiveUser).mockReturnValue(mockUseActiveUser())

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <PlainSurveyComponent formModel={generateThreePageSurvey()}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
})

test('handles page numbers in initial url', () => {
  asMockedFn(useUser).mockReturnValue(mockUseUser(false))
  asMockedFn(useActiveUser).mockReturnValue(mockUseActiveUser())

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <PlainSurveyComponent formModel={generateThreePageSurvey()}/>
    </MockI18nProvider>,
    ['/foo?page=2'])
  render(RoutedComponent)
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
})

test('updates urls on page navigation', async () => {
  asMockedFn(useUser).mockReturnValue(mockUseUser(false))
  asMockedFn(useActiveUser).mockReturnValue(mockUseActiveUser())

  const user = userEvent.setup()
  const { RoutedComponent, router } = setupRouterTest(
    <MockI18nProvider>
      <PlainSurveyComponent formModel={generateThreePageSurvey()}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  await act(() => user.click(screen.getByText('Next')))
  expect(screen.getByText('You are on page2')).toBeInTheDocument()
  expect(router.state.location.search).toEqual('?page=2')
  await act(() => user.click(screen.getByText('Next')))
  expect(screen.getByText('You are on page3')).toBeInTheDocument()
  expect(router.state.location.search).toEqual('?page=3')
  await act(() => user.click(screen.getByText('Previous')))
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
          { type: 'html', html: '<span>You are on page1</span>' },
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
  const maleProfile: Profile = { sexAtBirth: 'male' }
  asMockedFn(useUser).mockReturnValue(mockUseUser(false))
  asMockedFn(useActiveUser).mockReturnValue({
    ...mockUseActiveUser(),
    profile: maleProfile
  })

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <PlainSurveyComponent formModel={dynamicSurvey} profile={maleProfile}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  const dynamicText = screen.queryByText('You have a sex of female')
  expect(dynamicText).toBeNull()
})

test('enables show on profile attributes', () => {
  const femaleProfile: Profile = { sexAtBirth: 'female' }
  asMockedFn(useUser).mockReturnValue(mockUseUser(false))
  asMockedFn(useActiveUser).mockReturnValue({
    ...mockUseActiveUser(),
    profile: femaleProfile
  })

  const { RoutedComponent } = setupRouterTest(
    <MockI18nProvider>
      <PlainSurveyComponent formModel={dynamicSurvey} profile={femaleProfile}/>
    </MockI18nProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  expect(screen.getByText('You have a sex of female')).toBeInTheDocument()
})

const sampleSurvey = {
  pages: [{
    elements: [{
      name: 'radioQ',
      type: 'radiogroup',
      choices: [{ text: 'A', value: 'a' }, { text: 'B', value: 'b' }]
    }, {
      name: 'textQ',
      type: 'text'
    }, {
      name: 'numberQ',
      type: 'dropdown',
      choices: [{ text: '35', value: 35 }, { text: '40', value: 40 }]
    }, {
      name: 'checkboxQ',
      type: 'checkbox',
      choices: [{ text: 'X', value: 'x' }, { text: 'Y', value: 'y' }]
    }]
  }],
  calculatedValues: [
    {
      name: 'qualified',
      expression: '{radioQ} = \'b\'',
      'includeIntoResult': true
    }
  ]
}

test('gets text answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = { 'textQ': 'some text' }
  const answers = getSurveyJsAnswerList(model)
  expect(answers).toHaveLength(2)
  expect(answers).toContainEqual({ questionStableId: 'textQ', stringValue: 'some text' })
  expect(answers).toContainEqual({ questionStableId: 'qualified', booleanValue: false })
})

test('gets choice answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = { 'radioQ': 'b' }
  const answers = getSurveyJsAnswerList(model)
  expect(answers).toHaveLength(2)
  expect(answers).toContainEqual({ questionStableId: 'radioQ', stringValue: 'b' })
  expect(answers).toContainEqual({ questionStableId: 'qualified', booleanValue: true })
})

test('gets numeric answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = { 'numberQ': 40 }
  const answers = getSurveyJsAnswerList(model)
  expect(answers).toContainEqual({ questionStableId: 'numberQ', numberValue: 40 })
})

test('gets computed values from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = { 'radioQ': 'b' }
  const answers = getSurveyJsAnswerList(model)
  expect(answers).toContainEqual({
    questionStableId: 'qualified',
    booleanValue: true
  })
})

test('gets checkbox answers from survey model', () => {
  const model = new Model(sampleSurvey)
  model.data = { 'checkboxQ': ['x', 'y'] }
  const answers = getSurveyJsAnswerList(model)
  expect(answers).toContainEqual({
    questionStableId: 'checkboxQ',
    objectValue: JSON.stringify(['x', 'y'])
  })
})

test('testGetUpdatedAnswersEmpty', () => {
  expect(getUpdatedAnswers({}, {})).toEqual([])
})

test('testGetUpdatedAnswersRemovedValue', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar' }, {})
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo' }])
})

test('testGetUpdatedAnswersStringNew', () => {
  const updatedAnswers = getUpdatedAnswers({}, { 'foo': 'bar' })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', stringValue: 'bar' }])
})


test('testGetUpdatedAnswersStringUnchanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar' }, { 'foo': 'bar' })
  expect(updatedAnswers).toEqual([])
})


test('testGetUpdatedAnswersStringUpdated', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar' }, { 'foo': 'baz' })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', stringValue: 'baz' }])
})

test('testGetUpdatedAnswersOtherAdded', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar' }, { 'foo': 'baz', 'foo-Comment': 'blah' })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', stringValue: 'baz', otherDescription: 'blah' }])
})

test('testGetUpdatedAnswersOtherRemoved', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar', 'foo-Comment': 'blah' }, { 'foo': 'baz' })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', stringValue: 'baz' }])
})

test('testGetUpdatedAnswersOtherChanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 'bar', 'foo-Comment': 'blah' },
    { 'foo': 'baz', 'foo-Comment': 'blah2' })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', stringValue: 'baz', otherDescription: 'blah2' }])
})

test('testGetUpdatedAnswersBooleanNew', () => {
  const updatedAnswers = getUpdatedAnswers({}, { 'foo': false })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', booleanValue: false }])
})

test('testGetUpdatedAnswersBooleanUnchanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': false }, { 'foo': false })
  expect(updatedAnswers).toEqual([])
})

test('testGetUpdatedAnswersBooleanChanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': true }, { 'foo': false })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', booleanValue: false }])
})

test('testGetUpdatedAnswersObjectNew', () => {
  const updatedAnswers = getUpdatedAnswers({}, { 'foo': ['bleck'] })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', objectValue: JSON.stringify(['bleck']) }])
})

test('testGetUpdatedAnswersObjectChanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': ['blah'] }, { 'foo': ['bleck'] })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', objectValue: JSON.stringify(['bleck']) }])
})

test('testGetUpdatedAnswersObjectUnchanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': ['blah'] }, { 'foo': ['blah'] })
  expect(updatedAnswers).toEqual([])
})

test('testGetUpdatedAnswersNumberNew', () => {
  const updatedAnswers = getUpdatedAnswers({}, { 'foo': 2 })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', numberValue: 2 }])
})

test('testGetUpdatedAnswersNumberNewZero', () => {
  const updatedAnswers = getUpdatedAnswers({}, { 'foo': 0 })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', numberValue: 0 }])
})

test('testGetUpdatedAnswersNumberChanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 2 }, { 'foo': 3 })
  expect(updatedAnswers).toEqual([{ questionStableId: 'foo', numberValue: 3 }])
})

test('testGetUpdatedAnswersNumberUnchanged', () => {
  const updatedAnswers = getUpdatedAnswers({ 'foo': 4 }, { 'foo': 4 })
  expect(updatedAnswers).toEqual([])
})
