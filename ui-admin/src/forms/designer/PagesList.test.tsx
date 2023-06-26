import { act, getByLabelText, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { PagesList } from './PagesList'

describe('PagesList', () => {
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
      },
      {
        elements: [
          {
            name: 'page2Intro',
            type: 'html',
            html: '<p>This is page 2</p>'
          }
        ]
      },
      {
        elements: [
          {
            name: 'page3Intro',
            type: 'html',
            html: '<p>This is page 3</p>'
          }
        ]
      }
    ]
  }

  it('renders list of pages', () => {
    // Act
    render(<PagesList formContent={formContent} readOnly={false} onChange={jest.fn()} />)

    // Assert
    const listItems = screen.getAllByRole('listitem')
    expect(listItems.map(el => el.textContent)).toEqual(['Page 1', 'Page 2', 'Page 3'])
  })

  it('allows reordering pages', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PagesList formContent={formContent} readOnly={false} onChange={onChange} />)

    // Act
    const listItems = screen.getAllByRole('listitem')
    const movePage2UpButton = getByLabelText(listItems[1], 'Move this page before the previous one')
    await act(() => user.click(movePage2UpButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...formContent,
      pages: [
        {
          elements: [
            {
              name: 'page2Intro',
              type: 'html',
              html: '<p>This is page 2</p>'
            }
          ]
        },
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
          elements: [
            {
              name: 'page3Intro',
              type: 'html',
              html: '<p>This is page 3</p>'
            }
          ]
        }
      ]
    })

    const movePage2DownButton = getByLabelText(listItems[1], 'Move this page after the next one')
    await act(() => user.click(movePage2DownButton))

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
          elements: [
            {
              name: 'page3Intro',
              type: 'html',
              html: '<p>This is page 3</p>'
            }
          ]
        },
        {
          elements: [
            {
              name: 'page2Intro',
              type: 'html',
              html: '<p>This is page 2</p>'
            }
          ]
        }
      ]
    })
  })
})
