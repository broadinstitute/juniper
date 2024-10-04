/* eslint-disable jest/expect-expect */
import {
  act,
  render,
  screen,
  waitFor,
  within
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import {
  FormContent,
  MockI18nProvider
} from '@juniper/ui-core'

import { FormPreview } from './FormPreview'
import { MOCK_ENGLISH_LANGUAGE } from '../test-utils/mocking-utils'

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
    render(
      <MockI18nProvider>
        <FormPreview formContent={formContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
      </MockI18nProvider>)


    screen.debug()
    // Assert
    screen.getAllByText('First name')
    screen.getAllByText('Last name')
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

        render(
          <MockI18nProvider>
            <FormPreview formContent={formContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
          </MockI18nProvider>)

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

        render(
          <MockI18nProvider>
            <FormPreview formContent={formContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
          </MockI18nProvider>)

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

    describe('language selector', () => {
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

      it('can show Spanish text', async () => {
        render(
          <MockI18nProvider>
            <FormPreview
              formContent={localizedFormContent as unknown as FormContent}
              currentLanguage={{ languageCode: 'es', id: '', languageName: 'Spanish' }}
            />
          </MockI18nProvider>)

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
        render(
          <MockI18nProvider>
            <FormPreview formContent={formContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
          </MockI18nProvider>)

        // Assert
        expect(screen.queryAllByLabelText('Hidden question')).toHaveLength(0)
      })

      it('can show invisible questions', async () => {
        // Arrange
        const user = userEvent.setup()

        render(
          <MockI18nProvider>
            <FormPreview formContent={formContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
          </MockI18nProvider>)

        // Act
        // Show invisible questions
        const showInvisibleElementsCheckbox = screen.getByLabelText('Show invisible questions')
        await act(() => user.click(showInvisibleElementsCheckbox))

        // Assert
        screen.getAllByLabelText('Hidden question')
      })
    })

    const dyanmicTextFormContent: FormContent = {
      title: 'Test survey',
      pages: [
        {
          elements: [
            {
              name: 'test_hello',
              type: 'text',
              title: 'Hello participant {profile.givenName} {profile.familyName}'
            },
            {
              name: 'test_proxy_hello',
              type: 'text',
              title: 'you are proxying {proxyProfile.givenName} {proxyProfile.familyName}'
            }
          ]
        }
      ]
    }

    it('updates dynamic text from participant profile', async () => {
      const user = userEvent.setup()
      render(
        <MockI18nProvider>
          <FormPreview formContent={dyanmicTextFormContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
        </MockI18nProvider>)
      // with no values specified, the dynamic text should not be replaced
      expect(screen.getByText('Hello participant {profile.givenName} {profile.familyName}'))
        .toBeInTheDocument()
      const participantFields = screen.getByTestId('profileInfoFields')

      await user.type(within(participantFields).getByLabelText('Given name'), 'Jonas')
      expect(screen.getByText('Hello participant Jonas {profile.familyName}'))
        .toBeInTheDocument()

      await user.type(within(participantFields).getByLabelText('Family name'), 'Salk')
      expect(screen.getByText('Hello participant Jonas Salk'))
        .toBeInTheDocument()

      // proxy fields should be unchanged
      expect(screen.getByText('you are proxying {proxyProfile.givenName} {proxyProfile.familyName}'))
        .toBeInTheDocument()
    })

    it('updates dynamic text from proxy profile', async () => {
      const user = userEvent.setup()
      render(
        <MockI18nProvider>
          <FormPreview formContent={dyanmicTextFormContent} currentLanguage={MOCK_ENGLISH_LANGUAGE}/>
        </MockI18nProvider>)
      // with no values specified, the dynamic text should not be replaced
      expect(screen.getByText('you are proxying {proxyProfile.givenName} {proxyProfile.familyName}'))
        .toBeInTheDocument()
      const proxyField = screen.getByTestId('proxyInfoFields')

      await user.type(within(proxyField).getByLabelText('Given name'), 'Child')
      expect(screen.getByText('you are proxying Child {proxyProfile.familyName}'))
        .toBeInTheDocument()

      await user.type(within(proxyField).getByLabelText('Family name'), 'Salk')
      expect(screen.getByText('you are proxying Child Salk'))
        .toBeInTheDocument()
    })
  })
})
