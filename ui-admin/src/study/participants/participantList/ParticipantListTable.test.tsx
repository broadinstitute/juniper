import { setupRouterTest } from '@juniper/ui-core'
import {
  mockEnrolleeSearchExpressionResult,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import {
  act,
  render,
  screen,
  waitFor
} from '@testing-library/react'
import React from 'react'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { userEvent } from '@testing-library/user-event'


describe('Participant List', () => {
  test('should be able to download participant list', async () => {
    const { RoutedComponent } = setupRouterTest(<ParticipantListTable
      studyEnvContext={mockStudyEnvContext()}
      participantList={[
        mockEnrolleeSearchExpressionResult()
      ]}
      reload={jest.fn()}
    />)

    render(RoutedComponent)

    await act(async () => {
      await userEvent.click(await screen.findByLabelText('Download table'))
    })

    await waitFor(async () => {
      expect(await screen.findByLabelText('Confirm download')).toBeInTheDocument()
    })


    window.URL.createObjectURL = jest.fn()
    document.createElement = jest.fn().mockReturnValue({
      click: jest.fn()
    })

    await act(async () => {
      await userEvent.click(await screen.findByLabelText('Confirm download'))
    })

    expect(window.URL.createObjectURL).toHaveBeenCalled()
  })
})
