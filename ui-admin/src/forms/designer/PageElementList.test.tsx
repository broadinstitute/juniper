import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { FormElement, FormPanel } from '@juniper/ui-core'

import { PageElementList } from './PageElementList'

describe('PageElementList', () => {
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
    render(<PageElementList readOnly={false} value={elements} onChange={jest.fn()} selectedElementPath={''}
      setSelectedElementPath={jest.fn()}/>)

    // Assert
    const listItems = screen.getAllByRole('listitem')
    expect(listItems.map(el => el.textContent)).toEqual(['foo', 'bar', 'baz'])
  })

  it('allows reodering elements', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
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

  describe('deleting elements', () => {
    const elements: FormElement[] = [
      { name: 'q1', title: 'q1', type: 'text' },
      {
        title: 'My panel',
        type: 'panel',
        elements: []
      },
      {
        title: 'My panel',
        type: 'panel',
        elements: [
          { name: 'q2', title: 'q2', type: 'text' },
          { name: 'q3', title: 'q3', type: 'text' }
        ]
      }
    ]

    it('allows deleting questions', async () => {
      // Arrange
      const user = userEvent.setup()

      const onChange = jest.fn()
      render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
        setSelectedElementPath={jest.fn()}/>)

      // Act
      const deleteButton = screen.getAllByLabelText('Delete this element')[0]
      await act(() => user.click(deleteButton))

      // Assert
      expect(onChange).toHaveBeenCalledWith(elements.slice(1))
    })

    it('allows deleting empty panels', async () => {
      // Arrange
      const user = userEvent.setup()

      const onChange = jest.fn()
      render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
        setSelectedElementPath={jest.fn()}/>)

      // Act
      const deleteButton = screen.getAllByLabelText('Delete this element')[1]
      await act(() => user.click(deleteButton))

      // Assert
      expect(onChange).toHaveBeenCalledWith([...elements.slice(0, 1), ...elements.slice(2)])
    })

    describe('deleting panels with content', () => {
      it('prompts for confirmation before deleting panels with content', async () => {
        // Arrange
        const user = userEvent.setup()

        const onChange = jest.fn()
        render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
          setSelectedElementPath={jest.fn()}/>)

        // Act
        const deleteButton = screen.getAllByLabelText('Delete this element')[2]
        await act(() => user.click(deleteButton))

        // Assert
        screen.getByText('Delete panel contents?')
        expect(onChange).not.toHaveBeenCalled()
      })

      it('allows deleting content with panel', async () => {
        // Arrange
        const user = userEvent.setup()

        const onChange = jest.fn()
        render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
          setSelectedElementPath={jest.fn()}/>)

        const deleteButton = screen.getAllByLabelText('Delete this element')[2]
        await act(() => user.click(deleteButton))

        // Act
        await act(() => user.click(screen.getByText('Delete contents')))

        // Assert
        expect(onChange).toHaveBeenCalledWith(elements.slice(0, 2))
      })

      it('allows keeping panel content on page', async () => {
        // Arrange
        const user = userEvent.setup()

        const onChange = jest.fn()
        render(<PageElementList readOnly={false} value={elements} onChange={onChange} selectedElementPath={''}
          setSelectedElementPath={jest.fn()}/>)

        const deleteButton = screen.getAllByLabelText('Delete this element')[2]
        await act(() => user.click(deleteButton))

        // Act
        await act(() => user.click(screen.getByText('Keep contents')))

        // Assert
        expect(onChange).toHaveBeenCalledWith([
          ...elements.slice(0, 2),
          ...(elements[2] as FormPanel).elements
        ])
      })
    })
  })
})
