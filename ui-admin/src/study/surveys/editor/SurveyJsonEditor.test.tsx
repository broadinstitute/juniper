import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { cloneDeep } from 'lodash'
import React from 'react'

import { FormContent, Question } from '@juniper/ui-core'

import { SurveyJsonEditor } from './SurveyJsonEditor'

const survey: FormContent = {
  title: 'Test survey',
  pages: [
    {
      elements: [
        {
          name: 'test_firstName',
          type: 'text',
          title: 'First name',
          isRequired: true
        },
        {
          name: 'test_lastName',
          type: 'text',
          title: 'Last name',
          isRequired: true
        }
      ]
    }
  ]
}

describe('SurveyJsonEditor', () => {
  it('renders survey as JSON', () => {
    // Act
    const { container } = render(<SurveyJsonEditor initialValue={survey} onChange={jest.fn()} />)

    // Assert
    const expectedContent = JSON.stringify(survey, null, 2).replace(/\s+/g, ' ') // Collapse whitespace
    expect(container).toHaveTextContent(expectedContent)
  })

  it('sets readonly attribute on textatrea', () => {
    // Act
    render(<SurveyJsonEditor initialValue={survey} readOnly onChange={jest.fn()} />)

    // Assert
    const textArea = screen.getByRole('textbox')
    expect(textArea).toHaveAttribute('readOnly')
  })

  it('calls onChange when edited with valid JSON', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<SurveyJsonEditor initialValue={survey} onChange={onChange} />)

    // Act
    const textArea = screen.getByRole('textbox')
    // Replaces "First name" with "Given name"
    await act(() => user.type(textArea, 'Given', { initialSelectionStart: 159, initialSelectionEnd: 164 }))

    // Assert
    const expectedEditedSurvey = cloneDeep(survey)
    ;(expectedEditedSurvey.pages[0].elements[0] as Question).title = 'Given name'

    expect(onChange).toHaveBeenCalledWith(true, expectedEditedSurvey)
  })

  it('calls onChange when edited with invalid JSON', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<SurveyJsonEditor initialValue={survey} onChange={onChange} />)

    // Act
    const textArea = screen.getByRole('textbox')
    // Removes quotes around "First name"
    await act(() => user.type(textArea, 'First name', { initialSelectionStart: 158, initialSelectionEnd: 170 }))

    // Assert
    expect(onChange).toHaveBeenCalledWith(false, undefined)
  })
})
