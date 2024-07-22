import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { FormPanel } from '@juniper/ui-core'

import { PanelElementList } from './PanelElementList'

describe('PanelElementList', () => {
  const elements: FormPanel['elements'] = [
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

  it('renders clickable', async () => {
    const setSelectedElementPath = jest.fn()
    render(<PanelElementList readOnly={false} value={elements}
      onChange={jest.fn()} selectedElementPath={'pages[0].elements[0]'}
      setSelectedElementPath={setSelectedElementPath}/>)

    const listItems = screen.getAllByRole('listitem')
    expect(listItems.map(el => el.textContent)).toEqual(['foo', 'bar', 'baz'])
    await userEvent.click(screen.getByText('bar'))
    expect(setSelectedElementPath).toHaveBeenCalledWith('pages[0].elements[0].elements[1]')
  })

  it('allows reodering elements', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PanelElementList readOnly={false} value={elements} onChange={onChange}  selectedElementPath={''}
      setSelectedElementPath={jest.fn()}/>)

    // Act
    const moveBarUpButton = screen.getAllByLabelText('Move this element before the previous one')[1]
    await act(() => user.click(moveBarUpButton))

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
    await act(() => user.click(moveBarDownButton))

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

  it('allows removing elements from panel', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PanelElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
      setSelectedElementPath={jest.fn()} />)

    // Act
    const deleteBarButton = screen.getAllByLabelText('Move this element out of panel')[1]
    await act(() => user.click(deleteBarButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith(
      [
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
      ],
      {
        name: 'bar',
        type: 'html',
        html: '<p>bar</p>'
      }
    )
  })

  it('allows deleting elements', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PanelElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
      setSelectedElementPath={jest.fn()} />)

    // Act
    const deleteBarButton = screen.getAllByLabelText('Delete this element')[1]
    await act(() => user.click(deleteBarButton))

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
      }
    ])
  })
})
