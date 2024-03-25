import { mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { alertDefaults } from '@juniper/ui-core'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import DashboardSettings, { AlertEditorView, AlertPreview } from './DashboardSettings'
import Api from 'api/api'
import userEvent from '@testing-library/user-event'

test('DashboardSettings renders a list of AlertEditorViews', async () => {
  //Arrange
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()
  const noActivitiesAlert = alertDefaults['NO_ACTIVITIES_REMAIN']
  const welcomeAlert = alertDefaults['WELCOME']

  jest.spyOn(Api, 'listPortalEnvAlerts').mockResolvedValue([noActivitiesAlert, welcomeAlert])

  render(<DashboardSettings portalContext={portalContext} studyEnvContext={studyEnvContext}/>)

  //Assert
  await waitFor(() => expect(screen.queryByText(welcomeAlert.title)).toBeInTheDocument())
  await waitFor(() => expect(screen.queryByText(noActivitiesAlert.title)).toBeInTheDocument())
})

test('AlertEditor updates the alert configuration', async () => {
  //Arrange
  const alert = alertDefaults['NO_ACTIVITIES_REMAIN']

  const updateAlert = jest.fn()
  render(<AlertEditorView initial={alert} isReadOnly={false} updateAlert={updateAlert} onSave={jest.fn}/>)

  //Act
  const detailInput = screen.getByLabelText('Detail')
  await userEvent.clear(detailInput)
  await userEvent.click(screen.getByText('Save'))

  //Assert
  expect(updateAlert).toHaveBeenCalledWith({ ...alert, detail: '' })
})

test('AlertEditor hides save button in readOnly mode', async () => {
  //Arrange
  const alert = alertDefaults['NO_ACTIVITIES_REMAIN']

  const updateAlert = jest.fn()
  render(<AlertEditorView initial={alert} isReadOnly={true} updateAlert={updateAlert} onSave={jest.fn}/>)

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
  const alert = alertDefaults['NO_ACTIVITIES_REMAIN']

  render(<AlertPreview alert={alert}/>)

  //Assert
  expect(screen.getByText(alert.title)).toBeInTheDocument()
  expect(screen.getByText(alert.detail!)).toBeInTheDocument()
})
