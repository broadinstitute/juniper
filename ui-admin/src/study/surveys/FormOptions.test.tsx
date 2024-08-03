import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockExpressionApis, mockStudyEnvContext, mockSurvey } from 'test-utils/mocking-utils'
import FormOptionsModal from './FormOptionsModal'
import { userEvent } from '@testing-library/user-event'


describe('FormOptions', () => {
  const studyEnvContext = mockStudyEnvContext()

  test('allows changing a survey to be required', async () => {
    mockExpressionApis()
    const updateWorkingForm = jest.fn()
    render(<FormOptionsModal
      studyEnvContext={studyEnvContext}
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
    mockExpressionApis()
    const studyEnvContext = mockStudyEnvContext()

    const updateWorkingForm = jest.fn()
    render(<FormOptionsModal
      studyEnvContext={studyEnvContext}
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

  test('admin forms should be admin-editable by default', async () => {
    mockExpressionApis()
    const updateWorkingForm = jest.fn()
    render(<FormOptionsModal
      studyEnvContext={studyEnvContext}
      workingForm={mockSurvey('ADMIN')}
      updateWorkingForm={updateWorkingForm}
      onDismiss={jest.fn()}
    />)

    //we don't display this option, because it's assumed to be true for admin forms
    expect(screen.queryByText('Allow study staff to edit participant responses')).not.toBeInTheDocument()
  })
})
