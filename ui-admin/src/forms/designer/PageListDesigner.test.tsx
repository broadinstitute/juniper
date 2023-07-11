import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { PageListDesigner } from './PageListDesigner'

describe('PageListDesigner', () => {
  const formContent: FormContent = {
    title: 'Test form',
    pages: [
      {
        elements: [
          {
            name: 'page1Intro',
            type: 'html',
            html: '<p>This is page 1</p>'
          }
        ]
      }
    ]
  }

  it('allows creating pages', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PageListDesigner formContent={formContent} readOnly={false} onChange={onChange} />)

    // Act
    const addPageButton = screen.getByText('Add page')
    await act(() => user.click(addPageButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...formContent,
      pages: [
        {
          elements: [
            {
              name: 'page1Intro',
              type: 'html',
              html: '<p>This is page 1</p>'
            }
          ]
        },
        {
          elements: []
        }
      ]
    })
  })

  it('shows a message when there are no pages', async () => {
    // Arrange
    const onChange = jest.fn()
    render(<PageListDesigner formContent={{ title: 'Empty form', pages: [] }} readOnly={false} onChange={onChange} />)

    // Assert
    expect(screen.getByText('This form does not contain any pages.')).toBeInTheDocument()
  })
})
