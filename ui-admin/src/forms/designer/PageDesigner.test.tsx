import { act, getAllByRole, getByLabelText, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { FormContentPage } from '@juniper/ui-core'

import { PageDesigner } from './PageDesigner'

describe('PageDesigner', () => {
  describe('creating a panel', () => {
    const page: FormContentPage = {
      elements: [
        { name: 'q1', title: '1?', type: 'text' },
        { name: 'q2', title: '2?', type: 'text' },
        { name: 'q3', type: 'html', html: '3' },
        {
          title: 'My panel',
          type: 'panel',
          elements: [
            { name: 'q4', title: '4?', type: 'text' },
            { name: 'q5', title: '5?', type: 'text' }
          ]
        },
        { name: 'q6', title: '6?', type: 'text' }
      ]
    }

    it('shows a modal with questions available to include in panel', async () => {
      // Arrange
      const user = userEvent.setup()

      render(<PageDesigner readOnly={false} value={page} onChange={jest.fn()}
        setSelectedElementPath={jest.fn()} selectedElementPath={'pages[0]'} addNextQuestion={jest.fn()}/>)

      const createPanelButton = screen.getByText('Add panel')

      // Act
      await act(() => user.click(createPanelButton))

      // Assert
      const elementsToIncludeFieldset = screen.getByRole('group', { name: 'Select elements to include' })
      // Should only include questions / HTML elements, not panels
      const elementCheckboxes = getAllByRole(elementsToIncludeFieldset, 'checkbox')
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const elementNames = elementCheckboxes.map(el => (el as HTMLInputElement).labels![0].textContent)
      expect(elementNames).toEqual(['q1', 'q2', 'q3', 'q6'])
    })

    it('updates page with new panel', async () => {
      // Arrange
      const user = userEvent.setup()

      const onChange = jest.fn()
      render(<PageDesigner readOnly={false} value={page} onChange={onChange}
        setSelectedElementPath={jest.fn()} selectedElementPath={'pages[0]'} addNextQuestion={jest.fn()}/>)

      await act(() => user.click(screen.getByText('Add panel')))

      // Act
      const elementsToIncludeFieldset = screen.getByRole('group', { name: 'Select elements to include' })
      await act(() => user.click(getByLabelText(elementsToIncludeFieldset, 'q3')))
      await act(() => user.click(getByLabelText(elementsToIncludeFieldset, 'q6')))

      await act(() => user.click(screen.getByText('Create panel')))

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...page,
        elements: [
          { name: 'q1', title: '1?', type: 'text' },
          { name: 'q2', title: '2?', type: 'text' },
          {
            type: 'panel',
            title: '',
            elements: [
              { name: 'q3', type: 'html', html: '3' },
              { name: 'q6', title: '6?', type: 'text' }
            ]
          },
          {
            type: 'panel',
            title: 'My panel',
            elements: [
              { name: 'q4', title: '4?', type: 'text' },
              { name: 'q5', title: '5?', type: 'text' }
            ]
          }
        ]
      })
    })
  })
})
