import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { FormElement } from '@juniper/ui-core'

import { ElementList } from './ElementList'

describe('ElementList', () => {
  const elements: FormElement[] = [
    {
      name: 'foo',
      type: 'html',
      html: '<p>foo</p>'
    },
    {
      name: 'bar',
      type: 'html',
      html: '<p>bar</p>'
    },
    {
      name: 'baz',
      type: 'html',
      html: '<p>baz</p>'
    }
  ]

  it('renders elements', () => {
    // Act
    render(<ElementList readOnly={false} value={elements} onChange={jest.fn()} />)

    // Assert
    const listItems = screen.getAllByRole('listitem')
    expect(listItems.map(el => el.textContent)).toEqual(['foo', 'bar', 'baz'])
  })

  it('allows reodering elements', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<ElementList readOnly={false} value={elements} onChange={onChange} />)

    // Act
    const moveBarUpButton = screen.getAllByLabelText('Move this element before the previous one')[1]
    await user.click(moveBarUpButton)

    // Assert
    expect(onChange).toHaveBeenCalledWith([
      {
        name: 'bar',
        type: 'html',
        html: '<p>bar</p>'
      },
      {
        name: 'foo',
        type: 'html',
        html: '<p>foo</p>'
      },
      {
        name: 'baz',
        type: 'html',
        html: '<p>baz</p>'
      }
    ])

    const moveBarDownButton = screen.getAllByLabelText('Move this element after the next one')[1]
    await user.click(moveBarDownButton)

    // Assert
    expect(onChange).toHaveBeenCalledWith([
      {
        name: 'foo',
        type: 'html',
        html: '<p>foo</p>'
      },
      {
        name: 'baz',
        type: 'html',
        html: '<p>baz</p>'
      },
      {
        name: 'bar',
        type: 'html',
        html: '<p>bar</p>'
      }
    ])
  })
})
