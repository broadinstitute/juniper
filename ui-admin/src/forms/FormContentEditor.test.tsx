import { render } from '@testing-library/react'
import React from 'react'

import { FormContentEditor } from './FormContentEditor'
import { mockPortal } from 'test-utils/mocking-utils'

//This is valid JSON, but invalid survey JSON
const formContent: string = JSON.stringify({
  title: 'Test survey',
  pages: [
    {}
  ]
})

describe('FormContentEditor', () => {
  it('should trap FormDesigner errors in an ErrorBoundary', () => {
    // avoid cluttering the console with the error message from the expected error
    jest.spyOn(console, 'error').mockImplementation(jest.fn())
    const { container } = render(<FormContentEditor
      portal={mockPortal()}
      initialAnswerMappings={[]}
      currentLanguage={{ languageCode: 'en', languageName: 'English', id: '' }}
      supportedLanguages={[]}
      initialContent={formContent} visibleVersionPreviews={[]} readOnly={false}
      onFormContentChange={jest.fn()}
      onAnswerMappingChange={jest.fn()}
    />)

    // Our custom ErrorBoundary text
    expect(container).toHaveTextContent('Something went wrong')
    // JSON Editor and Preview tabs should still be visible
    expect(container).toHaveTextContent('JSON Editor')
    expect(container).toHaveTextContent('Preview')
  })
})
