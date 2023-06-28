import { fireEvent, render, screen } from '@testing-library/react'
import React from 'react'

import { HtmlElement } from '@juniper/ui-core'

import { HtmlDesigner } from './HtmlDesigner'

describe('HtmlDesigner', () => {
  const element: HtmlElement = {
    name: 'testElement',
    type: 'html',
    html: '<h2>Section heading</h2><p>This is a section.</p>'
  }

  it('renders HTML', () => {
    // Act
    render(<HtmlDesigner element={element} readOnly={false} onChange={jest.fn()} />)

    // Assert
    const htmlTextarea = screen.getByLabelText('HTML')
    expect(htmlTextarea.textContent).toBe('<h2>Section heading</h2><p>This is a section.</p>')
  })

  it('allows editing HTML', () => {
    // Arrange
    const onChange = jest.fn()
    render(<HtmlDesigner element={element} readOnly={false} onChange={onChange} />)

    // Act
    const htmlTextarea = screen.getByLabelText('HTML')
    fireEvent.change(htmlTextarea, {
      target: {
        value: '<h2>New section heading</h2><p>This is a section.</p>'
      }
    })

    // Assert
    expect(onChange).toHaveBeenLastCalledWith({
      name: 'testElement',
      type: 'html',
      html: '<h2>New section heading</h2><p>This is a section.</p>'
    })
  })
})
