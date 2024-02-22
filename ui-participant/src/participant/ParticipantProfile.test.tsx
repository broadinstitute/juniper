import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import Api, { Profile } from '../api/api'
import { setupRouterTest } from '../test-utils/router-testing-utils'
import ProvideFullTestUserContext from '../test-utils/ProvideFullTestUserContext'
import { ParticipantProfile } from './ParticipantProfile'
import userEvent from '@testing-library/user-event'

const jsalkProfile: Profile = {
  givenName: 'Jonas',
  familyName: 'Salk',
  birthDate: [1987, 11, 12],
  mailingAddress: {
    street1: '415 Main St',
    street2: '',
    city: 'Cambridge',
    state: 'MA',
    country: 'US',
    postalCode: '02119'
  },
  doNotEmailSolicit: false,
  phoneNumber: '123-456-7890',
  contactEmail: 'jsalk@test.com',
  doNotEmail: false,
  sexAtBirth: 'M'
}


test('renders jsalk profile', async () => {
  jest.spyOn(Api, 'findProfile').mockImplementation(
    () => Promise.resolve(jsalkProfile))


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
    >
      <ParticipantProfile/>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  expect(screen.getByText('Jonas Salk')).toBeInTheDocument()
  expect(screen.getByText('11/12/1987')).toBeInTheDocument()
  expect(screen.getByText('415 Main St')).toBeInTheDocument()
  expect(screen.getByText('Cambridge MA 02119')).toBeInTheDocument()
  expect(screen.getByText('US')).toBeInTheDocument()
  expect(screen.getByText('jsalk@test.com')).toBeInTheDocument()
  expect(screen.getByText('On')).toBeInTheDocument()
  expect(screen.getByText('Off')).toBeInTheDocument()
})

test('renders empty profile', async () => {
  jest.spyOn(Api, 'findProfile').mockImplementation(
    () => Promise.resolve({}))


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={{}}
    >
      <ParticipantProfile/>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(async () => expect(screen.getAllByText('Not provided')[0]).toBeInTheDocument())

  expect(screen.getAllByText('Not provided')[0]).toBeInTheDocument()
  expect(screen.queryByText('undefined', { exact: false })).not.toBeInTheDocument()
  expect(screen.queryByText('null', { exact: false })).not.toBeInTheDocument()
  expect(screen.queryByText('NaN', { exact: false })).not.toBeInTheDocument()
})

test('opens expected modals', async () => {
  jest.spyOn(Api, 'findProfile').mockImplementation(
    () => Promise.resolve(jsalkProfile))


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
    >
      <ParticipantProfile/>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  // get all the modal buttons
  const editNameBtn = screen.getByLabelText('Edit Name')
  const editBirthdayBtn = screen.getByLabelText('Edit Birthday')
  const editMailingAddressBtn = screen.getByLabelText('Edit Primary Address')
  const editEmailBtn = screen.getByLabelText('Edit Email')
  const editPhoneBtn = screen.getByLabelText('Edit Phone Number')
  const editNotificationsBtn = screen.getByLabelText('Edit Notifications')
  const editSolicitBtn = screen.getByLabelText('Edit Do Not Solicit')

  // test out opening and closing every modal
  expect(screen.queryByText('Edit Name')).not.toBeInTheDocument()
  await userEvent.click(editNameBtn)
  expect(screen.getByText('Edit Name')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Birthday')).not.toBeInTheDocument()
  await userEvent.click(editBirthdayBtn)
  expect(screen.getByText('Edit Birthday')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Mailing Address')).not.toBeInTheDocument()
  await userEvent.click(editMailingAddressBtn)
  expect(screen.getByText('Edit Mailing Address')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Communication Preferences')).not.toBeInTheDocument()
  await userEvent.click(editEmailBtn)
  expect(screen.getByText('Edit Communication Preferences')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Communication Preferences')).not.toBeInTheDocument()
  await userEvent.click(editPhoneBtn)
  expect(screen.getByText('Edit Communication Preferences')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Communication Preferences')).not.toBeInTheDocument()
  await userEvent.click(editNotificationsBtn)
  expect(screen.getByText('Edit Communication Preferences')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))

  expect(screen.queryByText('Edit Communication Preferences')).not.toBeInTheDocument()
  await userEvent.click(editSolicitBtn)
  expect(screen.getByText('Edit Communication Preferences')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Cancel'))
})

test('updates name properly', async () => {
  jest.spyOn(Api, 'findProfile').mockImplementation(
    () => Promise.resolve(jsalkProfile))

  const updatedProfile: Profile = {
    ...jsalkProfile,
    givenName: 'Test',
    familyName: 'McTester'
  }

  const updateProfileSpy = jest.spyOn(Api, 'updateProfile').mockImplementation(
    () => Promise.resolve(updatedProfile))


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
      ppUser={{ id: 'testppuserid', profile: jsalkProfile, profileId: '' }}
    >
      <ParticipantProfile/>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  await userEvent.click(screen.getByLabelText('Edit Name'))

  await userEvent.clear(screen.getByPlaceholderText('Given Name'))
  await userEvent.type(screen.getByPlaceholderText('Given Name'), 'Test')

  await userEvent.clear(screen.getByPlaceholderText('Family Name'))
  await userEvent.type(screen.getByPlaceholderText('Family Name'), 'McTester')

  await userEvent.click(screen.getByText('Save'))

  await waitFor(() => expect(updateProfileSpy)
    .toHaveBeenCalledWith({
      ppUserId: 'testppuserid',
      profile: updatedProfile
    }))

  expect(screen.getByText('Test McTester')).toBeInTheDocument()
})
