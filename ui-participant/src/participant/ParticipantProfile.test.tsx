import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import Api, { PortalParticipantUser } from '../api/api'
import ProvideFullTestUserContext from '../test-utils/ProvideFullTestUserContext'
import { ParticipantProfile } from './ParticipantProfile'
import { userEvent } from '@testing-library/user-event'
import { asMockedFn, Enrollee, MockI18nProvider, Profile, setupRouterTest } from '@juniper/ui-core'
import { useParams } from 'react-router-dom'

const jsalkProfile: Profile = {
  id: '1234',
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

const sallyProfile: Profile = {
  id: '5678',
  givenName: 'Sally',
  familyName: 'Salk',
  birthDate: [1990, 1, 1],
  mailingAddress: {
    street1: '123 Main St',
    street2: '',
    city: 'Boston',
    state: 'MA',
    country: 'US',
    postalCode: '02119'
  },
  doNotEmailSolicit: false,
  phoneNumber: '123-456-7890',
  contactEmail: ''
}

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn()
}))

jest.mock('mixpanel-browser')


test('renders jsalk profile', async () => {
  asMockedFn(useParams).mockReturnValue({})
  jest.spyOn(Api, 'findProfile').mockResolvedValue(jsalkProfile)

  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
    >
      <MockI18nProvider>
        <ParticipantProfile/>
      </MockI18nProvider>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  expect(screen.getByText('Jonas Salk')).toBeInTheDocument()
  expect(screen.getByText('11/12/1987')).toBeInTheDocument()
  expect(screen.getByText('415 Main St')).toBeInTheDocument()
  expect(screen.getByText('Cambridge MA 02119')).toBeInTheDocument()
  expect(screen.getByText('US')).toBeInTheDocument()
  expect(screen.getByText('jsalk@test.com')).toBeInTheDocument()
  expect(screen.getByText('{on}')).toBeInTheDocument()
  expect(screen.getByText('{off}')).toBeInTheDocument()
})

test('renders empty profile', async () => {
  asMockedFn(useParams).mockReturnValue({})

  jest.spyOn(Api, 'findProfile').mockResolvedValue({})


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={{}}
    >
      <MockI18nProvider>
        <ParticipantProfile/>
      </MockI18nProvider>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(async () => expect(screen.getAllByText('{notProvided}')[0]).toBeInTheDocument())

  expect(screen.getAllByText('{notProvided}')[0]).toBeInTheDocument()
  expect(screen.queryByText('undefined', { exact: false })).not.toBeInTheDocument()
  expect(screen.queryByText('null', { exact: false })).not.toBeInTheDocument()
  expect(screen.queryByText('NaN', { exact: false })).not.toBeInTheDocument()
})

test('opens expected modals', async () => {
  asMockedFn(useParams).mockReturnValue({})

  jest.spyOn(Api, 'findProfile').mockResolvedValue(jsalkProfile)


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
    >
      <MockI18nProvider>
        <ParticipantProfile/>
      </MockI18nProvider>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  // get all the modal buttons
  const editNameBtn = screen.getByLabelText('{editName}')
  const editBirthDateBtn = screen.getByLabelText('{editBirthDate}')
  const editMailingAddressBtn = screen.getByLabelText('{editPrimaryAddress}')
  const editEmailBtn = screen.getByLabelText('{editContactEmail}')
  const editPhoneBtn = screen.getByLabelText('{editPhoneNumber}')
  const editNotificationsBtn = screen.getByLabelText('{editNotifications}')
  const editSolicitBtn = screen.getByLabelText('{editDoNotSolicit}')

  // test out opening and closing every modal
  expect(screen.queryByText('{editName}')).not.toBeInTheDocument()
  await userEvent.click(editNameBtn)
  expect(screen.getByText('{editName}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editBirthDate}')).not.toBeInTheDocument()
  await userEvent.click(editBirthDateBtn)
  expect(screen.getByText('{editBirthDate}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editMailingAddress}')).not.toBeInTheDocument()
  await userEvent.click(editMailingAddressBtn)
  expect(screen.getByText('{editMailingAddress}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editContactEmail}')).not.toBeInTheDocument()
  await userEvent.click(editEmailBtn)
  expect(screen.getByText('{editContactEmail}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editPhoneNumber}')).not.toBeInTheDocument()
  await userEvent.click(editPhoneBtn)
  expect(screen.getByText('{editPhoneNumber}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editCommunicationPreferences}')).not.toBeInTheDocument()
  await userEvent.click(editNotificationsBtn)
  expect(screen.getByText('{editCommunicationPreferences}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))

  expect(screen.queryByText('{editCommunicationPreferences}')).not.toBeInTheDocument()
  await userEvent.click(editSolicitBtn)
  expect(screen.getByText('{editCommunicationPreferences}')).toBeInTheDocument()
  await userEvent.click(screen.getByText('{cancel}'))
})

test('updates name properly', async () => {
  asMockedFn(useParams).mockReturnValue({})

  jest.spyOn(Api, 'findProfile').mockResolvedValue(jsalkProfile)

  const updatedProfile: Profile = {
    ...jsalkProfile,
    givenName: 'Test',
    familyName: 'McTester'
  }

  const updateProfileSpy = jest.spyOn(Api, 'updateProfile').mockResolvedValue(updatedProfile)

  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      profile={jsalkProfile}
      ppUsers={[{
        id: 'testppuserid',
        profile: jsalkProfile,
        profileId: jsalkProfile.id || '',
        participantUserId: ''
      }]}
    >
      <MockI18nProvider>
        <ParticipantProfile/>
      </MockI18nProvider>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Jonas Salk')).toBeInTheDocument())

  await userEvent.click(screen.getByLabelText('{editName}'))

  await userEvent.clear(screen.getByPlaceholderText('{givenName}'))
  await userEvent.type(screen.getByPlaceholderText('{givenName}'), 'Test')

  await userEvent.clear(screen.getByPlaceholderText('{familyName}'))
  await userEvent.type(screen.getByPlaceholderText('{familyName}'), 'McTester')

  await userEvent.click(screen.getByText('{save}'))

  await waitFor(() => expect(updateProfileSpy)
    .toHaveBeenCalledWith({
      ppUserId: 'testppuserid',
      profile: updatedProfile
    }))

  expect(screen.getByText('Test McTester')).toBeInTheDocument()
  // should not have a link to all profiles if not proxied
  expect(screen.queryByText('{allProfiles}')).not.toBeInTheDocument()
})

test('shows correct profile in proxied environment', async () => {
  asMockedFn(useParams).mockReturnValue({
    ppUserId: 'sallyUser'
  })

  const ppusers: PortalParticipantUser[] = [
    {
      id: 'jsalkuser',
      profileId: '1234',
      participantUserId: 'jsalk',
      profile: jsalkProfile
    },
    {
      id: 'sallyUser',
      profileId: '5678',
      participantUserId: 'sally',
      profile: sallyProfile
    }
  ]
  const enrollees: Enrollee[] = [
    {
      id: 'testjsalkenrollee',
      profileId: '1234',
      profile: jsalkProfile,
      consented: true,
      subject: false,
      createdAt: 0,
      kitRequests: [],
      lastUpdatedAt: 0,
      participantTasks: [],
      participantNotes: [],
      participantUserId: 'testjsalkuser',
      shortcode: 'ABCD',
      studyEnvironmentId: 'asdf',
      surveyResponses: []
    },
    {
      id: 'testsallyenrollee',
      profileId: '5678',
      profile: sallyProfile,
      consented: true,
      subject: false,
      participantNotes: [],
      createdAt: 0,
      kitRequests: [],
      lastUpdatedAt: 0,
      participantTasks: [],
      participantUserId: 'testjsalkuser',
      shortcode: 'ABCD',
      studyEnvironmentId: 'asdf',
      surveyResponses: []
    }
  ]

  jest.spyOn(Api, 'findProfile').mockResolvedValue(sallyProfile)


  const { RoutedComponent } = setupRouterTest(
    <ProvideFullTestUserContext
      ppUsers={ppusers}
      enrollees={enrollees}
    >
      <MockI18nProvider>
        <ParticipantProfile/>
      </MockI18nProvider>
    </ProvideFullTestUserContext>)
  render(RoutedComponent)
  // should show sally's profile

  await waitFor(() => expect(screen.getByText('Sally Salk')).toBeInTheDocument())

  expect(screen.getByText('Sally Salk')).toBeInTheDocument()
  expect(screen.getByText('{allProfiles}')).toBeInTheDocument() // should be a link to all profiles if proxied
})
