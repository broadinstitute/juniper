import { render } from '@testing-library/react'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormContentJsonEditor } from './FormContentJsonEditor'

const formContent: FormContent = {
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

// TODO (JN-1384): Fix tests / create new tests for the new JSON editor.
describe('FormContentJsonEditor', () => {
  // eslint-disable-next-line jest/expect-expect
  it('renders form content as JSON', () => {
    // Act
    render(<FormContentJsonEditor initialValue={formContent} onChange={jest.fn()}/>)

    // Assert
    // const expectedContent = JSON.stringify(formContent, null, 2).replace(/\s+/g, ' ') // Collapse whitespace
  })
  //
  // eslint-disable-next-line jest/no-commented-out-tests
  // it('sets readonly attribute on textarea', () => {
  //   // Act
  //   render(<FormContentJsonEditor initialValue={formContent} readOnly onChange={jest.fn()} />)
  //
  //   // Assert
  //   const textArea = screen.getByRole('textbox')
  //   expect(textArea).toHaveAttribute('readOnly')
  // })
  //
  // eslint-disable-next-line jest/no-commented-out-tests
  // it('calls onChange when edited with valid JSON', async () => {
  //   // Arrange
  //   const user = userEvent.setup()
  //
  //   const onChange = jest.fn()
  //   render(<FormContentJsonEditor initialValue={formContent} onChange={onChange} />)
  //
  //   // Act
  //   const textArea = screen.getByRole('textbox')
  //   // Replaces "First name" with "Given name"
  //   await act(() => user.type(textArea, 'Given', { initialSelectionStart: 159, initialSelectionEnd: 164 }))
  //
  //   // Assert
  //   const expectedEditedContent = cloneDeep(formContent)
  //   ;(expectedEditedContent.pages[0].elements[0] as TitledQuestion).title = 'Given name'
  //
  //   expect(onChange).toHaveBeenCalledWith([], expectedEditedContent)
  // })
  //
  // eslint-disable-next-line jest/no-commented-out-tests
  // it('calls onChange when edited with invalid JSON', async () => {
  //   // Arrange
  //   const user = userEvent.setup()
  //
  //   const onChange = jest.fn()
  //   render(<FormContentJsonEditor initialValue={formContent} onChange={onChange} />)
  //
  //   // Act
  //   const textArea = screen.getByRole('textbox')
  //   // Removes quotes around "First name"
  //   await act(() => user.type(textArea, 'First name', { initialSelectionStart: 158, initialSelectionEnd: 170 }))
  //
  //   // Assert
  //   expect(onChange).toHaveBeenCalledWith(
  //     [expect.stringContaining('Unexpected token')], undefined)
  // })
  //
  // eslint-disable-next-line jest/no-commented-out-tests
  // it('shows feedback when edited with invalid JSON', async () => {
  //   // Arrange
  //   const user = userEvent.setup()
  //
  //   const onChange = jest.fn()
  //   render(<FormContentJsonEditor initialValue={formContent} onChange={onChange} />)
  //
  //   // Act
  //   const textArea = screen.getByRole('textbox')
  //   // Removes quotes around "First name"
  //   await act(() => user.type(textArea, 'First name', { initialSelectionStart: 158, initialSelectionEnd: 170 }))
  //
  //   // Assert
  //   expect(textArea).toHaveClass('is-invalid')
  // })
})
