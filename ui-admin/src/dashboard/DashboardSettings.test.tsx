import { mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { alertDefaults } from '@juniper/ui-core'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import DashboardSettings, {AlertEditor, AlertEditorView} from './DashboardSettings'
import Api from 'api/api'
import userEvent from '@testing-library/user-event'

test('DashboardSettings renders an alert', async () => {
  //Arrange
  const portalContext = mockPortalContext()
  const studyEnvContext = mockStudyEnvContext()

  jest.spyOn(Api, 'listPortalEnvAlerts').mockImplementation(() => Promise.resolve([alert]))

  const alert = alertDefaults['NO_ACTIVITIES_REMAIN']
  render(<DashboardSettings portalContext={portalContext} studyEnvContext={studyEnvContext}/>)

  //Assert
  await waitFor(() => expect(screen.getAllByText(alert.title)).toHaveLength(1))
  await waitFor(() => expect(screen.getAllByText(alert.detail!)).toHaveLength(2))
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

test('AlertPreview renders an alert', async () => {
  //Arrange
  const alert = alertDefaults['NO_ACTIVITIES_REMAIN']

  render(<AlertEditorView initial={alert} isReadOnly={false} updateAlert={jest.fn()} onSave={jest.fn}/>)

  //Assert
  await waitFor(() => expect(screen.getByText(alert.title)).toBeInTheDocument())
  await waitFor(() => expect(screen.getByText(alert.detail!)).toBeInTheDocument())
})
