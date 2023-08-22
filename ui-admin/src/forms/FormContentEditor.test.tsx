import { render } from '@testing-library/react'
import React from 'react'

import { FormContentEditor } from './FormContentEditor'

//This is valid JSON, but invalid survey JSON
const formContent: string = JSON.stringify({
  title: 'Test survey',
  pages: [
    {}
  ]
})

describe('FormContentEditor', () => {
  it('should trap errors in an ErrorBoundary', () => {
    // Arrange
    const { container } = render(<FormContentEditor
      initialContent={formContent} readOnly={false} onChange={jest.fn()}
    />)

    // Assert
    // Our custom ErrorBoundary text
    expect(container).toHaveTextContent('Something went wrong')
    // JSON Editor and Preview tabs should still be visible
    expect(container).toHaveTextContent('JSON Editor')
    expect(container).toHaveTextContent('Preview')
  })
})
