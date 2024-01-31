/* eslint-disable jest/expect-expect */
import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormPreview } from './FormPreview'

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

describe('FormPreview', () => {
  it('renders form', () => {
    // Act
    render(<FormPreview formContent={formContent} />)

    // Assert
    screen.getAllByLabelText('First name')
    screen.getAllByLabelText('Last name')
  })

  describe('options', () => {
    describe('ignore validation', () => {
      const formContent: FormContent = {
        title: 'Test survey',
        pages: [
          {
            elements: [
              {
                name: 'question1',
                title: 'First question',
                type: 'text',
                isRequired: true
              }
            ]
          },
          {
            elements: [
              {
                name: 'question2',
                title: 'Second question',
                type: 'text',
                isRequired: true
              }
            ]
          }
        ]
      }

      it('ignores form validation by default', async () => {
        // Arrange
        const user = userEvent.setup()

        render(<FormPreview formContent={formContent} />)

        // Act
        // Attempt to advance to the next page.
        const nextPageButton = screen.getByTitle('Next')
        await act(() => user.click(nextPageButton))

        // Assert
        // Reached the second page despite the first page requiring an answer.
        screen.getByText('Second question')
      })

      it('can require form validation', async () => {
        // Arrange
        const user = userEvent.setup()

        render(<FormPreview formContent={formContent} />)

        // Act
        // Turn off 'Ignore validation'
        const ignoreValidationCheckbox = screen.getByLabelText('Ignore validation')
        await act(() => user.click(ignoreValidationCheckbox))

        // Attempt to advance to the next page.
        const nextPageButton = screen.getByTitle('Next')
        await act(() => user.click(nextPageButton))

        // Assert
        // Still on the first page.
        screen.getByText('First question')
        screen.getByText('Response required.')
      })
    })

    describe('locale input', () => {
      const localizedFormContent = {
        title: {
          default: 'Test survey',
          es: 'Encuesta de prueba'
        },
        pages: [
          {
            elements: [
              {
                name: 'test_firstName',
                type: 'text',
                title: {
                  default: 'First name',
                  es: 'Nombre'
                },
                isRequired: true
              },
              {
                name: 'test_lastName',
                type: 'text',
                title: {
                  default: 'Last name',
                  es: 'Apellido'
                },
                isRequired: true
              }
            ]
          }
        ]
      }

      it('defaults to English', () => {
        render(<FormPreview formContent={localizedFormContent as unknown as FormContent} />)

        screen.getByText('First name')
        screen.getByText('Last name')
      })

      it('can switch to Spanish', async () => {
        const user = userEvent.setup()

        render(<FormPreview formContent={localizedFormContent as unknown as FormContent} />)

        const localeInput = screen.getByLabelText('Locale')
        await act(() => user.type(localeInput, 'es'))

        waitFor(() => {
          screen.getByText('Nombre')
          screen.getByText('Apellido')
        })
      })
    })

    describe('show invisible questions', () => {
      const formContent: FormContent = {
        title: 'Test survey',
        pages: [
          {
            elements: [
              {
                name: 'visible_question',
                title: 'Visible question',
                type: 'text'
              },
              {
                name: 'hidden_question',
                title: 'Hidden question',
                type: 'text',
                visibleIf: 'false'
              }
            ]
          }
        ]
      }

      it('hides invisible questions by default', () => {
        // Act
        render(<FormPreview formContent={formContent} />)

        // Assert
        expect(screen.queryAllByLabelText('Hidden question')).toHaveLength(0)
      })

      it('can show invisible questions', async () => {
        // Arrange
        const user = userEvent.setup()

        render(<FormPreview formContent={formContent} />)

        // Act
        // Show invisible questions
        const showInvisibleElementsCheckbox = screen.getByLabelText('Show invisible questions')
        await act(() => user.click(showInvisibleElementsCheckbox))

        // Assert
        screen.getAllByLabelText('Hidden question')
      })
    })
  })
})
