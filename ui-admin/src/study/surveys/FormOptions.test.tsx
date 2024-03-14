import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockSurvey } from 'test-utils/mocking-utils'
import FormOptionsModal from './FormOptionsModal'
import userEvent from '@testing-library/user-event'

jest.mock('api/api', () => ({
  getSurvey: () => {
    return Promise.resolve(mockSurvey())
  }
}))

describe('FormOptions', () => {
  test('allows changing a survey to be required', async () => {
    const updateWorkingForm = jest.fn()
    render(<FormOptionsModal
      workingForm={mockSurvey()}
      updateWorkingForm={updateWorkingForm}
      onDismiss={jest.fn()}
    />)

    await userEvent.click(screen.getByLabelText('Required'))
    expect(updateWorkingForm).toHaveBeenCalledWith({
      ...mockSurvey(),
      required: true
    })
  })

  test('allows changing a survey to be auto-updating of versions', async () => {
    const updateWorkingForm = jest.fn()
    render(<FormOptionsModal
      workingForm={mockSurvey()}
      updateWorkingForm={updateWorkingForm}
      onDismiss={jest.fn()}
    />)

    await userEvent.click(screen.getByLabelText('Auto-update participant tasks', { exact: false }))
    expect(updateWorkingForm).toHaveBeenCalledWith({
      ...mockSurvey(),
      autoUpdateTaskAssignments: true
    })
  })
})
