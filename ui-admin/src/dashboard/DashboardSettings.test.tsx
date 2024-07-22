import {
  mockDashboardAlert,
  mockPortalContext,
  mockPortalEnvironment
} from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import DashboardSettings, { AlertEditorView, AlertPreview } from './DashboardSettings'
import Api from 'api/api'
import { userEvent } from '@testing-library/user-event'
import { MockI18nProvider } from '@juniper/ui-core'

test('DashboardSettings renders a list of AlertEditorViews', async () => {
  //Arrange
  const portalContext = mockPortalContext()
  const noActivitiesAlert = mockDashboardAlert('No Activities left', 'there are none', 'NO_ACTIVITIES_REMAIN')
  const welcomeAlert = mockDashboardAlert('Welcome!', 'welcome to the study', 'WELCOME')

  jest.spyOn(Api, 'listPortalEnvAlerts').mockResolvedValue([noActivitiesAlert, welcomeAlert])

  render(<MockI18nProvider>
    <DashboardSettings portalContext={portalContext} currentEnv={mockPortalEnvironment('sandbox')}/>
  </MockI18nProvider>)

  //Assert
  await waitFor(() => expect(screen.queryByText(welcomeAlert.title)).toBeInTheDocument())
  await waitFor(() => expect(screen.queryByText(noActivitiesAlert.title)).toBeInTheDocument())
})

test('AlertEditor updates the alert configuration', async () => {
  //Arrange
  const noActivitiesAlert = mockDashboardAlert('No Activities left', 'there are none', 'NO_ACTIVITIES_REMAIN')

  const updateAlert = jest.fn()
  render(<AlertEditorView initial={noActivitiesAlert} isReadOnly={false} updateAlert={updateAlert} onSave={jest.fn}/>)

  //Act
  const detailInput = screen.getByLabelText('Detail')
  await userEvent.clear(detailInput)
  await userEvent.click(screen.getByText('Save'))

  //Assert
  expect(updateAlert).toHaveBeenCalledWith({ ...noActivitiesAlert, detail: '' })
})

test('AlertEditor hides save button in readOnly mode', async () => {
  //Arrange
  const noActivitiesAlert = mockDashboardAlert('No Activities left', 'there are none', 'NO_ACTIVITIES_REMAIN')

  const updateAlert = jest.fn()
  render(<AlertEditorView initial={noActivitiesAlert} isReadOnly={true} updateAlert={updateAlert} onSave={jest.fn}/>)

  //Act
  const titleInput = screen.getByLabelText('Title')
  const typeInput = screen.getByLabelText('Type')
  const detailInput = screen.getByLabelText('Detail')

  //Assert
  expect(titleInput).toBeDisabled()
  expect(typeInput).toBeDisabled()
  expect(detailInput).toBeDisabled()
  expect(screen.queryByText('Save')).not.toBeInTheDocument()
})

test('AlertPreview renders an alert', async () => {
  //Arrange
  const noActivitiesAlert = mockDashboardAlert('No Activities left', 'there are none', 'NO_ACTIVITIES_REMAIN')

  render(<AlertPreview alert={noActivitiesAlert}/>)

  //Assert
  expect(screen.getByText(noActivitiesAlert.title)).toBeInTheDocument()
  expect(screen.getByText(noActivitiesAlert.detail!)).toBeInTheDocument()
})
