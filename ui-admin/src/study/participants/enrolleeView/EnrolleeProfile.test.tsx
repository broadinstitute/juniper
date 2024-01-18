import React from 'react'

import EnrolleeProfile from './EnrolleeProfile'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, Screen, screen } from '@testing-library/react'
import { dateToUSLocaleString } from '../../../util/timeUtils'


const getInputFromLabel = (screen: Screen, label: string) : HTMLInputElement => {
  const val = screen.getByText(label, { exact: false })

  const inputElement = val.querySelector<HTMLInputElement>('input')

  if (inputElement === null) {
    throw new Error(`could not find input with label ${ label}`)
  }

  return inputElement
}

test('renders enrollee profile', async () => {
  jest.spyOn(window, 'alert').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const enrollee = mockEnrollee()

  const { RoutedComponent } = setupRouterTest(
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    <EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={() => {}}/>)
  render(RoutedComponent)


  expect(screen.getByDisplayValue(enrollee.profile.givenName)).toBeInTheDocument()
  expect(screen.getByDisplayValue(enrollee.profile.familyName)).toBeInTheDocument()
  expect(screen.getByDisplayValue(dateToUSLocaleString(enrollee.profile.birthDate))).toBeInTheDocument()
  expect(screen.getByDisplayValue(enrollee.profile.mailingAddress.city)).toBeInTheDocument()
  expect(screen.getByDisplayValue(enrollee.profile.mailingAddress.street1)).toBeInTheDocument()
  expect(screen.getByDisplayValue(enrollee.profile.mailingAddress.postalCode)).toBeInTheDocument()
  expect(screen.getByDisplayValue(enrollee.profile.mailingAddress.state)).toBeInTheDocument()

  getInputFromLabel(screen, 'Family name')
})
